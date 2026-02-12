package ftp.commands;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;

import ftp.model.UserSession;

public class PortCommandTest {

    private PortCommand portCommand;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        portCommand = new PortCommand();
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testPortNotLoggedIn() throws Exception {
        portCommand.execute("192,168,1,100,200,50", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("530"), "Should return 530 when not logged in");
    }

    @Test
    void testPortSuccess() throws Exception {
        session.login("testuser");
        
        portCommand.execute("192,168,1,100,200,50", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("200"), "Should return 200 PORT command successful");
        assertTrue(output.contains("PORT command successful"), "Should contain success message");
        
        // Verify PORT mode is set
        assertTrue(session.hasPortMode(), "PORT mode should be set");
        assertNotNull(session.getPortModeAddress(), "PORT address should be set");
        assertEquals(200 * 256 + 50, session.getPortMode(), "PORT should be 51250");
    }

    @Test
    void testPortInvalidFormat() throws Exception {
        session.login("testuser");
        
        // Too few parameters
        portCommand.execute("192,168,1", session, printWriter);
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for invalid format");
        
        stringWriter.getBuffer().setLength(0);
        
        // Too many parameters
        portCommand.execute("192,168,1,100,200,50,60", session, printWriter);
        output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for invalid format");
    }

    @Test
    void testPortInvalidIP() throws Exception {
        session.login("testuser");
        
        // Invalid IP octet (>255)
        portCommand.execute("192,168,1,300,200,50", session, printWriter);
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for invalid IP");
        
        stringWriter.getBuffer().setLength(0);
        
        // Negative IP octet
        portCommand.execute("192,168,-1,100,200,50", session, printWriter);
        output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for invalid IP");
    }

    @Test
    void testPortInvalidPort() throws Exception {
        session.login("testuser");
        
        // Invalid port (>255)
        portCommand.execute("192,168,1,100,300,50", session, printWriter);
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for invalid port");
        
        stringWriter.getBuffer().setLength(0);
        
        // Port 0 is invalid (reserved)
        portCommand.execute("192,168,1,100,0,0", session, printWriter);
        output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for port 0");
    }

    @Test
    void testPortParsing() throws Exception {
        session.login("testuser");
        
        // Test port calculation: p1 * 256 + p2
        portCommand.execute("127,0,0,1,20,50", session, printWriter);
        assertEquals(20 * 256 + 50, session.getPortMode(), "Port should be calculated correctly");
        
        stringWriter.getBuffer().setLength(0);
        session.clearPortMode();
        
        // Test edge cases
        portCommand.execute("0,0,0,0,0,1", session, printWriter);
        assertEquals(1, session.getPortMode(), "Port should be 1");
        
        stringWriter.getBuffer().setLength(0);
        session.clearPortMode();
        
        portCommand.execute("255,255,255,255,255,254", session, printWriter);
        assertEquals(255 * 256 + 254, session.getPortMode(), "Port should be 65534");
    }

    @Test
    void testPortClearsPassiveServer() throws Exception {
        session.login("testuser");
        
        // Set PASV first
        PasvCommand pasvCommand = new PasvCommand();
        pasvCommand.execute(null, session, printWriter);
        assertNotNull(session.getPassiveServer(), "PASV server should be set");
        
        stringWriter.getBuffer().setLength(0);
        
        // Set PORT - should clear PASV
        portCommand.execute("192,168,1,100,200,50", session, printWriter);
        assertNull(session.getPassiveServer(), "PASV server should be cleared");
        assertTrue(session.hasPortMode(), "PORT mode should be set");
    }

    @Test
    void testPortReplacesExistingPort() throws Exception {
        session.login("testuser");
        
        portCommand.execute("192,168,1,100,200,50", session, printWriter);
        InetAddress firstAddress = session.getPortModeAddress();
        int firstPort = session.getPortMode();
        
        stringWriter.getBuffer().setLength(0);
        
        portCommand.execute("10,0,0,1,100,200", session, printWriter);
        InetAddress secondAddress = session.getPortModeAddress();
        int secondPort = session.getPortMode();
        
        assertNotEquals(firstAddress, secondAddress, "Should replace with new address");
        assertNotEquals(firstPort, secondPort, "Should replace with new port");
    }

    @Test
    void testPortEmptyArgs() throws Exception {
        session.login("testuser");
        
        portCommand.execute(null, session, printWriter);
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for empty args");
        
        stringWriter.getBuffer().setLength(0);
        
        portCommand.execute("", session, printWriter);
        output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for empty string");
    }
}
