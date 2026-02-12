package ftp.data;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.ConnectionException;

public class PassiveServer {

    private static final Logger logger = LogManager.getLogger(PassiveServer.class);
    private ServerSocket serverSocket;

    public int openRandomPort() throws ConnectionException {
        try {
            serverSocket = new ServerSocket(0); // 0 = OS picks a free port
            int port = serverSocket.getLocalPort();
            logger.info("Passive data connection opened on port: {}", port);
            return port;
        } catch (IOException e) {
            throw new ConnectionException("Cannot open passive data connection: " + e.getMessage());
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void close() throws ConnectionException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                logger.info("Closing passive data connection on port: {}", serverSocket.getLocalPort());
                serverSocket.close();
            } catch (IOException e) {
                throw new ConnectionException("Error closing data connection: " + e.getMessage());
            }
        }
    }
}