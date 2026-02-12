package ftp.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ftp.commands.UserCommand;
import ftp.exceptions.FTPException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;

class UserCommandTest {

    private UserCommand userCommand;
    private UserSession session;
    private StringWriter output;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        userCommand = new UserCommand();
        session = new UserSession();
        output = new StringWriter();
        printWriter = new PrintWriter(output, true);
    }

    @Test
    void testExecute_withValidUsername() throws FTPException {
        // Arrange
        String username = "testuser";

        // Act
        userCommand.execute(username, session, printWriter);

        // Assert session state
        assertEquals(username, session.getUsername(), "Username should be set in session");
        assertFalse(session.isLoggedIn(), "Session should not be logged in yet");

        // Assert output
        String response = output.toString().trim();
        assertEquals("331 Username ok, need password.", response);
    }

   

@Test
void testExecute_withNullUsername() {
    SyntaxException exception = assertThrows(SyntaxException.class, () -> {
        userCommand.execute(null, session, printWriter);
    });
    assertEquals("Syntax error in parameters.", exception.getMessage());
}

@Test
void testExecute_withBlankUsername() {
    SyntaxException exception = assertThrows(SyntaxException.class, () -> {
        userCommand.execute("   ", session, printWriter);
    });
    assertEquals("Syntax error in parameters.", exception.getMessage());
}
}
