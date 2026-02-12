package ftp.commands;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

class PwdCommandTest {

    private UserSession session;
    private PathUtils pathUtils;
    private PwdCommand command;
    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        session = new UserSession();
        pathUtils = new PathUtils(System.getProperty("java.io.tmpdir")); // temp folder as root
        command = new PwdCommand(pathUtils);

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        // session is not logged in by default
        AuthException ex = assertThrows(AuthException.class, () ->
                command.execute(null, session, out)
        );
        assertEquals("Not logged in.", ex.getMessage());
    }

    @Test
    void shouldReturnCurrentDirectory_whenLoggedIn() throws FTPException {
        // log in
        session.login("user");
        session.setCurrentDirectory("/folder/sub");

        command.execute(null, session, out);

        out.flush();
        String response = stringWriter.toString();

        assertTrue(response.contains("257 \"/folder/sub\" is the current directory."));
    }
}
