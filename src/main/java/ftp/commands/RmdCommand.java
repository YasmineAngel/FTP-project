package ftp.commands;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.exceptions.SyntaxException;
import ftp.exceptions.FileSystemException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RmdCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(RmdCommand.class);

    private final PathUtils pathUtils;

    public RmdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: RMD {}", args);

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
            logger.error("S -> C: 550 Directory not found.");
            throw new FileSystemException("Directory not found.", 550);
        }

        if (!pathUtils.isDirectory(target)) {
            logger.error("S -> C: 550 Not a directory.");
            throw new FileSystemException("Not a directory.", 550);
        }

        try {

            Files.delete(target); // fails if not empty

            String response = "250 Directory removed successfully.";
            out.println(response);

            logger.debug("S -> C: {}", response);

        } catch (IOException e) {

            logger.error("Failed to remove directory {}: {}", target, e.getMessage());

            throw new FileSystemException("Remove directory operation failed.", 550);
        }
    }
}
