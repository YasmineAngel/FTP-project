package ftp.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ftp.exceptions.AuthException;
import ftp.exceptions.FileSystemException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

class RmdCommandTest {

    private UserSession session;
    private PathUtils pathUtils;
    private RmdCommand command;
    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        session = new UserSession();
        pathUtils = new PathUtils(System.getProperty("java.io.tmpdir")); // temp folder as root
        command = new RmdCommand(pathUtils);

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        AuthException ex = assertThrows(AuthException.class, () ->
                command.execute("anyDir", session, out)
        );
        assertEquals("Not logged in.", ex.getMessage());
    }

    @Test
    void shouldThrowSyntaxException_whenArgsIsNullOrBlank() {
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
    void shouldThrowFileSystemException_whenDirectoryNotFound() {
        session.login("user");

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute("missingDir", session, out)
        );
        assertEquals("Directory not found.", ex.getMessage());
    }

    @Test
    void shouldThrowFileSystemException_whenTargetIsNotDirectory() throws Exception {
        session.login("user");

        // Create a temp file
        Path tempFile = Files.createTempFile("testFile", ".txt");
        tempFile.toFile().deleteOnExit();

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute(tempFile.toAbsolutePath().toString(), session, out)
        );
        assertEquals("Not a directory.", ex.getMessage());
    }

    @Test
    void shouldRemoveEmptyDirectorySuccessfully() throws Exception {
        session.login("user");

        // Create a temporary directory
        Path tempDir = Files.createTempDirectory("testDir");
        tempDir.toFile().deleteOnExit();

        command.execute(tempDir.toAbsolutePath().toString(), session, out);

        assertFalse(Files.exists(tempDir));

        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("250 Directory removed successfully."));
    }

    @Test
    void shouldThrowFileSystemException_whenDirectoryNotEmpty() throws Exception {
        session.login("user");

        Path tempDir = Files.createTempDirectory("testDir");
        Path tempFile = Files.createTempFile(tempDir, "inner", ".txt");
        tempDir.toFile().deleteOnExit();
        tempFile.toFile().deleteOnExit();

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute(tempDir.toAbsolutePath().toString(), session, out)
        );
        assertEquals("Remove directory operation failed.", ex.getMessage());
    }
}
