package ftp.commands;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class PwdCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(PwdCommand.class);

    private final PathUtils pathUtils;

    public PwdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: PWD");

        if (!session.isLoggedIn()) {
            logger.warn("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        String virtualPath = session.getCurrentDirectory();
        String response = "257 \"" + virtualPath + "\" is the current directory.";

        out.println(response);
        logger.debug("S -> C: {}", response);
    }
}
