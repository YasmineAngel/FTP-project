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
import java.util.ArrayList;
import java.util.List;

import ftp.exceptions.ConnectionException;
import ftp.exceptions.UnavailableFileException;

public class ListManagerTest {

    @TempDir
    Path tempDir;

    private Path testDir;
    private Path subDir;
    private Path testFile1;
    private Path testFile2;

    @BeforeEach
    void setUp() throws IOException {
        testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);
        
        subDir = testDir.resolve("subdir");
        Files.createDirectories(subDir);
        
        testFile1 = testDir.resolve("file1.txt");
        Files.write(testFile1, "Content 1".getBytes());
        
        testFile2 = testDir.resolve("file2.txt");
        Files.write(testFile2, "Content 2".getBytes());
        
        // Create a file in subdirectory
        Path subFile = subDir.resolve("subfile.txt");
        Files.write(subFile, "Sub content".getBytes());
    }

    @Test
    void testSendDirectoryListingSuccess() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        List<String> receivedLines = new ArrayList<>();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedLines.add(line);
                    }
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
            ListManager.sendDirectoryListing(testDir, dataSocket);
            
            // Give it a moment to process
            Thread.sleep(100);
            
            assertFalse(receivedLines.isEmpty(), "Should receive directory listing");
            
            // Verify Unix-style format: permissions, link count, owner, group, size, date, time, filename
            // Format: "-rw-r--r-- 1 owner group          9 Feb 10 15:30 file2.txt"
            for (String line : receivedLines) {
                // Check that line starts with d or - followed by permissions, then has at least 5 fields
                assertTrue(line.matches("^[d-][rwx-]{9}\\s+\\d+\\s+\\S+\\s+\\S+\\s+\\d+.*"), 
                    "Line should match Unix format: " + line);
            }
            
            // Should contain our files and subdirectory
            boolean hasFile1 = receivedLines.stream().anyMatch(l -> l.contains("file1.txt"));
            boolean hasFile2 = receivedLines.stream().anyMatch(l -> l.contains("file2.txt"));
            boolean hasSubDir = receivedLines.stream().anyMatch(l -> l.contains("subdir"));
            
            assertTrue(hasFile1, "Should list 1.txt");
            assertTrue(hasFile2, "Should list file2.txt");
            assertTrue(hasSubDir, "Should list subdir");
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendDirectoryListingNullDirectory() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            assertThrows(UnavailableFileException.class, () -> {
                ListManager.sendDirectoryListing(null, dataSocket);
            });
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendDirectoryListingNonExistentDirectory() throws Exception {
        Path nonExistent = tempDir.resolve("nonexistent");
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            assertThrows(UnavailableFileException.class, () -> {
                ListManager.sendDirectoryListing(nonExistent, dataSocket);
            });
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendDirectoryListingFileInsteadOfDirectory() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        try (Socket dataSocket = new Socket("localhost", port)) {
            assertThrows(UnavailableFileException.class, () -> {
                ListManager.sendDirectoryListing(testFile1, dataSocket);
            });
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendDirectoryListingEmptyDirectory() throws Exception {
        Path emptyDir = testDir.resolve("emptydir");
        Files.createDirectories(emptyDir);
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        List<String> receivedLines = new ArrayList<>();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedLines.add(line);
                    }
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
            ListManager.sendDirectoryListing(emptyDir, dataSocket);
            
            Thread.sleep(100);
            
            // Empty directory should produce empty listing
            assertTrue(receivedLines.isEmpty(), "Empty directory should produce empty listing");
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendDirectoryListingFormat() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        List<String> receivedLines = new ArrayList<>();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedLines.add(line);
                    }
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
            ListManager.sendDirectoryListing(testDir, dataSocket);
            
            Thread.sleep(100);
            
            // Check Unix-style format for each line
            // Format: "-rw-r--r-- 1 owner group          9 Feb 10 15:30 file2.txt"
            for (String line : receivedLines) {
                // Verify Unix-style format: permissions (d or - followed by 9 chars), link count, owner, group, size, ...
                assertTrue(line.matches("^[d-][rwx-]{9}\\s+\\d+\\s+\\S+\\s+\\S+\\s+\\d+.*"), 
                    "Line should match Unix format: " + line);
                
                // Check that directory entries start with 'd'
                if (line.contains("subdir")) {
                    assertTrue(line.startsWith("d"), 
                        "Directory should be marked with 'd': " + line);
                }
                
                // Check that file entries start with '-'
                if (line.contains("file1.txt") || line.contains("file2.txt")) {
                    assertTrue(line.startsWith("-"), 
                        "File should be marked with '-': " + line);
                }
            }
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendDirectoryListingConnectionError() throws Exception {
        Socket closedSocket = new Socket();
        closedSocket.close();
        
        assertThrows(ConnectionException.class, () -> {
            ListManager.sendDirectoryListing(testDir, closedSocket);
        });
    }

    @Test
    void testSendDirectoryListingWithManyFiles() throws Exception {
        // Create many files
        for (int i = 0; i < 50; i++) {
            Path file = testDir.resolve("file" + i + ".txt");
            Files.write(file, ("Content " + i).getBytes());
        }
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        List<String> receivedLines = new ArrayList<>();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedLines.add(line);
                    }
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
            ListManager.sendDirectoryListing(testDir, dataSocket);
            
            Thread.sleep(200);
            
            // Should receive all files plus subdirectory
            assertTrue(receivedLines.size() >= 50, 
                "Should receive listing for all files");
        } finally {
            serverSocket.close();
        }
    }

    @Test
    void testSendDirectoryListingFileSizes() throws Exception {
        // Create files with known sizes
        Path smallFile = testDir.resolve("small.txt");
        Files.write(smallFile, "A".getBytes()); // 1 byte
        
        Path largeFile = testDir.resolve("large.txt");
        byte[] largeData = new byte[10000];
        Files.write(largeFile, largeData); // 10000 bytes
        
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        
        List<String> receivedLines = new ArrayList<>();
        
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedLines.add(line);
                    }
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
            ListManager.sendDirectoryListing(testDir, dataSocket);
            
            Thread.sleep(100);
            
            // Find the lines for our test files
            String smallLine = receivedLines.stream()
                .filter(l -> l.contains("small.txt"))
                .findFirst()
                .orElse(null);
            String largeLine = receivedLines.stream()
                .filter(l -> l.contains("large.txt"))
                .findFirst()
                .orElse(null);
            
            assertNotNull(smallLine, "Should find small.txt in listing");
            assertNotNull(largeLine, "Should find large.txt in listing");
            
            // Extract sizes from Unix format: "-rw-r--r-- 1 owner group          9 Feb 10 15:30 file.txt"
            // Size is the 5th field (index 4) after splitting by whitespace
            long smallSize = extractSize(smallLine);
            long largeSize = extractSize(largeLine);
            
            assertEquals(1, smallSize, "Small file should be 1 byte");
            assertEquals(10000, largeSize, "Large file should be 10000 bytes");
        } finally {
            serverSocket.close();
        }
    }

    private long extractSize(String line) {
        // Unix format: "-rw-r--r-- 1 owner group          9 Feb 10 15:30 file.txt"
        // Split by whitespace: [0]=permissions, [1]=link_count, [2]=owner, [3]=group, [4]=size, [5+]=date/time/filename
        String[] parts = line.trim().split("\\s+");
        if (parts.length >= 5) {
            try {
                // Size is the 5th field (index 4)
                return Long.parseLong(parts[4]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
