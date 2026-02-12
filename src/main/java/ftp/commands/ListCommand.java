package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import ftp.data.ListManager;
import ftp.exceptions.ConnectionException;
import ftp.exceptions.FTPException;
import ftp.exceptions.UnavailableFileException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

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

        // Check if PORT or PASV mode is set
        boolean isPassiveMode = session.getPassiveServer() != null;
        boolean isPortMode = session.hasPortMode();
        
        if (!isPassiveMode && !isPortMode) {
            out.print("425 Use PORT or PASV first.\r\n");
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

        // 3. Accept or connect data connection and use ListManager
        Socket dataSocket;
        try {
            if (isPassiveMode) {
                // PASV mode: accept connection from client
                dataSocket = session.getPassiveServer().getServerSocket().accept();
            } else {
                // PORT mode: connect to client
                dataSocket = new Socket(session.getPortModeAddress(), session.getPortMode());
            }
        } catch (IOException e) {
            throw new ConnectionException("Failed to establish data connection: " + e.getMessage());
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
                if (isPassiveMode) {
                    session.clearPassiveServer();
                } else {
                    session.clearPortMode();
                }
            } catch (IOException | ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}