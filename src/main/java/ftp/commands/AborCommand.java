package ftp.commands;

import ftp.model.UserSession;
import ftp.exceptions.FTPException;
import ftp.exceptions.ConnectionException;
import java.io.IOException;
import java.io.PrintWriter;

public class AborCommand implements FTPCommand {
    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        // If there is an active passive server/transfer, clean it up
        if (session.getPassiveServer() != null) {
            try {
                session.clearPassiveServer(); // Closes the data socket
            } catch (IOException | ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            }
            out.print("426 Connection closed; transfer aborted.\r\n");
            out.print("226 Abort successful.\r\n");
        } else {
            out.print("226 Abort ignored (no active transfer).\r\n");
        }
        out.flush();
    }
}