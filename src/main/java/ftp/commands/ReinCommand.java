package ftp.commands;

import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;
import ftp.exceptions.FTPException;
import java.io.PrintWriter;

public class ReinCommand implements FTPCommand {
	@Override
	public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
		session.logout();
		sendResponse(out, new FtpResponse(FtpResponseCode.SERVICE_READY, "Service ready for new user."));
	}

	private void sendResponse(PrintWriter out, FtpResponse response) {
		out.print(response.serialize());
		out.flush();
	}
}