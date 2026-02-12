package ftp.commands;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintWriter;
import java.io.StringWriter;

import ftp.model.UserSession;

public class SystCommandTest {

    private SystCommand systCommand;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        systCommand = new SystCommand();
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testSystResponse() throws Exception {
        systCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("215"), "Should return 215");
        assertTrue(output.contains("UNIX Type: L8"), "Should contain UNIX Type: L8");
    }

    @Test
    void testSystWithArguments() throws Exception {
        // SYST should ignore arguments
        systCommand.execute("ignored", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("215"), "Should return 215 regardless of arguments");
        assertTrue(output.contains("UNIX Type: L8"), "Should contain UNIX Type: L8");
    }

    @Test
    void testSystNoSessionDependency() throws Exception {
        // SYST should work regardless of login status
        systCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("215"), "Should work when not logged in");
        
        session.login("testuser");
        stringWriter.getBuffer().setLength(0);
        
        systCommand.execute(null, session, printWriter);
        
        output = stringWriter.toString();
        assertTrue(output.contains("215"), "Should work when logged in");
    }

    @Test
    void testSystResponseFormat() throws Exception {
        systCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString().trim();
        assertEquals("215 UNIX Type: L8", output, "Response should match exact format");
    }
}
