package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import ftp.data.TransferManager;
import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class StorCommand implements FTPCommand {
    
    private final PathUtils pathUtils;
    

    public StorCommand(PathUtils pathUtils) {
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

        // Check if PORT or PASV mode is set
        boolean isPassiveMode = session.getPassiveServer() != null;
        boolean isPortMode = session.hasPortMode();
        
        if (!isPassiveMode && !isPortMode) {
            out.print("425 Use PORT or PASV first.\r\n");
            out.flush();
            return;
        }

        // Resolve file path
        Path filePath = pathUtils.resolve(session.getCurrentDirectory(), args);

        // Validate path is within root (security)
        if (filePath == null || !pathUtils.isWithinRoot(filePath)) {
            out.print("550 Invalid path.\r\n");
            out.flush();
            return;
        }

        // Get restart marker
        long restartMarker = session.getRestartMarker();
        session.clearRestartMarker(); // Clear after use

        // Accept or connect data connection
        String mode = session.getTransferType() == UserSession.TransferType.ASCII 
            ? "ASCII" : "BINARY";
        String restartMsg = restartMarker > 0 ? " (restarting at " + restartMarker + ")" : "";
        out.print("150 Opening " + mode + " mode data connection for " + args + restartMsg + ".\r\n");
        out.flush();

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
            // Receive file (with restart marker if specified)
            TransferManager.receiveFile(filePath, dataSocket, restartMarker);
            
            out.print("226 Transfer complete.\r\n");
            out.flush();
            
        } finally {
            try {
                dataSocket.close();
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