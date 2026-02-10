package ftp.data;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import ftp.exceptions.ConnectionException;
import ftp.exceptions.StorageException;
import ftp.exceptions.UnavailableFileException;

public class TransferManagerTest {

    @TempDir
    Path tempDir;

    private Path testFile;
    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);
        testFile = testDir.resolve("testfile.txt");
        Files.write(testFile, "Hello, World!".getBytes());
    }

    @Test
    void testSendFileSuccess() throws Exception {
        // Create a server socket and client socket
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                // Read the file content
                try (BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream())) {
                    byte[] buffer = new byte[4096];
                    int bytesRead = bis.read(buffer);
                    assertTrue(bytesRead > 0, "Should receive data");
                }
                clientSocket.close();
            } catch (Exception e) {
                fail("Server thread error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        Thread.sleep(100);
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            TransferManager.sendFile(testFile, dataSocket);
            // If no exception is thrown, test passes
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendFileFileNotFound() throws Exception {
        Path nonExistentFile = testDir.resolve("nonexistent.txt");
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            assertThrows(UnavailableFileException.class, () -> {
                TransferManager.sendFile(nonExistentFile, dataSocket);
            });
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendFileDirectoryInsteadOfFile() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            assertThrows(UnavailableFileException.class, () -> {
                TransferManager.sendFile(testDir, dataSocket);
            });
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testReceiveFileSuccess() throws Exception {
        Path targetFile = testDir.resolve("received.txt");
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                // Send data
                try (BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream())) {
                    byte[] data = "Test data".getBytes();
                    bos.write(data);
                    bos.flush();
                }
                clientSocket.close();
            } catch (Exception e) {
                fail("Server thread error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        Thread.sleep(100);
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            TransferManager.receiveFile(targetFile, dataSocket);
            
            assertTrue(Files.exists(targetFile), "File should be created");
            String content = Files.readString(targetFile);
            assertEquals("Test data", content, "File content should match");
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testReceiveFileParentDirectoryDoesNotExist() throws Exception {
        Path targetFile = testDir.resolve("nonexistent").resolve("file.txt");
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            assertThrows(StorageException.class, () -> {
                TransferManager.receiveFile(targetFile, dataSocket);
            });
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testReceiveFileLargeData() throws Exception {
        Path targetFile = testDir.resolve("largefile.txt");
        
        // Create large data (1MB)
        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream())) {
                    bos.write(largeData);
                    bos.flush();
                }
                clientSocket.close();
            } catch (Exception e) {
                fail("Server thread error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        Thread.sleep(100);
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            TransferManager.receiveFile(targetFile, dataSocket);
            
            assertTrue(Files.exists(targetFile), "File should be created");
            assertEquals(largeData.length, Files.size(targetFile), "File size should match");
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testReceiveFileOverwritesExistingFile() throws Exception {
        Path targetFile = testDir.resolve("existing.txt");
        Files.write(targetFile, "Old content".getBytes());
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream())) {
                    bos.write("New content".getBytes());
                    bos.flush();
                }
                clientSocket.close();
            } catch (Exception e) {
                fail("Server thread error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        Thread.sleep(100);
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            TransferManager.receiveFile(targetFile, dataSocket);
            
            String content = Files.readString(targetFile);
            assertEquals("New content", content, "File should be overwritten");
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendFileConnectionError() throws Exception {
        // Create a socket and immediately close it to simulate connection error
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close(); // Close before connecting
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            // This should fail because server closed
            Thread.sleep(50);
            dataSocket.close();
        } catch (Exception e) {
            // Expected - connection will fail
        }
        
        // Try with a closed socket
        Socket closedSocket = new Socket();
        closedSocket.close();
        
        assertThrows(ConnectionException.class, () -> {
            TransferManager.sendFile(testFile, closedSocket);
        });
    }

    @Test
    void testReceiveFileConnectionError() throws Exception {
        Socket closedSocket = new Socket();
        closedSocket.close();
        
        Path targetFile = testDir.resolve("test.txt");
        
        assertThrows(ConnectionException.class, () -> {
            TransferManager.receiveFile(targetFile, closedSocket);
        });
    }

    @Test
    void testSendFileEmptyFile() throws Exception {
        Path emptyFile = testDir.resolve("empty.txt");
        Files.createFile(emptyFile);
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream())) {
                    int bytesRead = bis.read();
                    assertEquals(-1, bytesRead, "Should read EOF for empty file");
                }
                clientSocket.close();
            } catch (Exception e) {
                fail("Server thread error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        Thread.sleep(100);
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            TransferManager.sendFile(emptyFile, dataSocket);
            // Should complete without error
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testReceiveFileEmptyData() throws Exception {
        Path targetFile = testDir.resolve("empty.txt");
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                // Send nothing (close immediately)
                clientSocket.close();
            } catch (Exception e) {
                fail("Server thread error: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        Thread.sleep(100);
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            TransferManager.receiveFile(targetFile, dataSocket);
            
            assertTrue(Files.exists(targetFile), "File should be created");
            assertEquals(0, Files.size(targetFile), "File should be empty");
        } finally {
            serverSocket.close();
        }
    }
}
