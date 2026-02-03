package ftp.core;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.net.Socket;

import ftp.model.UserSession;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final String rootPath;
    private final CommandExecutor commandExecutor; 
    private static final Logger logger =LogManager.getLogger(ClientHandler.class);
    public ClientHandler(Socket socket, String rootPath) {
        this.socket = socket;
        this.rootPath = rootPath;
        this.commandExecutor = new CommandExecutor(rootPath);
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
            UserSession session = new UserSession();

             while (session.isConnected()) {
                String line = in.readLine();
                if (line == null) break;
                logger.info("C -> S: {}", line);
                commandExecutor.execute(line, session, out);
             }

        } catch (IOException e) {
        	logger.error("Client Disconnected",e);
        }finally {
            logger.info("Client session ended.");
        }
    }
}