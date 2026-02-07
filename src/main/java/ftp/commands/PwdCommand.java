package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class PwdCommand implements FTPCommand {

    private final PathUtils pathUtils;
    public PwdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }
    @Override
    public void execute(String args, UserSession session, PrintWriter out) {

        if (!session.isLoggedIn()) {
            out.println("530 Not logged in.");
            return;
        }

        String cwd = session.getCurrentDirectory();
        pathUtils.resolve(cwd, null);

        // out.println("257 \"" + cwd + "\" is the current directory.");
    }
}
