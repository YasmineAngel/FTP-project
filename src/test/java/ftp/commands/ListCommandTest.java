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

public class ListCommandTest {

    @TempDir
    Path tempDir;

    private ListCommand listCommand;
    private PathUtils pathUtils;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        pathUtils = new PathUtils(tempDir.toString());
        listCommand = new ListCommand(pathUtils);
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testListNotLoggedIn() throws Exception {
        listCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("530"), "Should return 530 when not logged in");
    }

    @Test
    void testListNoPassiveServer() throws Exception {
        session.login("testuser");
        
        listCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("425"), "Should return 425 when no passive server");
    }

    @Test
    void testListSuccessCurrentDirectory() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        // Create test directory with files
        Path testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);
        Files.write(testDir.resolve("file1.txt"), "Content".getBytes());
        
        session.setCurrentDirectory("/testdir");
        
        ServerSocket serverSocket = passiveServer.getServerSocket();
        int port = serverSocket.getLocalPort();
        
        // Start command execution in a thread (it will block on accept)
        Thread commandThread = new Thread(() -> {
            try {
                listCommand.execute(null, session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and read listing
        try (Socket dataSocket = new Socket("localhost", port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(dataSocket.getInputStream()))) {
            
            // Read directory listing
            String line;
            while ((line = reader.readLine()) != null) {
                // Consume listing
            }
            
            // Wait for command to complete
            commandThread.join(2000);
            
            String output = stringWriter.toString();
            assertTrue(output.contains("150"), "Should send 150 opening data connection");
            assertTrue(output.contains("226"), "Should send 226 transfer complete");
        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    @Test
    void testListWithArgument() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        // Create test directory
        Path testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);
        Files.write(testDir.resolve("file.txt"), "Content".getBytes());
        
        ServerSocket serverSocket = passiveServer.getServerSocket();
        int port = serverSocket.getLocalPort();
        
        // Start command execution in a thread (it will block on accept)
        Thread commandThread = new Thread(() -> {
            try {
                listCommand.execute("testdir", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and read listing
        try (Socket dataSocket = new Socket("localhost", port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(dataSocket.getInputStream()))) {
            
            // Read directory listing
            String line;
            while ((line = reader.readLine()) != null) {
                // Consume listing
            }
            
            // Wait for command to complete
            commandThread.join(2000);
            
            String output = stringWriter.toString();
            assertTrue(output.contains("150"), "Should send 150 opening data connection");
            assertTrue(output.contains("226"), "Should send 226 transfer complete");
        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    @Test
    void testListDirectoryNotFound() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        listCommand.execute("nonexistent", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("550"), "Should return 550 for directory not found");
    }

    @Test
    void testListInvalidPath() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        // Try to access path outside root (path traversal)
        listCommand.execute("../../etc", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("550"), "Should return 550 for invalid path");
    }
}
