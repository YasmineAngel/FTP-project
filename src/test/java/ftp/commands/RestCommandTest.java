package ftp.commands;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintWriter;
import java.io.StringWriter;

import ftp.model.UserSession;

public class RestCommandTest {

    private RestCommand restCommand;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        restCommand = new RestCommand();
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testRestNotLoggedIn() throws Exception {
        restCommand.execute("100", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("530"), "Should return 530 when not logged in");
    }

    @Test
    void testRestSuccess() throws Exception {
        session.login("testuser");
        
        restCommand.execute("100", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("350"), "Should return 350 Restart position accepted");
        assertTrue(output.contains("Restart position accepted"), "Should contain restart message");
        assertTrue(output.contains("100"), "Should contain the marker value");
        
        assertEquals(100, session.getRestartMarker(), "Restart marker should be set to 100");
    }

    @Test
    void testRestZero() throws Exception {
        session.login("testuser");
        
        restCommand.execute("0", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("350"), "Should return 350");
        assertEquals(0, session.getRestartMarker(), "Restart marker should be 0");
    }

    @Test
    void testRestClearWithNull() throws Exception {
        session.login("testuser");
        
        // Set a marker first
        session.setRestartMarker(500);
        assertEquals(500, session.getRestartMarker(), "Marker should be set");
        
        // Clear with null args
        restCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("350"), "Should return 350");
        assertEquals(0, session.getRestartMarker(), "Restart marker should be cleared to 0");
    }

    @Test
    void testRestClearWithEmpty() throws Exception {
        session.login("testuser");
        
        // Set a marker first
        session.setRestartMarker(500);
        
        // Clear with empty string
        restCommand.execute("", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("350"), "Should return 350");
        assertEquals(0, session.getRestartMarker(), "Restart marker should be cleared to 0");
    }

    @Test
    void testRestLargeNumber() throws Exception {
        session.login("testuser");
        
        restCommand.execute("999999999", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("350"), "Should return 350");
        assertEquals(999999999L, session.getRestartMarker(), "Should accept large numbers");
    }

    @Test
    void testRestNegative() throws Exception {
        session.login("testuser");
        
        restCommand.execute("-100", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for negative marker");
        assertEquals(0, session.getRestartMarker(), "Marker should not be set");
    }

    @Test
    void testRestInvalidFormat() throws Exception {
        session.login("testuser");
        
        restCommand.execute("abc", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for invalid format");
    }

    @Test
    void testRestMultipleCalls() throws Exception {
        session.login("testuser");
        
        restCommand.execute("100", session, printWriter);
        assertEquals(100, session.getRestartMarker(), "First marker should be 100");
        
        stringWriter.getBuffer().setLength(0);
        
        restCommand.execute("200", session, printWriter);
        assertEquals(200, session.getRestartMarker(), "Second marker should be 200");
        
        stringWriter.getBuffer().setLength(0);
        
        restCommand.execute("0", session, printWriter);
        assertEquals(0, session.getRestartMarker(), "Third marker should be 0");
    }

    @Test
    void testRestWithWhitespace() throws Exception {
        session.login("testuser");
        
        restCommand.execute("  500  ", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("350"), "Should return 350");
        assertEquals(500, session.getRestartMarker(), "Should handle whitespace");
    }
}
