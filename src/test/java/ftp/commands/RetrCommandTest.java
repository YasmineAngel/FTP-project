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

public class RetrCommandTest {

    @TempDir
    Path tempDir;

    private RetrCommand retrCommand;
    private PathUtils pathUtils;
    private UserSession session;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private Path testFile;

    @BeforeEach
    void setUp() throws IOException {
        pathUtils = new PathUtils(tempDir.toString());
        retrCommand = new RetrCommand(pathUtils);
        session = new UserSession();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        testFile = tempDir.resolve("testfile.txt");
        Files.write(testFile, "Hello, World!".getBytes());
    }

    @Test
    void testRetrNotLoggedIn() throws Exception {
        retrCommand.execute("testfile.txt", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("530"), "Should return 530 when not logged in");
    }

    @Test
    void testRetrNoPassiveServer() throws Exception {
        session.login("testuser");
        
        retrCommand.execute("testfile.txt", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("425"), "Should return 425 when no passive server");
    }

    @Test
    void testRetrNoArguments() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        retrCommand.execute(null, session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for missing arguments");
    }

    @Test
    void testRetrEmptyArguments() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        retrCommand.execute("", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("501"), "Should return 501 for empty arguments");
    }

    @Test
    void testRetrFileNotFound() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        retrCommand.execute("nonexistent.txt", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("550"), "Should return 550 for file not found");
    }

    @Test
    void testRetrSuccess() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        ServerSocket serverSocket = passiveServer.getServerSocket();
        int port = serverSocket.getLocalPort();
        byte[] receivedData = new byte[1024];
        int[] bytesReceived = {0};
        
        // Start command execution in a thread (it will block on accept)
        Thread commandThread = new Thread(() -> {
            try {
                retrCommand.execute("testfile.txt", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and receive file
        try (Socket dataSocket = new Socket("localhost", port);
             BufferedInputStream bis = new BufferedInputStream(dataSocket.getInputStream())) {
            
            // Read all data until EOF
            int bytesRead;
            while ((bytesRead = bis.read(receivedData)) != -1) {
                bytesReceived[0] += bytesRead;
            }
            
            // Wait for command to complete
            commandThread.join(2000);
            
            String output = stringWriter.toString();
            assertTrue(output.contains("150"), "Should send 150 opening data connection");
            assertTrue(output.contains("226"), "Should send 226 transfer complete");
            assertTrue(output.contains("testfile.txt"), "Should mention filename in response");
            
            // Verify data was sent
            assertTrue(bytesReceived[0] > 0, "Should receive file data");
        } finally {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    @Test
    void testRetrWithAsciiMode() throws Exception {
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
                retrCommand.execute("testfile.txt", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and receive file
        try (Socket dataSocket = new Socket("localhost", port);
             BufferedInputStream bis = new BufferedInputStream(dataSocket.getInputStream())) {
            
            // Read all data until EOF
            byte[] buffer = new byte[1024];
            while (bis.read(buffer) != -1) {
                // Consume all data
            }
            
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
    void testRetrWithBinaryMode() throws Exception {
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
                retrCommand.execute("testfile.txt", session, printWriter);
            } catch (Exception e) {
                // Ignore
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Give command time to start and block on accept
        Thread.sleep(200);
        
        // Connect to unblock accept and receive file
        try (Socket dataSocket = new Socket("localhost", port);
             BufferedInputStream bis = new BufferedInputStream(dataSocket.getInputStream())) {
            
            // Read all data until EOF
            byte[] buffer = new byte[1024];
            while (bis.read(buffer) != -1) {
                // Consume all data
            }
            
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

    @Test
    void testRetrDirectoryInsteadOfFile() throws Exception {
        session.login("testuser");
        PassiveServer passiveServer = new PassiveServer();
        passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);
        
        Path testDir = tempDir.resolve("testdir");
        Files.createDirectories(testDir);
        
        retrCommand.execute("testdir", session, printWriter);
        
        String output = stringWriter.toString();
        assertTrue(output.contains("550"), "Should return 550 for directory instead of file");
    }
}
