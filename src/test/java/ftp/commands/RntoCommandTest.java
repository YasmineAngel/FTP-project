package ftp.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ftp.exceptions.AuthException;
import ftp.exceptions.FileSystemException;
import ftp.exceptions.SequenceException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

class RntoCommandTest {

    private UserSession session;
    private PathUtils pathUtils;
    private RntoCommand command;
    private StringWriter stringWriter;
    private PrintWriter out;

    @BeforeEach
    void setUp() {
        session = new UserSession();
        pathUtils = new PathUtils(System.getProperty("java.io.tmpdir"));
        command = new RntoCommand(pathUtils);
        session.setCurrentDirectory("/");
        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
    }

    @Test
    void shouldThrowAuthException_whenNotLoggedIn() {
        AuthException ex = assertThrows(AuthException.class, () -> {
            command.execute("newName.txt", session, out);
        });
        assertEquals("Not logged in.", ex.getMessage());
    }

    @Test
    void shouldThrowSequenceException_whenNoRnfr() {
        session.login("user");

        SequenceException ex = assertThrows(SequenceException.class, () ->
                command.execute("newName.txt", session, out)
        );
        assertEquals("Bad sequence of commands.", ex.getMessage());
    }

    @Test
    void shouldThrowSyntaxException_whenArgsIsNullOrBlank() {
        session.login("user");
        session.setRenameFrom("someFile.txt");

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
    void shouldThrowFileSystemException_whenTargetAlreadyExists() throws Exception {
        session.login("user");
        Path root = pathUtils.getRootDir();
        Path tempFile1 = Files.createTempFile(root, "rnto1", ".txt");
        Path tempFile2 = Files.createTempFile(root, "rnto2", ".txt");

        Path fromResolved = pathUtils.resolve("/", tempFile1.getFileName().toString());
        session.setRenameFrom(fromResolved.toAbsolutePath().toString());

        FileSystemException ex = assertThrows(FileSystemException.class, () ->
                command.execute(tempFile2.getFileName().toString(), session, out)
        );
        assertEquals("Target already exists.", ex.getMessage());
    }

    @Test
    void shouldRenameFileSuccessfully() throws Exception {
        session.login("user");
        Path root = pathUtils.getRootDir();
        Path oldFile = Files.createTempFile(root, "rntoOld", ".txt");
        String newFileName = "rntoNewFile_" + System.nanoTime() + ".txt";
        Path newFilePath = root.resolve(newFileName);

        Path fromResolved = pathUtils.resolve("/", oldFile.getFileName().toString());
        session.setRenameFrom(fromResolved.toAbsolutePath().toString());

        command.execute(newFileName, session, out);

        assertTrue(Files.exists(newFilePath));
        assertFalse(Files.exists(oldFile));
        assertNull(session.getRenameFrom());
        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("250 Rename successful."));
    }

    @Test
    void shouldRenameDirectorySuccessfully() throws Exception {
        session.login("user");
        Path root = pathUtils.getRootDir();
        Path oldDir = Files.createTempDirectory(root, "rntoOldDir");
        String newDirName = "rntoNewDir_" + System.nanoTime();
        Path newDirPath = root.resolve(newDirName);

        Path fromResolved = pathUtils.resolve("/", oldDir.getFileName().toString());
        session.setRenameFrom(fromResolved.toAbsolutePath().toString());

        command.execute(newDirName, session, out);

        assertTrue(Files.exists(newDirPath));
        assertFalse(Files.exists(oldDir));
        assertNull(session.getRenameFrom());
        out.flush();
        String response = stringWriter.toString();
        assertTrue(response.contains("250 Rename successful."));
    }
}
