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

        boolean isPassiveMode = session.getPassiveServer() != null;
        boolean isPortMode = session.hasPortMode();
        
        if (!isPassiveMode && !isPortMode) {
            out.print("425 Use PORT or PASV first.\r\n");
            out.flush();
            return;
        }

        out.print("150 Opening data connection for directory list.\r\n");
        out.flush();

        Path dirToList = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (dirToList == null || !pathUtils.exists(dirToList) || !pathUtils.isDirectory(dirToList)) {
            out.print("550 Directory not found.\r\n");
            out.flush();
            return;
        }

        Socket dataSocket;
        try {
            if (isPassiveMode) {
                dataSocket = session.getPassiveServer().getServerSocket().accept();
            } else {
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
            try {
                if (!dataSocket.isClosed()) {
                    dataSocket.shutdownOutput();
                }
            } catch (IOException ignored) {
                throw new ConnectionException("Failed to close data connection: " + ignored.getMessage());
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