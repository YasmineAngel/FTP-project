package ftp.commands;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class RmdCommand implements FTPCommand {

    private final PathUtils pathUtils;

    public RmdCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

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

        Path target = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (target == null || !pathUtils.exists(target)) {
            out.print("550 Directory not found.\r\n");
            out.flush();
            return;
        }

        if (!pathUtils.isDirectory(target)) {
            out.print("550 Not a directory.\r\n");
            out.flush();
            return;
        }

        try {
            Files.delete(target); // fails if directory not empty
            out.print("250 Directory removed successfully.\r\n");
            out.flush();
        } catch (IOException e) {
            out.print("550 Remove directory operation failed.\r\n");
            out.flush();
        }
    }
}
