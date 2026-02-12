package ftp.commands;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.FTPException;
import ftp.exceptions.SyntaxException;
import ftp.model.UserSession;

public class UserCommand implements FTPCommand {

    private static final Logger logger = LogManager.getLogger(UserCommand.class);

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        logger.info("C -> S: USER {}", args);

        if (args == null || args.isBlank()) {
            logger.info("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        session.logout(); // reset any previous state
        session.setUsername(args); // temporary login step (password comes next)

        out.println("331 Username ok, need password.");
        logger.info("S -> C: 331 Username ok, need password.");
    }
}
