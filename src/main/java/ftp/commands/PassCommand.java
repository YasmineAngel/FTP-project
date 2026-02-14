package ftp.commands;

import ftp.exceptions.FTPException;
import ftp.exceptions.SequenceException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

        String response = "230 User logged in, proceed.";
        out.println(response);

        logger.debug("S -> C: {}", response);
    }
}
