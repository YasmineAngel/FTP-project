package ftp.commands;

import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.PrintWriter;
import java.net.Socket;

import static org.mockito.Mockito.*;

public class AborCommandTest {

    private UserSession mockSession;
    private PrintWriter mockWriter;
    private Socket mockSocket;
    private AborCommand command;

    @BeforeEach
    void setUp() {
        mockSession = mock(UserSession.class);
        mockWriter = mock(PrintWriter.class);
        mockSocket = mock(Socket.class);
        command = new AborCommand();
    }

    @Test
    void testAbortActiveSocketInCurrentSession() throws Exception {
        when(mockSession.getActiveDataSocket()).thenReturn(mockSocket);
        when(mockSocket.isClosed()).thenReturn(false);

        command.execute(null, mockSession, mockWriter);

        verify(mockSocket).close();
        verify(mockWriter).print("426 Connection closed; transfer aborted.\r\n");
        verify(mockWriter).print("221 Abort successful.\r\n");
        verify(mockWriter, atLeastOnce()).flush();
    }

    @Test
    void testAbortUsingSharedSessionRegistry() throws Exception {
        when(mockSession.getActiveDataSocket()).thenReturn(null);
        when(mockSession.getUsername()).thenReturn("yasmine");
        
        UserSession mainSession = mock(UserSession.class);
        when(mainSession.getActiveDataSocket()).thenReturn(mockSocket);
        when(mockSocket.isClosed()).thenReturn(false);

        try (MockedStatic<UserSession> mockedUserSession = mockStatic(UserSession.class)) {
            mockedUserSession.when(() -> UserSession.getSessionForUser("yasmine")).thenReturn(mainSession);

            command.execute(null, mockSession, mockWriter);

            verify(mockSocket).close();
            verify(mockWriter).print("426 Connection closed; transfer aborted.\r\n");
        }
    }

    @Test
    void testAbortWithNoActiveTransfer() throws Exception {
        when(mockSession.getActiveDataSocket()).thenReturn(null);
        when(mockSession.getPassiveServer()).thenReturn(null);

        command.execute(null, mockSession, mockWriter);

        verify(mockWriter).print("221 Abort ignored (no active transfer).\r\n");
    }
}