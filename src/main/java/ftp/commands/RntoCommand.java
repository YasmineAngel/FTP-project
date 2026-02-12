package ftp.commands;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.exceptions.SequenceException;
import ftp.exceptions.SyntaxException;
import ftp.exceptions.FileSystemException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class RntoCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(RntoCommand.class);

    private final PathUtils pathUtils;

    public RntoCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.info("C -> S: RNTO {}", args);

        if (!session.isLoggedIn()) {
            logger.info("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        String fromPathStr = session.getRenameFrom();
        if (fromPathStr == null) {
            logger.info("S -> C: 503 Bad sequence of commands.");
            throw new SequenceException("Bad sequence of commands.");
        }

        if (args == null || args.isBlank()) {
            logger.info("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path from = Paths.get(fromPathStr);
        Path to = pathUtils.resolve(session.getCurrentDirectory(), args);

        try {

            if (to == null) {
                logger.info("S -> C: 550 Access denied.");
                throw new FileSystemException("Access denied.", 550);
            }

            if (Files.exists(to)) {
                logger.info("S -> C: 550 Target already exists.");
                throw new FileSystemException("Target already exists.", 550);
            }

            Files.move(from, to);

            String response = "250 Rename successful.";
            out.println(response);
            logger.info("S -> C: {}", response);

        } catch (IOException e) {

            logger.error("Rename failed from {} to {}: {}", from, to, e.getMessage());
            throw new FileSystemException("Rename operation failed.", 550);

        } finally {
            session.clearRenameFrom();
        }
    }
}
