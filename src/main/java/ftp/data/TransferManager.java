package ftp.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransferManager {

    private static final Logger logger = LogManager.getLogger(TransferManager.class);

    public static void receiveFile(Path filePath, Socket dataSocket) throws IOException {
        logger.info("Receiving file: {}", filePath);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()));
             BufferedInputStream bis = new BufferedInputStream(dataSocket.getInputStream())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            bos.flush();
            logger.info("File received successfully: {} ({} bytes)", filePath, totalBytes);
        } catch (IOException e) {
            logger.error("Error receiving file: {}", filePath, e);
            throw e;
        }
    }

    public static void sendFile(Path filePath, Socket dataSocket) throws IOException {
        logger.info("Sending file: {}", filePath);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath.toFile()));
             BufferedOutputStream bos = new BufferedOutputStream(dataSocket.getOutputStream())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            bos.flush();
            logger.info("File sent successfully: {} ({} bytes)", filePath, totalBytes);
        } catch (IOException e) {
            logger.error("Error sending file: {}", filePath, e);
            throw e;
        }
    }
}