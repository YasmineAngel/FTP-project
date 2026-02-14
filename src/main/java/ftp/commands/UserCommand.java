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
        logger.debug("C -> S: USER {}", args);

        if (args == null || args.isBlank()) {
            logger.error("S -> C: 501 Syntax error in parameters.");
            throw new SyntaxException("Syntax error in parameters.");
        }

        session.logout();
        session.setUsername(args); 

        out.println("331 Username ok, need password.");
        logger.debug("S -> C: 331 Username ok, need password.");
    }
}
