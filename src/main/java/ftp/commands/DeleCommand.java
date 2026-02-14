package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.exceptions.FileSystemException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;
import ftp.utils.FileLockManager;
import ftp.utils.PathUtils;

public class DeleCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(DeleCommand.class);

    private final PathUtils pathUtils;

    public DeleCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: DELE {}", args);

        FileLockManager lockManager = FileLockManager.getInstance();

        if (!session.isLoggedIn()) {
            logger.error("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        if (args == null || args.isBlank()) {
            logger.error("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path target = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (target == null || !pathUtils.exists(target)) {
            logger.error("S -> C: 550 File not found.");
            throw new FileSystemException("File not found.");
        }

        if (!pathUtils.isFile(target)) {
            logger.error("S -> C: 550 Not a regular file.");
            throw new FileSystemException("Not a regular file.");
        }

        lockManager.acquireWriteLock(target);

        try {
            Files.delete(target);

            FtpResponse response = new FtpResponse(
                FtpResponseCode.FILE_ACTION_OK, 
                "File deleted successfully."
            );
            out.print(response.serialize());
            out.flush();
            logger.debug("S -> C: {}", response.serialize().trim());

        } catch (IOException e) {
            logger.error("Delete failed for {}: {}", target, e.getMessage());

            throw new FileSystemException("Delete operation failed.");
        } finally {
            lockManager.releaseWriteLock(target);
        }
    }
}
