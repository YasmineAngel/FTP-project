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
        logger.info("Receiving file: {}", filePath);
        
        // Check if parent directory exists and is writable
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            throw new StorageException("Parent directory does not exist");
        }
        
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
        logger.info("Sending file: {}", filePath);
        
        // Check if file exists and is readable
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new UnavailableFileException("File not found");
        }
        
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