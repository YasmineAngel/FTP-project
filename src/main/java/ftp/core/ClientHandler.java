package ftp.core;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    public ClientHandler(Socket socket, String rootPath) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // PROTOCOL RULE: The server must speak first with a 220 code
            // We use \r\n as required by FTP RFC standards
            out.print("220 Welcome to our Team FTP Server\r\n");
            out.flush(); // Ensure the message is sent immediately

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client sent: " + inputLine);
                // For Milestone 1, we just acknowledge receipt
                out.print("502 Command not implemented yet\r\n");
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        }
    }
}