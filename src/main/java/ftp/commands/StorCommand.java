package ftp.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

public class StorCommand implements FTPCommand {
    
    private final PathUtils pathUtils;
    private static final Logger logger = LogManager.getLogger(UserCommand.class);

    public StorCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws ftp.exceptions.FTPException {
    	logger.debug("Session Object ID: " + System.identityHashCode(session));

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

        FileLockManager lockManager = FileLockManager.getInstance();
        Path filePath = pathUtils.resolve(session.getCurrentDirectory(), args);

        
        if (filePath == null || !pathUtils.isWithinRoot(filePath)) {
           FtpResponse response = new FtpResponse(
                FtpResponseCode.FILE_UNAVAILABLE,
                "Invalid Path"
            );

            out.print(response.serialize());
            out.flush();
            return;
        }
        lockManager.acquireWriteLock(filePath);
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

            TransferManager.receiveFile(filePath, dataSocket, restartMarker);
            
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
            throw new ConnectionException("Transfer interrupted: " + e.getMessage());
            
        } finally {
            lockManager.releaseWriteLock(filePath);
            try {
            	if (dataSocket != null) {
                    dataSocket.close();
                }
            	session.setActiveDataSocket(null);
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