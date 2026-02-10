package ftp.commands;

import java.io.PrintWriter;
import java.nio.file.Path;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class CwdCommand implements FTPCommand{

    private final PathUtils pathUtils;

    public CwdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    // @Override
    // public void execute(String args, UserSession session, PrintWriter out) {

    //     if (!session.isLoggedIn()) {
    //         out.print("530 Not logged in.\r\n");
    //         out.flush();
    //         return;
    //     }

    //     if (args == null || args.isBlank()) {
    //         out.print("501 Syntax error in parameters.\r\n");
    //         out.flush();
    //         return;
    //     }

    //     Path resolved = pathUtils.resolve(session.getCurrentDirectory(), args);

    //     if (resolved == null) {
    //         out.print("550 Access denied.\r\n");
    //         out.flush();
    //         return;
    //     }

    //     if (!pathUtils.exists(resolved) || !pathUtils.isDirectory(resolved)) {
    //         out.print("550 Not a directory.\r\n");
    //         out.flush();
    //         return;
    //     }

    //     session.setCurrentDirectory(pathUtils.toFtpPath(resolved));
    //     out.print("250 Directory successfully changed.\r\n");
    //     out.flush();
    // }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
        if (!session.isLoggedIn()) {
            out.print("530 Not logged in.\r\n");
            out.flush();
            return;
        }
    
        if (args == null || args.isBlank()) {
            out.print("501 Syntax error in parameters.\r\n");
            out.flush();
            return;
        }
    
        // NEW LOGIC: Check if the path is absolute or relative
        Path resolved;
        if (args.startsWith("/")) {
            // Handle CWD /folder_3
            resolved = pathUtils.resolveAbsolute(args);
        } else {
            // Handle CWD folder_3 (relative to current location)
            resolved = pathUtils.resolve(session.getCurrentDirectory(), args);
        }
    
        if (resolved == null) {
            out.print("550 Access denied.\r\n");
            out.flush();
            return;
        }
    
        if (!pathUtils.exists(resolved) || !pathUtils.isDirectory(resolved)) {
            out.print("550 Not a directory.\r\n");
            out.flush();
            return;
        }
    
        // Convert back to virtual path and save to session
        String virtualPath = pathUtils.toFtpPath(resolved);
        session.setCurrentDirectory(virtualPath);
        
        out.print("250 Directory successfully changed to " + virtualPath + "\r\n");
        out.flush();
    }
}
