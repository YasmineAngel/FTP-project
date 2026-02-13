package ftp.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ftp.exceptions.AuthException;
import ftp.exceptions.FileSystemException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

class MkdCommandTest {

    private UserSession session;
    private PathUtils pathUtils;
    private MkdCommand command;
    private StringWriter stringWriter;
    private PrintWriter out;
    private Path tempRoot;

    @BeforeEach
    void setUp() throws Exception {
        session = new UserSession();
        tempRoot = Files.createTempDirectory("ftp-test"); // temporary root
        tempRoot.toFile().deleteOnExit();

        pathUtils = new PathUtils(tempRoot.toString());
        command = new MkdCommand(pathUtils);

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        AuthException ex = assertThrows(AuthException.class, () ->
                command.execute("newDir", session, out)
        );
        assertEquals("Not logged in.", ex.getMessage());
    }

    @Test
    void shouldThrowSyntaxException_whenArgsIsNull() throws Exception {
        session.login("user");

        SyntaxException ex1 = assertThrows(SyntaxException.class, () ->
                command.execute(null, session, out)
        );
        assertEquals("Syntax error in parameters.", ex1.getMessage());

        SyntaxException ex2 = assertThrows(SyntaxException.class, () ->
                command.execute("   ", session, out)
        );
        assertEquals("Syntax error in parameters.", ex2.getMessage());
    }

    @Test
    void shouldCreateDirectorySuccessfully() throws Exception {
        session.login("user");

        String dirName = "newFolder";
        Path targetPath = tempRoot.resolve(dirName);

        command.execute(dirName, session, out);

        // Directory should exist
        assertTrue(Files.exists(targetPath) && Files.isDirectory(targetPath));

        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("257 \"" + pathUtils.toFtpPath(targetPath) + "\" directory created."));
    }

    @Test
    void shouldThrowFileSystemException_whenDirectoryAlreadyExists() throws Exception {
        session.login("user");

        String dirName = "existingFolder";
        Path existingPath = tempRoot.resolve(dirName);
        Files.createDirectory(existingPath);

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute(dirName, session, out)
        );
        assertEquals("Directory already exists.", ex.getMessage());
    }

    @Test
    void shouldThrowFileSystemException_whenAccessDenied() throws Exception {
        session.login("user");

        // Try creating a directory outside of the root
        String invalidDir = "../forbidden";
        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute(invalidDir, session, out)
        );
        assertEquals("Access denied.", ex.getMessage());
    }
}
