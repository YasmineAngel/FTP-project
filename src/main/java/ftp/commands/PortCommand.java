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

        // Parse PORT command: PORT h1,h2,h3,h4,p1,p2
        // Example: PORT 192,168,1,100,200,50
        String[] parts = args.split(",");
        if (parts.length != 6) {
            out.print("501 Syntax error in parameters.\r\n");
            out.flush();
            return;
        }

        try {
            // Parse IP address (h1,h2,h3,h4)
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

            // Parse port (p1,p2) where port = p1 * 256 + p2
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

            // Create InetAddress from bytes
            InetAddress clientAddress = InetAddress.getByAddress(address);

            // Clear any existing passive server (PORT replaces PASV)
            try {
                session.clearPassiveServer();
            } catch (java.io.IOException | ConnectionException e) {
                // Ignore errors when clearing
            }

            // Store PORT mode information in session
            session.setPortMode(clientAddress, port);

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
