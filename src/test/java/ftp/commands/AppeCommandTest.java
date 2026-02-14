package ftp.commands;

import ftp.data.TransferManager;
import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AppeCommandTest {

    private PathUtils mockPathUtils;
    private UserSession mockSession;
    private PrintWriter mockWriter;
    private Path mockPath;
    private AppeCommand command;

    @BeforeEach
    void setUp() {
        mockPathUtils = mock(PathUtils.class);
        mockSession = mock(UserSession.class);
        mockWriter = mock(PrintWriter.class);
        mockPath = mock(Path.class);
        command = new AppeCommand(mockPathUtils);

        when(mockSession.getCurrentDirectory()).thenReturn("/");
        when(mockPathUtils.resolve(anyString(), anyString())).thenReturn(mockPath);
        when(mockPathUtils.isWithinRoot(any())).thenReturn(true);
    }

    @Test
    void testAppeNotLoggedIn() throws Exception {
        when(mockSession.isLoggedIn()).thenReturn(false);
        command.execute("file.txt", mockSession, mockWriter);

        verify(mockWriter).print("530 Not logged in.\r\n");
    }

    @Test
    void testAppeSuccessPassiveMode() throws Exception {
        when(mockSession.isLoggedIn()).thenReturn(true);
        ftp.data.PassiveServer mockPassiveServer = mock(ftp.data.PassiveServer.class);
        java.net.ServerSocket mockServerSocket = mock(java.net.ServerSocket.class);
        
        when(mockSession.getPassiveServer()).thenReturn(mockPassiveServer);
        when(mockPassiveServer.getServerSocket()).thenReturn(mockServerSocket);
        
        Socket mockDataSocket = mock(Socket.class);
        when(mockServerSocket.accept()).thenReturn(mockDataSocket);
        when(mockSession.getTransferType()).thenReturn(UserSession.TransferType.BINARY);

        try (MockedStatic<TransferManager> mockedTM = mockStatic(TransferManager.class)) {
            command.execute("test.txt", mockSession, mockWriter);

            verify(mockWriter).print("150 Opening BINARY mode data connection for test.txt (append).\r\n");
            verify(mockWriter).print("250 Transfer complete.\r\n");
            mockedTM.verify(() -> TransferManager.receiveFile(eq(mockPath), eq(mockDataSocket), anyLong(), eq(true)));
        }
    }

    @Test
    void testAppeAbortDuringTransfer() throws Exception {
        when(mockSession.isLoggedIn()).thenReturn(true);
        ftp.data.PassiveServer mockPassiveServer = mock(ftp.data.PassiveServer.class);
        java.net.ServerSocket mockServerSocket = mock(java.net.ServerSocket.class);
        
        when(mockSession.getPassiveServer()).thenReturn(mockPassiveServer);
        when(mockPassiveServer.getServerSocket()).thenReturn(mockServerSocket);
        
        when(mockServerSocket.accept()).thenThrow(new java.io.IOException("Socket error"));
        when(mockSession.getTransferType()).thenReturn(UserSession.TransferType.BINARY);

        assertThrows(ConnectionException.class, () -> {
            command.execute("error.txt", mockSession, mockWriter);
        });
        
        verify(mockWriter).print(startsWith("425 Data connection error:"));
        verify(mockWriter, atLeastOnce()).flush();
    }
}