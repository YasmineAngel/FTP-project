package ftp.commands;

import ftp.model.UserSession;
import ftp.exceptions.FTPException;
import ftp.exceptions.ConnectionException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class AborCommand implements FTPCommand {
	@Override
	public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
	    try {
	        if (session.getPassiveServer() != null) {
	        	session.clearPassiveServer(); // throws IOException
	            out.print("426 Connection closed; transfer aborted.\r\n");
	            out.print("226 Abort successful.\r\n");
	        } else {
	            out.print("226 Abort ignored (no active transfer).\r\n");
	        }
	        out.flush();
	    } catch (java.io.IOException e) {
	        // Wrapping the IO error into protocol-level exception
	    	throw new ftp.exceptions.TransferException("Failed to close data connection: " + e.getMessage());	    }
	}
	}
