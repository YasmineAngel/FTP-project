package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;

public class PwdCommand implements FTPCommand {

    
    @Override
    public void execute(String args, UserSession session, PrintWriter out) {

        if (!session.isLoggedIn()) {
            out.println("530 Not logged in.");
            return;
        }

        String cwd = session.getCurrentDirectory();
        PathUtils pathUtils =  new PathUtils(cwd);
        pathUtils.resolve(cwd, null);

        out.println("257 \"" + cwd + "\" is the current directory.");
    }
}
