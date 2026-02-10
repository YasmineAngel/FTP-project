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
            out.print("530 Not logged in.\r\n");
            out.flush();
            return;
        }

        // Return the virtual FTP path (e.g., "/")
        String cwd = session.getCurrentDirectory();
        
        // Standard FTP response format for PWD
        out.print("257 \"" + cwd + "\" is the current directory.\r\n");
        out.flush(); 
    }
}