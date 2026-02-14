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

import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;


public class ListCommand implements FTPCommand {
    private final PathUtils pathUtils;

    public ListCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        if (!session.isLoggedIn()) {
            FtpResponse response = new FtpResponse(
                FtpResponseCode.NOT_LOGGED_IN
            );

            out.print(response.serialize());
            out.flush();
            return;
        }

        boolean isPassiveMode = session.getPassiveServer() != null;
        boolean isPortMode = session.hasPortMode();
        
        if (!isPassiveMode && !isPortMode) {
            FtpResponse response = new FtpResponse(
                FtpResponseCode.INITIALIZE_TRANSFER_MODE_FIRST
            );

            out.print(response.serialize());
            out.flush();
            return;
        }
        FtpResponse response = new FtpResponse(
                FtpResponseCode.DATA_CONNECTION_START,
                 "for directory list"
        );
        out.print(response.serialize());
        out.flush();

        Path dirToList = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (!pathUtils.isExistingDirectory(dirToList)) {
            response = new FtpResponse(
                    FtpResponseCode.FILE_UNAVAILABLE         
            );
            out.print(response.serialize());
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
            ListManager.sendDirectoryListing(dirToList, dataSocket, pathUtils);
            response = new FtpResponse(
                FtpResponseCode.DATA_TRANSFER_COMPLETE
            );

            out.print(response.serialize());
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