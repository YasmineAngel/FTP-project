package ftp.commands;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class RnfrCommand implements FTPCommand {

    private final PathUtils pathUtils;

    public RnfrCommand(PathUtils pathUtils) {
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

        Path target = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (!Files.exists(target)) {
            out.println("550 File or directory does not exist.");
            return;
        }

        // Store Path as string in session
        session.setRenameFrom(target.toAbsolutePath().toString());
        out.println("350 Ready for RNTO.");
    }
}
