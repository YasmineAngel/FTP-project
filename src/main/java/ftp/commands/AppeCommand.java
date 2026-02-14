package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import ftp.data.TransferManager;
import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;
import ftp.utils.PathUtils;


public class AppeCommand implements FTPCommand {
    
    private final PathUtils pathUtils;

    public AppeCommand(PathUtils pathUtils) {
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

        boolean isPassiveMode = session.getPassiveServer() != null;
        boolean isPortMode = session.hasPortMode();
        
        if (!isPassiveMode && !isPortMode) {
            out.print("425 Use PORT or PASV first.\r\n");
            out.flush();
            return;
        }

        Path filePath = pathUtils.resolve(session.getCurrentDirectory(), args);

        if (filePath == null || !pathUtils.isWithinRoot(filePath)) {
            out.print("550 Invalid path.\r\n");
            out.flush();
            return;
        }

        String mode = session.getTransferType() == UserSession.TransferType.ASCII 
            ? "ASCII" : "BINARY";
        out.print("150 Opening " + mode + " mode data connection for " + args + " (append).\r\n");
        out.flush();

        Socket dataSocket = null;
        try {
            if (isPassiveMode) {
                dataSocket = session.getPassiveServer().getServerSocket().accept();
            } else {
                dataSocket = new Socket(session.getPortModeAddress(), session.getPortMode());
            }

            session.setActiveDataSocket(dataSocket);

            TransferManager.receiveFile(filePath, dataSocket, session.getRestartMarker(), true);            
            out.print("226 Transfer complete.\r\n");
            out.flush();
            
        } catch (IOException e) {
            if (session.getActiveDataSocket() == null) {
                out.print("426 Connection closed; transfer aborted.\r\n");
            } else {
                out.print("425 Data connection error: " + e.getMessage() + "\r\n");
            }
            out.flush();
            throw new ConnectionException("Append interrupted: " + e.getMessage());
        } finally {
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
            }
        }
    }
}