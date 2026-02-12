package ftp.commands;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.exceptions.SyntaxException;
import ftp.exceptions.FileSystemException;
import ftp.model.UserSession;
import ftp.utils.FileLockManager;
import ftp.utils.PathUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeleCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(DeleCommand.class);

    private final PathUtils pathUtils;

    public DeleCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.info("C -> S: DELE {}", args);

        FileLockManager lockManager = FileLockManager.getInstance();

        if (!session.isLoggedIn()) {
            logger.info("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        if (args == null || args.isBlank()) {
            logger.info("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path target = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (target == null || !pathUtils.exists(target)) {
            logger.info("S -> C: 550 File not found.");
            throw new FileSystemException("File not found.", 550);
        }

        if (!pathUtils.isFile(target)) {
            logger.info("S -> C: 550 Not a regular file.");
            throw new FileSystemException("Not a regular file.", 550);
        }

        lockManager.acquireWriteLock(target);

        try {
            Files.delete(target);

            String response = "250 File deleted successfully.";
            out.println(response);
            logger.info("S -> C: {}", response);

        } catch (IOException e) {

            logger.error("Delete failed for {}: {}", target, e.getMessage());

            throw new FileSystemException("Delete operation failed.", 550);

        } finally {
            lockManager.releaseWriteLock(target);
        }
    }
}
