package ftp.commands;

import ftp.exceptions.AuthException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CdupCommandTest {

    private PathUtils pathUtils;
    private UserSession session;
    private CdupCommand command;

    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        pathUtils = mock(PathUtils.class);
        session = mock(UserSession.class);

        command = new CdupCommand(pathUtils);

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {

        when(session.isLoggedIn()).thenReturn(false);

        assertThrows(AuthException.class, () ->
                command.execute(null, session, out)
        );

        verify(session, never()).setCurrentDirectory(any());
    }

    @Test
    void shouldChangeToParentDirectory_whenLoggedIn() throws Exception {

        when(session.isLoggedIn()).thenReturn(true);
        when(session.getCurrentDirectory()).thenReturn("/folder/sub");

        Path currentPath = mock(Path.class);
        Path parentPath = mock(Path.class);

        when(pathUtils.resolve("/folder/sub", null)).thenReturn(currentPath);
        when(pathUtils.getParent(currentPath)).thenReturn(parentPath);
        when(pathUtils.toFtpPath(parentPath)).thenReturn("/folder");

        command.execute(null, session, out);

        out.flush();
        String response = stringWriter.toString();

        assertTrue(response.contains("250 Directory changed to /folder"));

        verify(session).setCurrentDirectory("/folder");
    }
}
