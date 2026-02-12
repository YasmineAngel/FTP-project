package ftp.commands;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

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

class DeleCommandTest {

    private UserSession session;
    private PathUtils pathUtils;
    private DeleCommand command;
    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        session = new UserSession();
        pathUtils = new PathUtils(System.getProperty("java.io.tmpdir")); // temporary folder
        command = new DeleCommand(pathUtils); // your actual constructor

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        // User session is not logged in by default
        AuthException ex = assertThrows(AuthException.class, () ->
                command.execute("any.txt", session, out)
        );
        assertEquals("Not logged in.", ex.getMessage());
    }

    @Test
    void shouldDeleteFileSuccessfully() throws Exception {
        session.login("user");

        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        command.execute(tempFile.getAbsolutePath(), session, out);

        assertFalse(tempFile.exists(), "File should be deleted");
        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("250 File deleted successfully."));
    }

    @Test
    void shouldThrowFileSystemException_whenFileNotFound() {
        session.login("user");

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute("missing.txt", session, out)
        );
        assertEquals("File not found.", ex.getMessage()); // match actual message
    }

    @Test
    void shouldThrowSyntaxException_whenArgsIsNull() {
        session.login("user");

        SyntaxException ex = assertThrows(SyntaxException.class, () ->
                command.execute(null, session, out)
        );
        assertEquals("Syntax error in parameters.", ex.getMessage());
    }

    @Test
    void shouldThrowFileSystemException_whenTargetIsNotFile() throws Exception {
        session.login("user");

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "testDir");
        tempDir.mkdir();
        tempDir.deleteOnExit();

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute(tempDir.getAbsolutePath(), session, out)
        );
        assertEquals("Not a regular file.", ex.getMessage()); // match actual message
    }
}
