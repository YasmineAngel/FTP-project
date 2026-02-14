package ftp.commands;

import java.io.PrintWriter;
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

public class CwdCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(CwdCommand.class);

    private final PathUtils pathUtils;

    public CwdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: CWD {}", args);

        if (!session.isLoggedIn()) {
            logger.error("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        if (args == null || args.isBlank()) {
            logger.error("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path resolved;
        if (args.startsWith("/")) {
            resolved = pathUtils.resolveAbsolute(args);
        } else {
            resolved = pathUtils.resolve(session.getCurrentDirectory(), args);
        }

        if (resolved == null) {
            logger.error("S -> C: 550 Access denied.");
            throw new FileSystemException("Access denied.");
        }

        if (!pathUtils.exists(resolved) || !pathUtils.isDirectory(resolved)) {
            logger.error("S -> C: 550 Not a directory.");
            throw new FileSystemException("Not a directory.");
        }

        String virtualPath = pathUtils.toFtpPath(resolved);
        session.setCurrentDirectory(virtualPath);
        FtpResponse response = new FtpResponse(
            FtpResponseCode.FILE_ACTION_OK, 
            "Directory successfully changed to " + virtualPath
        );

        out.print(response.serialize());
        out.flush();

        logger.debug("S -> C: {}", response.serialize().trim());
    }
}
