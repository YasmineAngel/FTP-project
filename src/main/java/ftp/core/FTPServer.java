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
	private static final Logger logger = LogManager.getLogger(FTPServer.class);

	private final String rootPath;
	// Handles multiple clients concurrently
	private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

	public FTPServer(int port, String rootPath) {
		this.port = port;
		this.rootPath = rootPath;
	}

	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			logger.info("FTP Server is running on port {}", port);

			while (true) {

				Socket clientSocket = serverSocket.accept();
				logger.info("Client connected from {}", clientSocket.getRemoteSocketAddress());

				threadPool.execute(new ClientHandler(clientSocket, rootPath));
			}
		} catch (IOException e) {
			logger.error("Could not start server", e);

		} finally {
			threadPool.shutdown();
		}
	}
}