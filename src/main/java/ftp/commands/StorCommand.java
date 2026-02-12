package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import ftp.data.PassiveServer;
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

        PassiveServer passiveServer = session.getPassiveServer();
        if (passiveServer == null) {
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

        // Accept data connection
        String mode = session.getTransferType() == UserSession.TransferType.ASCII 
            ? "ASCII" : "BINARY";
        out.print("150 Opening " + mode + " mode data connection for " + args + ".\r\n");
        out.flush();

        Socket dataSocket;
        try {
            dataSocket = passiveServer.getServerSocket().accept();
        } catch (IOException e) {
            throw new ConnectionException("Failed to accept data connection: " + e.getMessage());
        }
        
        try {
            // Receive file
            TransferManager.receiveFile(filePath, dataSocket);
            
            out.print("226 Transfer complete.\r\n");
            out.flush();
            
        } finally {
            try {
                dataSocket.close();
                session.clearPassiveServer(); // Clean up after transfer
            } catch (IOException | ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}