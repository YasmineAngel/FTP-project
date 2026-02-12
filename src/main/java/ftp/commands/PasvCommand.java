package ftp.commands;

import java.io.PrintWriter;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.data.PassiveServer;
import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;

public class PasvCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(PasvCommand.class);

    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws ftp.exceptions.FTPException {
        if (!session.isLoggedIn()) {
            out.print("530 Not logged in.\r\n");
            out.flush();
            return;
        }

        // Close any existing passive server and clear PORT mode (PASV replaces PORT)
        try {
            session.clearPassiveServer();
        } catch (java.io.IOException | ConnectionException e) {
            throw new ConnectionException(e.getMessage());
        }
        session.clearPortMode();

        // Create and open new passive server
        PassiveServer passiveServer = new PassiveServer();
        int port = passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);

        // Get server IP address
        InetAddress localAddress;
        try {
            localAddress = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException e) {
            throw new ConnectionException("Cannot determine local address: " + e.getMessage());
        }
        byte[] address = localAddress.getAddress();
        
        // Format: 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2)
        // Port is split into two bytes: p1 = port / 256, p2 = port % 256
        int p1 = port / 256;
        int p2 = port % 256;
        
        String response = String.format("227 Entering Passive Mode (%d,%d,%d,%d,%d,%d)\r\n",
                address[0] & 0xFF, address[1] & 0xFF, address[2] & 0xFF, address[3] & 0xFF,
                p1, p2);
        
        out.print(response);
        out.flush();
        logger.info("PASV mode activated on port: {}", port);
    }
}