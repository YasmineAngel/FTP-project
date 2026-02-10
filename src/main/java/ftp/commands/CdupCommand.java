package ftp.commands;

import ftp.model.UserSession;
import ftp.utils.PathUtils;
import java.io.PrintWriter;
import java.nio.file.Path;

public class CdupCommand implements FTPCommand {
    private final PathUtils pathUtils;

    public CdupCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
        if (!session.isLoggedIn()) {
            out.print("530 Not logged in.\r\n");
            out.flush();
            return;
        }

        Path currentFsPath = pathUtils.resolve(session.getCurrentDirectory(), null);
        Path parent = pathUtils.getParent(currentFsPath);

        String newFtpPath = pathUtils.toFtpPath(parent);
        session.setCurrentDirectory(newFtpPath);
        
        out.print("200 Directory changed to " + newFtpPath + "\r\n");
        out.flush();
    }
}