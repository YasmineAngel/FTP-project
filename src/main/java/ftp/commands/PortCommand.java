package ftp.commands;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;

public class PortCommand implements FTPCommand {
    
    private static final Logger logger = LogManager.getLogger(PortCommand.class);

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

        
        String[] parts = args.split(",");
        if (parts.length != 6) {
            out.print("501 Syntax error in parameters.\r\n");
            out.flush();
            return;
        }

        try {
            
            byte[] address = new byte[4];
            for (int i = 0; i < 4; i++) {
                int octet = Integer.parseInt(parts[i].trim());
                if (octet < 0 || octet > 255) {
                    out.print("501 Invalid IP address.\r\n");
                    out.flush();
                    return;
                }
                address[i] = (byte) octet;
            }

           
            int p1 = Integer.parseInt(parts[4].trim());
            int p2 = Integer.parseInt(parts[5].trim());
            if (p1 < 0 || p1 > 255 || p2 < 0 || p2 > 255) {
                out.print("501 Invalid port number.\r\n");
                out.flush();
                return;
            }
            int port = p1 * 256 + p2;
            if (port < 1 || port > 65535) {
                out.print("501 Invalid port number.\r\n");
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

            out.print("200 PORT command successful.\r\n");
            out.flush();
            logger.info("PORT mode set: {}:{}", clientAddress.getHostAddress(), port);
        } catch (NumberFormatException e) {
            out.print("501 Syntax error in parameters.\r\n");
            out.flush();
        } catch (UnknownHostException e) {
            out.print("501 Invalid IP address.\r\n");
            out.flush();
        }
    }
}
