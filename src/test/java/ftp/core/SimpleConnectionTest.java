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
        
        int port;
        try (ServerSocket tempSocket = new ServerSocket(0)) {
            port = tempSocket.getLocalPort();
        }
        
        String root = "./ftp-root";

       
        Thread serverThread = new Thread(() -> {
            try {
                new FTPServer(port, root).start();
            } catch (Exception e) {
             
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true); 
        serverThread.start();

        Thread.sleep(1000);

            
            try (Socket socket = new Socket("localhost", port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            String greeting = in.readLine();
            
            
            assertTrue(greeting != null && greeting.contains("220"), 
                "Server should send 220 greeting code. Got: " + greeting);
        } catch (java.net.ConnectException e) {
       
            throw new AssertionError("Could not connect to server on port " + port + 
                ". Server may have failed to start. Error: " + e.getMessage(), e);
        }
    }
}