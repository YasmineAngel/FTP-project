package ftp.commands;

import ftp.model.UserSession;
import ftp.exceptions.FTPException;
import java.io.PrintWriter;

public class AborCommand implements FTPCommand {
    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        // If there is an active passive server/transfer, clean it up
        if (session.getPassiveServer() != null) {
            session.clearPassiveServer(); // Closes the data socket
            out.print("426 Connection closed; transfer aborted.\r\n");
            out.print("226 Abort successful.\r\n");
        } else {
            out.print("226 Abort ignored (no active transfer).\r\n");
        }
        out.flush();
    }
}