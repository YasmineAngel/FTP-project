package ftp.data;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PassiveServer {

    private static final Logger logger = LogManager.getLogger(PassiveServer.class);
    private ServerSocket serverSocket;

    public int openRandomPort() throws IOException {
        serverSocket = new ServerSocket(0); // 0 = OS picks a free port
        int port = serverSocket.getLocalPort();
        logger.info("Passive data connection opened on port: {}", port);
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void close() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            logger.info("Closing passive data connection on port: {}", serverSocket.getLocalPort());
            serverSocket.close();
        }
    }
}