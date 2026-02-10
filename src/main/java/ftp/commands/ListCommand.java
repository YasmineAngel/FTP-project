package ftp.commands;

import ftp.model.UserSession;
import ftp.utils.PathUtils;
import ftp.data.ListManager; // Import your new manager
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class ListCommand implements FTPCommand {
    private final PathUtils pathUtils;

    public ListCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
        if (session.getPassiveServer() == null) {
            out.print("425 Use PASV first.\r\n");
            out.flush();
            return;
        }

        try {
            // 1. Notify control channel
            out.print("150 Opening data connection for directory list.\r\n");
            out.flush();

            // 2. Resolve the directory to list
            // If args is null, list current directory. If args is a path, list that.
            Path dirToList = pathUtils.resolve(session.getCurrentDirectory(), args);

            // 3. Accept data connection and use ListManager
            try (Socket dataSocket = session.getPassiveServer().getServerSocket().accept()) {
                ListManager.sendDirectoryListing(dirToList, dataSocket);
            }

            // 4. Close out the transaction
            session.clearPassiveServer();
            out.print("226 Transfer complete.\r\n");
            out.flush();

        } catch (Exception e) {
            out.print("425 Can't open data connection or directory error.\r\n");
            out.flush();
        }
    }
}