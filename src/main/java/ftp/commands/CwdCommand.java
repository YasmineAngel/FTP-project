package ftp.commands;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

import java.io.PrintWriter;
import java.nio.file.Path;

public class CwdCommand implements FTPCommand {

    private final PathUtils pathUtils;

    public CwdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {

        if (!session.isLoggedIn()) {
            out.println("530 Not logged in.");
            return;
        }

        if (args == null || args.isBlank()) {
            out.println("501 Syntax error in parameters.");
            return;
        }

        Path resolved = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (resolved == null) {
            out.println("550 Access denied.");
            return;
        }

        if (!pathUtils.exists(resolved) || !pathUtils.isDirectory(resolved)) {
            out.println("550 Not a directory.");
            return;
        }

        session.setCurrentDirectory(pathUtils.toFtpPath(resolved));
        out.println("250 Directory successfully changed.");
    }
}
