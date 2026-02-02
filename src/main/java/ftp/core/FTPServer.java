package ftp.core;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTPServer {
    private final int port;
    private static final Logger logger =LogManager.getLogger(FTPServer.class);

    private final String rootPath;
    // Creates a pool of 10 threads to handle multiple clients simultaneously
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public FTPServer(int port, String rootPath) {
        this.port = port;
        this.rootPath = rootPath;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("FTP Server is running on port {}", port);



            while (true) {
                // Server pauses here until a client connects
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected from {}", clientSocket.getRemoteSocketAddress());


                // Hand the connection to a worker thread from the pool
                threadPool.execute(new ClientHandler(clientSocket, rootPath));
            }
        } catch (IOException e) {
        	logger.error("Could not start server",e);

        } finally {
            threadPool.shutdown(); // Clean up threads when server stops
        }
    }
}