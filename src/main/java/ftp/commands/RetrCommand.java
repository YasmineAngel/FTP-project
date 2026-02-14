package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import ftp.data.TransferManager;
import ftp.utils.FileLockManager;
import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

public class RetrCommand implements FTPCommand {
    
    private final PathUtils pathUtils;
    
    public RetrCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws ftp.exceptions.FTPException {
        if (!session.isLoggedIn()) {
            FtpResponse response = new FtpResponse(
                FtpResponseCode.NOT_LOGGED_IN
            );

            out.print(response.serialize());
            out.flush();
            return;
        }

        if (args == null || args.trim().isEmpty()) {
            FtpResponse response = new FtpResponse(
                FtpResponseCode.SYNTAX_ERROR_IN_ARGUMENTS
            );
            out.print(response.serialize());
            out.flush();
            return;
        }

        FileLockManager lockManager = FileLockManager.getInstance();
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

        Path filePath = pathUtils.resolve(session.getCurrentDirectory(), args);

       
        if (filePath == null || !pathUtils.exists(filePath) || !pathUtils.isFile(filePath)) {
            FtpResponse response = new FtpResponse(
                FtpResponseCode.FILE_UNAVAILABLE
            );

            out.print(response.serialize());
            out.flush();
            return;
        }


        lockManager.acquireReadLock(filePath);

        long restartMarker = session.getRestartMarker();
        session.clearRestartMarker(); 

        
        String mode = session.getTransferType() == UserSession.TransferType.ASCII 
            ? "ASCII" : "BINARY";
        String restartMsg = restartMarker > 0 ? " (restarting at " + restartMarker + ")" : "";
        FtpResponse response = new FtpResponse(
            FtpResponseCode.DATA_CONNECTION_START,
            "Opening " + mode + " mode data connection for " + args + restartMsg

        );

        out.print(response.serialize());
        out.flush();
        


        Socket dataSocket = null;
        try {
            if (isPassiveMode) {
                dataSocket = session.getPassiveServer().getServerSocket().accept();
            } else {
                dataSocket = new Socket(session.getPortModeAddress(), session.getPortMode());
            }

            session.setActiveDataSocket(dataSocket);

            TransferManager.sendFile(filePath, dataSocket, restartMarker);
            session.clearRestartMarker();
            response = new FtpResponse(
                FtpResponseCode.DATA_TRANSFER_COMPLETE
            );

            out.print(response.serialize());
            out.flush();
           

        } catch (IOException e) {
            response = new FtpResponse(
                FtpResponseCode.CONNECTION_CLOSED
            );

            out.print(response.serialize());
            out.flush();
            throw new ConnectionException("Download interrupted: " + e.getMessage());
            
        } finally {

        	try {
                if (dataSocket != null) dataSocket.close();
                session.setActiveDataSocket(null);
                session.clearPassiveServer();
                session.clearPortMode();

                lockManager.releaseReadLock(filePath);

            } catch (IOException | ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}