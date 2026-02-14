package ftp.commands;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.FTPException;
import ftp.exceptions.SequenceException;
import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

public class PassCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(PassCommand.class);

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {

        logger.debug("C -> S: PASS ******");

        if (session.getUsername() == null) {
            logger.error("S -> C: 503 Bad sequence of commands.");
            throw new SequenceException("Bad sequence of commands.");
        }

        session.login(session.getUsername());

        FtpResponse response = new FtpResponse(
            FtpResponseCode.COMMAND_OK,
            "User logged in, proceed."
        );

        out.print(response.serialize());
        out.flush();

        logger.debug("S -> C: {}", response.serialize().trim());
    }
}
