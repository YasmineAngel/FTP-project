package ftp.commands;

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

public class RnfrCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(RnfrCommand.class);

    private final PathUtils pathUtils;

    public RnfrCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: RNFR {}", args);

        if (!session.isLoggedIn()) {
            logger.error("S -> C: 530 Not logged in.");
            throw new AuthException("Not logged in.");
        }

        if (args == null || args.isBlank()) {
            logger.error("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        Path target = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (target == null || !Files.exists(target)) {
            logger.error("S -> C: 550 File or directory does not exist.");
            throw new FileSystemException("File or directory does not exist.");
        }

        session.setRenameFrom(target.toAbsolutePath().toString());

        FtpResponse response = new FtpResponse(
            FtpResponseCode.NEED_ACCOUNT, 
            "Ready for RNTO."
        );

        out.print(response.serialize());
        out.flush();

        logger.debug("S -> C: {}", response.serialize().trim());
    }
}
