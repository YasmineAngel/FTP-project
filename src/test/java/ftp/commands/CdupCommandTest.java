package ftp.commands;

import ftp.exceptions.AuthException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class CdupCommandTest {

    private PathUtils pathUtils;
    private UserSession session;
    private CdupCommand command;

    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        // Use real objects
        session = new UserSession();
        pathUtils = new PathUtils("/ftp-root"); 
        command = new CdupCommand(pathUtils);

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        // Session is not logged in by default
        AuthException ex = assertThrows(AuthException.class, () -> command.execute(null, session, out));
        assertEquals("Not logged in.", ex.getMessage());
    }

    @Test
    void shouldChangeToParentDirectory_whenLoggedIn() throws Exception {
        // Log in and set current directory
        session.login("user");
        session.setCurrentDirectory("/folder/sub");

        // Execute CDUP
        command.execute(null, session, out);

        out.flush();
        String response = stringWriter.toString();

        // Check session directory updated
        assertEquals("/folder", session.getCurrentDirectory());

        // Check response
        assertTrue(response.contains("250 Directory changed to /folder"));
    }
}
