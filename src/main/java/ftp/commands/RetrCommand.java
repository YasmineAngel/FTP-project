package ftp.commands;

import ftp.data.PassiveServer;
import ftp.data.TransferManager;
import ftp.model.UserSession;
import ftp.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class RetrCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(RetrCommand.class);
    private final PathUtils pathUtils;

    public RetrCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
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

        try {
            // Resolve file path
            Path filePath = pathUtils.resolve(session.getCurrentDirectory(), args);

            // Validate path
            if (filePath == null || !pathUtils.exists(filePath) || !pathUtils.isFile(filePath)) {
                out.print("550 File not found.\r\n");
                out.flush();
                return;
            }

            // Accept data connection
            String mode = session.getTransferType() == UserSession.TransferType.ASCII 
                ? "ASCII" : "BINARY";
            out.print("150 Opening " + mode + " mode data connection for " + args + ".\r\n");
            out.flush();

            Socket dataSocket = passiveServer.getServerSocket().accept();
            
            try {
                // Send file
                TransferManager.sendFile(filePath, dataSocket);
                
                out.print("226 Transfer complete.\r\n");
                out.flush();
                
            } finally {
                dataSocket.close();
                session.clearPassiveServer(); // Clean up after transfer
            }

        } catch (IOException e) {
            logger.error("Error retrieving file: {}", args, e);
            out.print("426 Connection closed; transfer aborted.\r\n");
            out.flush();
            session.clearPassiveServer();
        }
    }
}