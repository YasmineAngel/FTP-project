package ftp.commands;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class RntoCommand implements FTPCommand {

    private final PathUtils pathUtils;

    public RntoCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {

        if (!session.isLoggedIn()) {
            out.println("530 Not logged in.");
            return;
        }

        String fromPathStr = session.getRenameFrom();
        if (fromPathStr == null) {
            out.println("503 Bad sequence of commands.");
            return;
        }

        if (args == null || args.isBlank()) {
            out.println("501 Syntax error in parameters.");
            return;
        }

        Path from = Paths.get(fromPathStr);
        Path to = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (Files.exists(to)) {
            out.println("550 Target already exists.");
            session.clearRenameFrom();
            return;
        }

        try {
            Files.move(from, to); // modern and throws exception on failure
            out.println("250 Rename successful.");
        } catch (Exception e) {
            out.println("550 Rename failed: " + e.getMessage());
        } finally {
            session.clearRenameFrom();
        }
    }
}
