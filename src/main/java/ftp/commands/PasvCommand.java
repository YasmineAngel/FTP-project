package ftp.commands;

import java.io.PrintWriter;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.data.PassiveServer;
import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;

import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

public class PasvCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(PasvCommand.class);

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

       
        try {
            session.clearPassiveServer();
        } catch (java.io.IOException | ConnectionException e) {
            throw new ConnectionException(e.getMessage());
        }
        session.clearPortMode();

        
        PassiveServer passiveServer = new PassiveServer();
        int port = passiveServer.openRandomPort();
        session.setPassiveServer(passiveServer);

       
        InetAddress localAddress;
        try {
            localAddress = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException e) {
            throw new ConnectionException("Cannot determine local address: " + e.getMessage());
        }
        byte[] address = localAddress.getAddress();
        
        
        int p1 = port / 256;
        int p2 = port % 256;
        
        String message = String.format("Entering Passive Mode (%d,%d,%d,%d,%d,%d)",
                address[0] & 0xFF, address[1] & 0xFF, address[2] & 0xFF, address[3] & 0xFF,
                p1, p2);
        
        FtpResponse response = new FtpResponse(
                FtpResponseCode.ENTERING_PASSIVE,
                message
        );
        logger.trace("S -> C: {}", response.serialize());
        out.print(response.serialize());
        out.flush();
        logger.debug("PASV mode activated on port: {}", port);
    }
}