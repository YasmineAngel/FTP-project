package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.exceptions.FileSystemException;
import ftp.exceptions.SequenceException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;
import ftp.utils.PathUtils;

public class RntoCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(RntoCommand.class);

    private final PathUtils pathUtils;

    public RntoCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: RNTO {}", args);

        if (!session.isLoggedIn()) {
            logger.error("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        String fromPathStr = session.getRenameFrom();
        if (fromPathStr == null) {
            logger.error("S -> C: 503 Bad sequence of commands.");
            throw new SequenceException("Bad sequence of commands.");
        }

        if (args == null || args.isBlank()) {
            logger.error("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path from = Paths.get(fromPathStr);
        Path to = pathUtils.resolve(session.getCurrentDirectory(), args);

        try {

            if (to == null) {
                logger.error("S -> C: 550 Access denied.");
                throw new FileSystemException("Access denied.");
            }

            if (Files.exists(to)) {
                logger.error("S -> C: 550 Target already exists.");
                throw new FileSystemException("Target already exists.");
            }

            Files.move(from, to);

            FtpResponse response = new FtpResponse(
                FtpResponseCode.FILE_ACTION_OK,
                "Rename successful."
            );
            out.print(response.serialize());
            out.flush();

            logger.debug("S -> C: {}", response.serialize().trim());

        } catch (IOException e) {

            logger.error("Rename failed from {} to {}: {}", from, to, e.getMessage());
            throw new FileSystemException("Rename operation failed.");

        } finally {
            session.clearRenameFrom();
        }
    }
}
