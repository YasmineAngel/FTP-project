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

public class RetrCommand implements FTPCommand {
    
    private final PathUtils pathUtils;

    public RetrCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws ftp.exceptions.FTPException {
        if (!session.isLoggedIn()) {
            out.print("530 Not logged in.\r\n");
            out.flush();
            return;
        }

        if (args == null || args.trim().isEmpty()) {
            out.print("501 Syntax error in parameters.\r\n");
            out.flush();
            return;
        }

        FileLockManager lockManager = FileLockManager.getInstance();
        boolean isPassiveMode = session.getPassiveServer() != null;
        boolean isPortMode = session.hasPortMode();
        
        if (!isPassiveMode && !isPortMode) {
            out.print("425 Use PORT or PASV first.\r\n");
            out.flush();
            return;
        }

        Path filePath = pathUtils.resolve(session.getCurrentDirectory(), args);

       
        if (filePath == null || !pathUtils.exists(filePath) || !pathUtils.isFile(filePath)) {
            out.print("550 File not found.\r\n");
            out.flush();
            return;
        }


        lockManager.acquireWriteLock(filePath);

        long restartMarker = session.getRestartMarker();
        session.clearRestartMarker(); 

        
        String mode = session.getTransferType() == UserSession.TransferType.ASCII 
            ? "ASCII" : "BINARY";
        String restartMsg = restartMarker > 0 ? " (restarting at " + restartMarker + ")" : "";
        out.print("150 Opening " + mode + " mode data connection for " + args + restartMsg + ".\r\n");
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
            
            out.print("226 Transfer complete.\r\n");
            out.flush();

        } catch (IOException e) {
            out.print("426 Connection closed; transfer aborted.\r\n");
            out.flush();
            throw new ConnectionException("Download interrupted: " + e.getMessage());
            
        } finally {

        	try {
                if (dataSocket != null) dataSocket.close();
                session.setActiveDataSocket(null);
                session.clearPassiveServer();
                session.clearPortMode();

                lockManager.releaseWriteLock(filePath);

            } catch (IOException | ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}