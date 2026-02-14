package ftp.commands;

import ftp.model.UserSession;
import ftp.exceptions.FTPException;

import java.io.PrintWriter;
import java.net.Socket;

public class AborCommand implements FTPCommand {
	@Override
	public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
	    Socket socketToClose = session.getActiveDataSocket();
	    
	    if (socketToClose == null && session.getUsername() != null) {
	        UserSession mainSession = UserSession.getSessionForUser(session.getUsername());
	        if (mainSession != null) {
	            socketToClose = mainSession.getActiveDataSocket();
	        }
	    }
		
	    try {
	        if (socketToClose != null && !socketToClose.isClosed()) {
	        	socketToClose.close();
	            out.print("426 Connection closed; transfer aborted.\r\n");
	            out.print("226 Abort successful.\r\n");
	            if (session.getPassiveServer() != null) {
	                session.clearPassiveServer();
	            }
	        } else {
	        	if (session.getPassiveServer() != null) {
	                session.clearPassiveServer();
	                out.print("226 Abort successful (passive server cleared).\r\n");
	            } else {
	                out.print("226 Abort ignored (no active transfer).\r\n");
	            }
	        }
	        out.flush();
	    } catch (java.io.IOException e) {
	    	throw new ftp.exceptions.TransferException("Failed to close data connection: " + e.getMessage());	    }
	}
	}
