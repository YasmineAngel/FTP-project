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

public class MkdCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(MkdCommand.class);

    private final PathUtils pathUtils;

    public MkdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.info("C -> S: MKD {}", args);

        if (!session.isLoggedIn()) {
            logger.info("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        if (args == null || args.isBlank()) {
            logger.info("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path target = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (target == null) {
            logger.info("S -> C: 550 Access denied.");
            throw new FileSystemException("Access denied.", 550);
        }

        try {

            if (Files.exists(target)) {
                logger.info("S -> C: 550 Directory already exists.");
                throw new FileSystemException("Directory already exists.", 550);
            }

            Files.createDirectories(target);

            String ftpPath = pathUtils.toFtpPath(target);
            String response = "257 \"" + ftpPath + "\" directory created.";

            out.println(response);
            logger.info("S -> C: {}", response);

        } catch (IOException e) {

            logger.error("Failed to create directory {}: {}", target, e.getMessage());

            throw new FileSystemException("Create directory operation failed.", 550);
        }
    }
}
