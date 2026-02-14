package ftp.commands;

import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

import ftp.model.UserSession;

public class RestCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(RestCommand.class);

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws ftp.exceptions.FTPException {
        if (!session.isLoggedIn()) {
            FtpResponse response = new FtpResponse(
                FtpResponseCode.NOT_LOGGED_IN
            );

            out.print(response.serialize());
            out.flush();
            return;
        }

        if (args == null || args.trim().isEmpty()) {
           
            session.setRestartMarker(0);
            FtpResponse response = new FtpResponse(
                FtpResponseCode.RESTART_POSITION_MARKED
                
            );
            out.print(response.serialize());
            out.flush();
            logger.debug("REST marker cleared");
            return;
        }

        try {
            long marker = Long.parseLong(args.trim());
            if (marker < 0) {
                logger.trace("Invalid restart marke");
                FtpResponse response = new FtpResponse(
                    FtpResponseCode.INVALID_ARGUMENTS,
                    "Invalid restart marker"
                );
                out.print(response.serialize());
                out.flush();
               
                return;
            }

            session.setRestartMarker(marker);
            FtpResponse response = new FtpResponse(
                FtpResponseCode.RESTART_POSITION_MARKED,
                "Restart position accepted (" + marker + ")"
            );

            out.print(response.serialize());
            out.flush();
            logger.info("REST marker set to: {}", marker);
        } catch (NumberFormatException e) {
           FtpResponse response = new FtpResponse(
                FtpResponseCode.SYNTAX_ERROR_IN_ARGUMENTS
            );

            out.print(response.serialize());
            out.flush();
        }
    }
}
