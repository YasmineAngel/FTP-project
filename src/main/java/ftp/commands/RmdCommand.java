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
import ftp.utils.PathUtils;

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
            throw new FileSystemException("Directory not found.");
        }

        if (!pathUtils.isDirectory(target)) {
            logger.error("S -> C: 550 Not a directory.");
            throw new FileSystemException("Not a directory.");
        }

        try {

            Files.delete(target); 

            FtpResponse response = new FtpResponse(
                FtpResponseCode.FILE_ACTION_OK,
                "Directory removed successfully."
            );

            out.print(response.serialize());
            out.flush();

            logger.debug("S -> C: {}", response.serialize().trim());

        } catch (IOException e) {

            logger.error("Failed to remove directory {}: {}", target, e.getMessage());

            throw new FileSystemException("Remove directory operation failed.");
        }
    }
}
