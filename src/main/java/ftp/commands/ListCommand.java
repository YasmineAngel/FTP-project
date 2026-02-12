package ftp.commands;

import ftp.model.UserSession;
import ftp.utils.PathUtils;
import ftp.data.ListManager;
import ftp.exceptions.FTPException;
import ftp.exceptions.ConnectionException;
import ftp.exceptions.UnavailableFileException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class ListCommand implements FTPCommand {
    private final PathUtils pathUtils;

    public ListCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        if (!session.isLoggedIn()) {
            out.print("530 Not logged in.\r\n");
            out.flush();
            return;
        }

        if (session.getPassiveServer() == null) {
            out.print("425 Use PASV first.\r\n");
            out.flush();
            return;
        }

        // 1. Notify control channel
        out.print("150 Opening data connection for directory list.\r\n");
        out.flush();

        // 2. Resolve the directory to list
        // If args is null, list current directory. If args is a path, list that.
        Path dirToList = pathUtils.resolve(session.getCurrentDirectory(), args);

        // Validate directory exists
        if (dirToList == null || !pathUtils.exists(dirToList) || !pathUtils.isDirectory(dirToList)) {
            out.print("550 Directory not found.\r\n");
            out.flush();
            return;
        }

        // 3. Accept data connection and use ListManager
        Socket dataSocket;
        try {
            dataSocket = session.getPassiveServer().getServerSocket().accept();
        } catch (IOException e) {
            throw new ConnectionException("Failed to accept data connection: " + e.getMessage());
        }

        try {
            ListManager.sendDirectoryListing(dirToList, dataSocket);
            out.print("226 Transfer complete.\r\n");
            out.flush();
        } catch (UnavailableFileException | ConnectionException e) {
            throw e;
        } finally {
            // Shutdown output stream to signal EOF to the client, then close
            try {
                if (!dataSocket.isClosed()) {
                    dataSocket.shutdownOutput();
                }
            } catch (IOException ignored) {
                // Ignore if already closed
            }
            try {
                if (!dataSocket.isClosed()) {
                    dataSocket.close();
                }
                session.clearPassiveServer();
            } catch (IOException | ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}