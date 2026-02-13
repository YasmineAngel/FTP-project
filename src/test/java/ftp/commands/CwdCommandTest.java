package ftp.commands;

import ftp.exceptions.AuthException;
import ftp.exceptions.FileSystemException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CwdCommandTest {

    private PathUtils pathUtils;
    private UserSession session;
    private CwdCommand command;

    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        pathUtils = new PathUtils(System.getProperty("java.io.tmpdir") + "/ftp-root");

        session = new UserSession();
        command = new CwdCommand(pathUtils);

        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter, true);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        session.logout();

        AuthException ex = assertThrows(AuthException.class,
                () -> command.execute("folder", session, out));

        assertEquals("Not logged in.", ex.getMessage());
    }

    @Test
    void shouldThrowSyntaxException_whenArgsNullOrBlank() {
        session.login("user");

        SyntaxException ex1 = assertThrows(SyntaxException.class,
                () -> command.execute(null, session, out));
        assertEquals("Syntax error in parameters.", ex1.getMessage());

        SyntaxException ex2 = assertThrows(SyntaxException.class,
                () -> command.execute("   ", session, out));
        assertEquals("Syntax error in parameters.", ex2.getMessage());
    }

    @Test
    void shouldThrowFileSystemException_whenDirectoryDoesNotExist() {
        session.login("user");

        FileSystemException ex = assertThrows(FileSystemException.class,
                () -> command.execute("nonexistent-folder", session, out));

        assertTrue(ex.getMessage().contains("Access denied") || ex.getMessage().contains("Not a directory"));
    }

    @Test
    void shouldChangeToRelativeDirectory_whenValid() throws Exception {
        session.login("user");

        Path root = Path.of(System.getProperty("java.io.tmpdir") + "/ftp-root");
        Path folder = root.resolve("folder1");
        folder.toFile().mkdirs(); 

        session.setCurrentDirectory("/"); 

        command.execute("folder1", session, out);

        String response = stringWriter.toString().trim();
        assertTrue(response.contains("250 Directory successfully changed to /folder1"));
        assertEquals("/folder1", session.getCurrentDirectory());
    }

    @Test
    void shouldChangeToAbsoluteDirectory_whenValid() throws Exception {
        session.login("user");

        Path root = Path.of(System.getProperty("java.io.tmpdir") + "/ftp-root");
        Path folder = root.resolve("folder2");
        folder.toFile().mkdirs();

        command.execute("/folder2", session, out);

        String response = stringWriter.toString().trim();
        assertTrue(response.contains("250 Directory successfully changed to /folder2"));
        assertEquals("/folder2", session.getCurrentDirectory());
    }
}
