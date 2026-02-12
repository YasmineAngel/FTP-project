package ftp.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.net.ServerSocket;
import java.net.Socket;

import ftp.exceptions.ConnectionException;

public class PassiveServerTest {

    private PassiveServer passiveServer;

    @BeforeEach
    void setUp() {
        passiveServer = new PassiveServer();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (passiveServer != null && passiveServer.getServerSocket() != null) {
            try {
                passiveServer.close();
            } catch (ConnectionException e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    void testOpenRandomPort() throws ConnectionException {
        int port = passiveServer.openRandomPort();
        
        assertTrue(port > 0, "Port should be greater than 0");
        assertTrue(port <= 65535, "Port should be less than or equal to 65535");
        assertNotNull(passiveServer.getServerSocket(), "ServerSocket should not be null");
        assertFalse(passiveServer.getServerSocket().isClosed(), "ServerSocket should not be closed");
    }

    @Test
    void testOpenRandomPortReturnsValidPort() throws ConnectionException {
        int port1 = passiveServer.openRandomPort();
        assertTrue(port1 > 0 && port1 <= 65535);
        
        PassiveServer passiveServer2 = new PassiveServer();
        int port2 = passiveServer2.openRandomPort();
        assertTrue(port2 > 0 && port2 <= 65535);
        
        // Ports should be different (very unlikely to be the same)
        // Note: This is probabilistic, but extremely unlikely to fail
        passiveServer2.close();
    }

    @Test
    void testGetServerSocket() throws ConnectionException {
        assertNull(passiveServer.getServerSocket(), "ServerSocket should be null before opening port");
        
        passiveServer.openRandomPort();
        assertNotNull(passiveServer.getServerSocket(), "ServerSocket should not be null after opening port");
    }

    @Test
    void testClose() throws Exception {
        passiveServer.openRandomPort();
        ServerSocket serverSocket = passiveServer.getServerSocket();
        
        assertFalse(serverSocket.isClosed(), "ServerSocket should not be closed before close()");
        
        passiveServer.close();
        
        assertTrue(serverSocket.isClosed(), "ServerSocket should be closed after close()");
    }

    @Test
    void testCloseWhenNull() throws ConnectionException {
        // Should not throw exception when serverSocket is null
        assertDoesNotThrow(() -> passiveServer.close());
    }

    @Test
    void testCloseWhenAlreadyClosed() throws Exception {
        passiveServer.openRandomPort();
        passiveServer.close();
        
        // Should not throw exception when already closed
        assertDoesNotThrow(() -> passiveServer.close());
    }

    @Test
    void testServerSocketAcceptsConnections() throws Exception {
        passiveServer.openRandomPort();
        int port = passiveServer.getServerSocket().getLocalPort();
        
        // Start accepting in a separate thread
        Thread acceptThread = new Thread(() -> {
            try {
                Socket clientSocket = passiveServer.getServerSocket().accept();
                clientSocket.close();
            } catch (Exception e) {
                // Ignore
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
        
        // Give it a moment to start
        Thread.sleep(100);
        
        // Connect to the server
        try (Socket client = new Socket("localhost", port)) {
            assertTrue(client.isConnected(), "Client should be able to connect");
        }
        
        acceptThread.join(1000);
    }

    @Test
    void testMultipleOpenCloseCycles() throws Exception {
        for (int i = 0; i < 5; i++) {
            passiveServer.openRandomPort();
            assertNotNull(passiveServer.getServerSocket());
            assertFalse(passiveServer.getServerSocket().isClosed());
            
            passiveServer.close();
            assertTrue(passiveServer.getServerSocket().isClosed());
        }
    }
}
