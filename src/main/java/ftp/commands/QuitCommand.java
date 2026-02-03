package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;

public class QuitCommand implements FTPCommand {

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
        out.println("221 Service closing control connection.");
        session.disconnect();
    }
}
