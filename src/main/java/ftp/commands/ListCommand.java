package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.data.ListManager;
import ftp.data.PassiveServer;
import ftp.model.UserSession;
import ftp.utils.PathUtils;

public class ListCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(ListCommand.class);
    private final PathUtils pathUtils;

    public ListCommand(PathUtils pathUtils) {
        this.pathUtils = pathUtils;
    }

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
        if (!session.isLoggedIn()) {
            out.print("530 Not logged in.\r\n");
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
            // Resolve directory path
            Path targetDir;
            if (args == null || args.trim().isEmpty()) {
                // List current directory
                targetDir = pathUtils.resolve(session.getCurrentDirectory(), null);
            } else {
                // List specified directory
                targetDir = pathUtils.resolve(session.getCurrentDirectory(), args);
            }

            // Validate path
            if (targetDir == null || !pathUtils.exists(targetDir) || !pathUtils.isDirectory(targetDir)) {
                out.print("550 Directory not found.\r\n");
                out.flush();
                return;
            }

            // Accept data connection
            out.print("150 Opening ASCII mode data connection for file list.\r\n");
            out.flush();

            Socket dataSocket = passiveServer.getServerSocket().accept();
            
            try {
                // Send directory listing
                ListManager.sendDirectoryListing(targetDir, dataSocket);
                
                out.print("226 Transfer complete.\r\n");
                out.flush();
                logger.info("Directory listing sent for: {}", targetDir);
                
            } finally {
                dataSocket.close();
                session.clearPassiveServer(); // Clean up after transfer
            }

        } catch (IOException e) {
            logger.error("Error listing directory", e);
            out.print("426 Connection closed; transfer aborted.\r\n");
            out.flush();
            try {
                session.clearPassiveServer();
            } catch (IOException ex) {
                System.getLogger(ListCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }
}