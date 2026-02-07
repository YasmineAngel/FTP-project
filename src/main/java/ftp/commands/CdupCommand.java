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
            out.println("530 Not logged in.");
            return;
        }

        Path currentFsPath = pathUtils.resolve(
                session.getCurrentDirectory(),
                null
        );

        if (currentFsPath == null) {
            out.println("550 Access denied.");
            return;
        }

        Path parent = pathUtils.getParent(currentFsPath);

        session.setCurrentDirectory(pathUtils.toFtpPath(parent));
        out.println("200 Directory changed to parent.");
    }
}
