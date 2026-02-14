package ftp.commands;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

import java.io.PrintWriter;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CdupCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(CdupCommand.class);

    private final PathUtils pathUtils;

    public CdupCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: CDUP");

        if (!session.isLoggedIn()) {
            logger.error("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        Path currentFsPath = pathUtils.resolve(session.getCurrentDirectory(), null);
        Path parent = pathUtils.getParent(currentFsPath);

        String newFtpPath = pathUtils.toFtpPath(parent);
        session.setCurrentDirectory(newFtpPath);

        String response = "250 Directory changed to " + newFtpPath;
        out.println(response);
        logger.debug("S -> C: {}", response);
    }
}
