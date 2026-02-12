package ftp.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.ConnectionException;
import ftp.exceptions.StorageException;
import ftp.exceptions.UnavailableFileException;

public class TransferManager {

    private static final Logger logger = LogManager.getLogger(TransferManager.class);

    public static void receiveFile(Path filePath, Socket dataSocket) throws StorageException, ConnectionException {
        receiveFile(filePath, dataSocket, 0);
    }

    public static void receiveFile(Path filePath, Socket dataSocket, long restartMarker) throws StorageException, ConnectionException {
        logger.info("Receiving file: {} (restart marker: {})", filePath, restartMarker);
        
        // Check if parent directory exists and is writable
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            throw new StorageException("Parent directory does not exist");
        }
        
        boolean append = restartMarker > 0 && Files.exists(filePath);
        
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath.toFile(), append));
             BufferedInputStream bis = new BufferedInputStream(dataSocket.getInputStream())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            bos.flush();
            logger.info("File received successfully: {} ({} bytes, appended: {})", filePath, totalBytes, append);
        } catch (FileNotFoundException e) {
            throw new StorageException("Cannot create file: " + e.getMessage());
        } catch (IOException e) {
            // Check if it's a socket error (connection) or file write error (storage)
            if (e.getMessage() != null && (e.getMessage().contains("Connection") || e.getMessage().contains("Socket"))) {
                throw new ConnectionException("Data connection error: " + e.getMessage());
            } else {
                throw new StorageException("Error writing file: " + e.getMessage());
            }
        }
    }

    public static void sendFile(Path filePath, Socket dataSocket) throws UnavailableFileException, ConnectionException {
        sendFile(filePath, dataSocket, 0);
    }

    public static void sendFile(Path filePath, Socket dataSocket, long restartMarker) throws UnavailableFileException, ConnectionException {
        logger.info("Sending file: {} (restart marker: {})", filePath, restartMarker);
        
        // Check if file exists and is readable
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new UnavailableFileException("File not found");
        }

        long fileSize = 0;
        try {
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            throw new UnavailableFileException("Cannot determine file size: " + e.getMessage());
        }

        if (restartMarker > fileSize) {
            throw new UnavailableFileException("Restart marker exceeds file size");
        }
        
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis);
             BufferedOutputStream bos = new BufferedOutputStream(dataSocket.getOutputStream())) {

            // Skip to restart marker if specified
            if (restartMarker > 0) {
                long skipped = fis.skip(restartMarker);
                if (skipped != restartMarker) {
                    throw new UnavailableFileException("Cannot skip to restart marker");
                }
                logger.info("Skipped {} bytes to restart marker", restartMarker);
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = restartMarker;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            bos.flush();
            logger.info("File sent successfully: {} ({} bytes total, {} bytes sent)", 
                filePath, fileSize, totalBytes - restartMarker);
        } catch (FileNotFoundException e) {
            throw new UnavailableFileException("File not found");
        } catch (IOException e) {
            // Check if it's a socket error (connection) or file read error (unavailable)
            if (e.getMessage() != null && (e.getMessage().contains("Connection") || e.getMessage().contains("Socket"))) {
                throw new ConnectionException("Data connection error: " + e.getMessage());
            } else {
                throw new UnavailableFileException("Error reading file: " + e.getMessage());
            }
        }
    }
}