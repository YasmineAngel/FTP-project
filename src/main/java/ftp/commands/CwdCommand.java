package ftp.commands;

import java.io.PrintWriter;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.AuthException;
import ftp.exceptions.FTPException;
import ftp.exceptions.SyntaxException;
import ftp.exceptions.FileSystemException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class CwdCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(CwdCommand.class);

    private final PathUtils pathUtils;

    public CwdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.info("C -> S: CWD {}", args);

        if (!session.isLoggedIn()) {
            logger.info("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        if (args == null || args.isBlank()) {
            logger.info("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path resolved;
        if (args.startsWith("/")) {
            resolved = pathUtils.resolveAbsolute(args);
        } else {
            resolved = pathUtils.resolve(session.getCurrentDirectory(), args);
        }

        if (resolved == null) {
            logger.info("S -> C: 550 Access denied.");
            throw new FileSystemException("Access denied.", 550);
        }

        if (!pathUtils.exists(resolved) || !pathUtils.isDirectory(resolved)) {
            logger.info("S -> C: 550 Not a directory.");
            throw new FileSystemException("Not a directory.", 550);
        }

        String virtualPath = pathUtils.toFtpPath(resolved);
        session.setCurrentDirectory(virtualPath);

        String response = "250 Directory successfully changed to " + virtualPath;
        out.println(response);
        logger.info("S -> C: {}", response);
    }
}
