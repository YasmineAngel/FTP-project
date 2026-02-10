package ftp.commands;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class MkdCommand implements FTPCommand {

    private final PathUtils pathUtils;

    public MkdCommand(PathUtils pathUtils) {
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

        if (target == null) {
            out.print("550 Access denied.\r\n");
            out.flush();
            return;
        }

        try {
            if (Files.exists(target)) {
                out.print("550 Directory already exists.\r\n");
                out.flush();
                return;
            }

            Files.createDirectories(target);

            String ftpPath = pathUtils.toFtpPath(target);
            out.print("257 \"" + ftpPath + "\" directory created.\r\n");
            out.flush();

        } catch (IOException e) {
            out.print("550 Create directory operation failed.\r\n");
            out.flush();
        }
    }
}
