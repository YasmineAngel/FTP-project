package ftp.commands;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

import ftp.model.UserSession;

public class PasvCommandTest {

    private PasvCommand pasvCommand;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        pasvCommand = new PasvCommand();
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testPasvNotLoggedIn() throws Exception {
        pasvCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("530"), "Should return 530 when not logged in");
    }

    @Test
    void testPasvSuccess() throws Exception {
        session.login("testuser");
        
        pasvCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("227"), "Should return 227 Entering Passive Mode");
        assertTrue(output.contains("Entering Passive Mode"), "Should contain passive mode message");
        
        // Verify format: 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2)
        Pattern pattern = Pattern.compile("227 Entering Passive Mode \\(\\d+,\\d+,\\d+,\\d+,\\d+,\\d+\\)");
        assertTrue(pattern.matcher(output).find(), "Should match PASV response format");
        
        // Verify passive server is set
        assertNotNull(session.getPassiveServer(), "Passive server should be set");
        assertNotNull(session.getPassiveServer().getServerSocket(), "Server socket should be created");
    }

    @Test
    void testPasvReplacesExistingPassiveServer() throws Exception {
        session.login("testuser");
        
        // First PASV
        pasvCommand.execute(null, session, printWriter);
        ftp.data.PassiveServer firstServer = session.getPassiveServer();
        int firstPort = firstServer.getServerSocket().getLocalPort();
        
        stringWriter.getBuffer().setLength(0); // Clear output
        
        // Second PASV
        pasvCommand.execute(null, session, printWriter);
        ftp.data.PassiveServer secondServer = session.getPassiveServer();
        int secondPort = secondServer.getServerSocket().getLocalPort();
        
        // Should have different server (old one closed, new one created)
        assertNotSame(firstServer, secondServer, "Should create new passive server");
        // Ports might be different (though could theoretically be the same)
        assertNotNull(secondServer, "New passive server should be set");
    }

    @Test
    void testPasvResponseFormat() throws Exception {
        session.login("testuser");
        
        pasvCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        
        // Extract port from response
        // Format: 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2)
        String[] parts = output.split("\\(")[1].split("\\)")[0].split(",");
        assertEquals(6, parts.length, "Should have 6 comma-separated values");
        
        int p1 = Integer.parseInt(parts[4].trim());
        int p2 = Integer.parseInt(parts[5].trim());
        int port = p1 * 256 + p2;
        
        assertTrue(port > 0 && port <= 65535, "Port should be in valid range");
        
        // Verify port matches actual server socket
        int actualPort = session.getPassiveServer().getServerSocket().getLocalPort();
        assertEquals(actualPort, port, "Response port should match actual port");
    }

    @Test
    void testPasvMultipleCalls() throws Exception {
        session.login("testuser");
        
        for (int i = 0; i < 3; i++) {
            stringWriter.getBuffer().setLength(0);
            pasvCommand.execute(null, session, printWriter);
            
            String output = stringWriter.toString();
            assertTrue(output.contains("227"), "Each call should return 227");
            assertNotNull(session.getPassiveServer(), "Passive server should be set");
        }
    }
}
