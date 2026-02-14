package ftp.commands;

import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;
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
				sendResponse(out,
						new FtpResponse(FtpResponseCode.CONNECTION_CLOSED, "Connection closed; transfer aborted."));
				sendResponse(out, new FtpResponse(FtpResponseCode.SERVICE_CLOSING, "Abort successful."));
				if (session.getPassiveServer() != null) {
					session.clearPassiveServer();
				}
			} else {
				if (session.getPassiveServer() != null) {
					session.clearPassiveServer();
					sendResponse(out, new FtpResponse(FtpResponseCode.SERVICE_CLOSING,
							"Abort successful (passive server cleared)."));
				} else {
					sendResponse(out,
							new FtpResponse(FtpResponseCode.SERVICE_CLOSING, "Abort ignored (no active transfer)."));
				}
			}
		} catch (java.io.IOException e) {
			throw new ftp.exceptions.TransferException("Failed to close data connection: " + e.getMessage());
		}
	}

	private void sendResponse(PrintWriter out, FtpResponse response) {
		out.print(response.serialize());
		out.flush();
	}

}
