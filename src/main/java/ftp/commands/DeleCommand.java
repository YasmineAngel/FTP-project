package ftp.commands;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleCommand implements FTPCommand {

    private final PathUtils pathUtils;

    public DeleCommand(PathUtils pathUtils) {
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
            out.print("550 File not found.\r\n");
            out.flush();
            return;
        }

        if (!pathUtils.isFile(target)) {
            out.print("550 Not a regular file.\r\n");
            out.flush();
            return;
        }

        try {
            Files.delete(target);
            out.print("250 File deleted successfully.\r\n");
            out.flush();
        } catch (IOException e) {
            out.print("550 Delete operation failed.\r\n");
            out.flush();
        }
    }
}
