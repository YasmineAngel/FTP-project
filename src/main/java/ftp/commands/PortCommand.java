package ftp.commands;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;

import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

public class PortCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(PortCommand.class);

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
        FtpResponse response = new FtpResponse(
            FtpResponseCode.SYNTAX_ERROR_IN_ARGUMENTS
        );

        
        if (args == null || args.trim().isEmpty()) {
            out.print(response.serialize());
            out.flush();
            return;
        }

        
        String[] parts = args.split(",");
        if (parts.length != 6) {
            out.print(response.serialize());
            out.flush();
            return;
        }

        try {
            
            byte[] address = new byte[4];
            for (int i = 0; i < 4; i++) {
                int octet = Integer.parseInt(parts[i].trim());
                if (octet < 0 || octet > 255) {
                    response = new FtpResponse(
                        FtpResponseCode.INVALID_ARGUMENTS,
                        "Invalid IP address."
                    );
                   out.print(response.serialize());
                    out.flush();
                    return;
                }
                address[i] = (byte) octet;
            }

            response = new FtpResponse(
                FtpResponseCode.INVALID_ARGUMENTS,
                "Invalid port number."
            );
            int p1 = Integer.parseInt(parts[4].trim());
            int p2 = Integer.parseInt(parts[5].trim());
            if (p1 < 0 || p1 > 255 || p2 < 0 || p2 > 255) {
                
                out.print(response.serialize());
                out.flush();
                return;
            }
            int port = p1 * 256 + p2;
            if (port < 1 || port > 65535) {
                out.print(response.serialize());
                out.flush();
                return;
            }

            
            InetAddress clientAddress = InetAddress.getByAddress(address);

            
            try {
                session.clearPassiveServer();
            } catch (java.io.IOException | ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            }

            session.setPortMode(port, clientAddress);
            response = new FtpResponse(
                FtpResponseCode.COMMAND_OK,
                "PORT command successful."
            );
            out.print(response.serialize());
            out.flush();
            
            logger.info("PORT mode set: {}:{}", clientAddress.getHostAddress(), port);
        } catch (NumberFormatException e) {
            response = new FtpResponse(
                FtpResponseCode.SYNTAX_ERROR_IN_ARGUMENTS
            );
            out.print(response.serialize());
            out.flush();
        } catch (UnknownHostException e) {
            response = new FtpResponse(
                FtpResponseCode.INVALID_ARGUMENTS,
                "Invalid IP address."
            );
            out.print(response.serialize());        
            out.flush();
        }
    }
}
