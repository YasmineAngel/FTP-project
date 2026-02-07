package ftp.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class SimpleConnectionTest {

    @Test
    void testServerGreetsClient() throws Exception {
        int port = 2126;
        String root = "./ftp-root";

        // 1. Start server in a background thread so it doesn't block the test
        Thread serverThread = new Thread(() -> new FTPServer(port, root).start());
        serverThread.setDaemon(true); // Ensures the thread stops when test finishes
        serverThread.start();

        // 2. Wait a moment for the ServerSocket to open
        Thread.sleep(500);

        // 3. Connect as a client and read the first line
        try (Socket socket = new Socket("localhost", port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            String greeting = in.readLine();
            
            // 4. Verify the server sent the correct RFC-compliant greeting
            assertTrue(greeting.contains("220"), "Server should send 220 greeting code.");
        }
    }
}