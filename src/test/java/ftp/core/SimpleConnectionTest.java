package ftp.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SimpleConnectionTest {

    @Test
    void testServerGreetsClient() throws Exception {
        // Use a random available port to avoid conflicts
        int port;
        try (ServerSocket tempSocket = new ServerSocket(0)) {
            port = tempSocket.getLocalPort();
        }
        
        String root = "./ftp-root";

        // 1. Start server in a background thread so it doesn't block the test
        Thread serverThread = new Thread(() -> {
            try {
                new FTPServer(port, root).start();
            } catch (Exception e) {
                // Server errors are logged, thread will exit
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true); // Ensures the thread stops when test finishes
        serverThread.start();

        // 2. Wait longer for the ServerSocket to open and be ready
        // Give it more time to ensure the server is fully initialized
        Thread.sleep(1000);

        // 3. Connect as a client and read the first line
        try (Socket socket = new Socket("localhost", port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            String greeting = in.readLine();
            
            // 4. Verify the server sent the correct RFC-compliant greeting
            assertTrue(greeting != null && greeting.contains("220"), 
                "Server should send 220 greeting code. Got: " + greeting);
        } catch (java.net.ConnectException e) {
            // If connection is refused, the server likely didn't start
            throw new AssertionError("Could not connect to server on port " + port + 
                ". Server may have failed to start. Error: " + e.getMessage(), e);
        }
    }
}