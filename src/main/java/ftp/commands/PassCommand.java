package ftp.commands;

import ftp.model.UserSession;
import java.io.PrintWriter;

public class PassCommand implements FTPCommand {

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {

        if (session.getUsername() == null) {
            out.println("503 Bad sequence of commands.");
            return;
        }

        // For now: accept any password
        session.login(session.getUsername());

        out.println("230 User logged in, proceed.");
    }
}

