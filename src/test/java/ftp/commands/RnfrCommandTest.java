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

class RnfrCommandTest {

    private UserSession session;
    private PathUtils pathUtils;
    private RnfrCommand command;
    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        session = new UserSession();
        pathUtils = new PathUtils(System.getProperty("java.io.tmpdir"));
        command = new RnfrCommand(pathUtils);
        session.setCurrentDirectory("/");
        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        AuthException ex = assertThrows(AuthException.class, () ->
                command.execute("anyFile.txt", session, out)
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
    void shouldThrowFileSystemException_whenTargetDoesNotExist() {
        session.login("user");

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute("missingFile.txt", session, out)
        );
        assertEquals("File or directory does not exist.", ex.getMessage());
    }

    @Test
    void shouldSetRenameFrom_whenTargetExists() throws Exception {
        session.login("user");
        Path root = pathUtils.getRootDir();
        Path tempFile = Files.createTempFile(root, "rnfrFile", ".txt");
        tempFile.toFile().deleteOnExit();
        command.execute(tempFile.getFileName().toString(), session, out);


        Path expectedResolved = pathUtils.resolve("/", tempFile.getFileName().toString());
        assertEquals(expectedResolved.toAbsolutePath().toString(), session.getRenameFrom());

        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("350 Ready for RNTO."));
    }

    @Test
    void shouldWorkForDirectory() throws Exception {
        session.login("user");
        Path root = pathUtils.getRootDir();
        Path tempDir = Files.createTempDirectory(root, "rnfrDir");

        command.execute(tempDir.getFileName().toString(), session, out);

        Path expectedResolved = pathUtils.resolve("/", tempDir.getFileName().toString());
        assertEquals(expectedResolved.toAbsolutePath().toString(), session.getRenameFrom());

        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("350 Ready for RNTO."));
    }
}
