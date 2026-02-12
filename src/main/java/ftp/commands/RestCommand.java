package ftp.commands;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.model.UserSession;

public class RestCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(RestCommand.class);

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws ftp.exceptions.FTPException {
        if (!session.isLoggedIn()) {
            out.print("530 Not logged in.\r\n");
            out.flush();
            return;
        }

        if (args == null || args.trim().isEmpty()) {
            // REST 0 clears the restart marker
            session.setRestartMarker(0);
            out.print("350 Restart position accepted (0).\r\n");
            out.flush();
            logger.info("REST marker cleared");
            return;
        }

        try {
            long marker = Long.parseLong(args.trim());
            if (marker < 0) {
                out.print("501 Invalid restart marker.\r\n");
                out.flush();
                return;
            }

            session.setRestartMarker(marker);
            out.print("350 Restart position accepted (" + marker + ").\r\n");
            out.flush();
            logger.info("REST marker set to: {}", marker);
        } catch (NumberFormatException e) {
            out.print("501 Syntax error in parameters.\r\n");
            out.flush();
        }
    }
}
