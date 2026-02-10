package ftp.commands;

import ftp.model.UserSession;
import ftp.exceptions.FTPException;
import java.io.PrintWriter;

public class ReinCommand implements FTPCommand {
    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        session.logout(); // Clears username, login status, and resets directory
        out.print("220 Service ready for new user.\r\n");
        out.flush();
    }
}