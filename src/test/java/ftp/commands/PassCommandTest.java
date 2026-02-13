package ftp.commands;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ftp.exceptions.FTPException;
import ftp.exceptions.SequenceException;
import ftp.model.UserSession;

class PassCommandTest {

    private UserSession session;
    private PassCommand command;
    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        session = new UserSession();
        command = new PassCommand();

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowSequenceException_whenUsernameNotSet() {
        
        SequenceException ex = assertThrows(SequenceException.class, () ->
                command.execute("anyPassword", session, out)
        );
        assertEquals("Bad sequence of commands.", ex.getMessage());
    }

    @Test
    void shouldLoginSuccessfully_whenUsernameIsSet() throws FTPException {
        session.setUsername("user1"); // simulate USER command before PASS

        command.execute("password", session, out);

        // User should be logged in
        assertTrue(session.isLoggedIn());

        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("230 User logged in, proceed."));
    }
}
