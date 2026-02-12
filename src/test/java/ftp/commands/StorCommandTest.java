package ftp.commands;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import ftp.data.PassiveServer;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class StorCommandTest {

    @TempDir
    Path tempDir;

    private StorCommand storCommand;
    private PathUtils pathUtils;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        pathUtils = new PathUtils(tempDir.toString());
        storCommand = new StorCommand(pathUtils);
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testStorNotLoggedIn() throws Exception {
        storCommand.execute("testfile.txt", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("530"), "Should return 530 when not logged in");
    }

    @Test
    void testStorNoPassiveServer() throws Exception {
        session.login("testuser");
        
        storCommand.execute("testfile.txt", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("425"), "Should return 425 when no passive server");
    }

    @Test
    void testStorNoArguments() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        storCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for missing arguments");
    }

    @Test
    void testStorEmptyArguments() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        storCommand.execute("", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for empty arguments");
    }

    @Test
    void testStorInvalidPath() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        // Try path traversal
        storCommand.execute("../../etc/passwd", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("550"), "Should return 550 for invalid path");
    }

    @Test
    void testStorSuccess() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        Path targetFile = tempDir.resolve("uploaded.txt");
        String testData = "Test file content";
        
        ServerSocket serverSocket = passiveServer.getServerSocket();
        int port = serverSocket.getLocalPort();
        
        // Start command execution in a thread (it will block on accept)
        Thread commandThread = new Thread(() -> {
            try {
                storCommand.execute("uploaded.txt", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and send file data
        try (Socket dataSocket = new Socket("localhost", port)) {
            try (BufferedOutputStream bos = new BufferedOutputStream(dataSocket.getOutputStream())) {
                bos.write(testData.getBytes());
                bos.flush();
            }
            // Close output stream to signal EOF
            
            // Wait for command to complete
            commandThread.join(2000);
            
            String output = stringWriter.toString();
            assertTrue(output.contains("150"), "Should send 150 opening data connection");
            assertTrue(output.contains("226"), "Should send 226 transfer complete");
            assertTrue(output.contains("uploaded.txt"), "Should mention filename in response");
            
            // Verify file was created
            assertTrue(Files.exists(targetFile), "File should be created");
            String content = Files.readString(targetFile);
            assertEquals(testData, content, "File content should match");
        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    @Test
    void testStorOverwritesExistingFile() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        Path targetFile = tempDir.resolve("existing.txt");
        Files.write(targetFile, "Old content".getBytes());
        String newData = "New content";
        
        ServerSocket serverSocket = passiveServer.getServerSocket();
        int port = serverSocket.getLocalPort();
        
        // Start command execution in a thread (it will block on accept)
        Thread commandThread = new Thread(() -> {
            try {
                storCommand.execute("existing.txt", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and send file data
        try (Socket dataSocket = new Socket("localhost", port)) {
            try (BufferedOutputStream bos = new BufferedOutputStream(dataSocket.getOutputStream())) {
                bos.write(newData.getBytes());
                bos.flush();
            }
            // Close output stream to signal EOF
            
            // Wait for command to complete
            commandThread.join(2000);
            
            String content = Files.readString(targetFile);
            assertEquals(newData, content, "File should be overwritten");
        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    @Test
    void testStorWithAsciiMode() throws Exception {
        session.login("testuser");
        session.setTransferType(UserSession.TransferType.ASCII);
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        ServerSocket serverSocket = passiveServer.getServerSocket();
        int port = serverSocket.getLocalPort();
        
        // Start command execution in a thread (it will block on accept)
        Thread commandThread = new Thread(() -> {
            try {
                storCommand.execute("test.txt", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and send file data
        try (Socket dataSocket = new Socket("localhost", port)) {
            try (BufferedOutputStream bos = new BufferedOutputStream(dataSocket.getOutputStream())) {
                bos.write("Test".getBytes());
                bos.flush();
            }
            // Close output stream to signal EOF
            
            // Wait for command to complete
            commandThread.join(2000);
            
            String output = stringWriter.toString();
            assertTrue(output.contains("ASCII"), "Should mention ASCII mode");
        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    @Test
    void testStorWithBinaryMode() throws Exception {
        session.login("testuser");
        session.setTransferType(UserSession.TransferType.BINARY);
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        ServerSocket serverSocket = passiveServer.getServerSocket();
        int port = serverSocket.getLocalPort();
        
        // Start command execution in a thread (it will block on accept)
        Thread commandThread = new Thread(() -> {
            try {
                storCommand.execute("test.txt", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and send file data
        try (Socket dataSocket = new Socket("localhost", port)) {
            try (BufferedOutputStream bos = new BufferedOutputStream(dataSocket.getOutputStream())) {
                bos.write("Test".getBytes());
                bos.flush();
            }
            // Close output stream to signal EOF
            
            // Wait for command to complete
            commandThread.join(2000);
            
            String output = stringWriter.toString();
            assertTrue(output.contains("BINARY"), "Should mention BINARY mode");
        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }
}
