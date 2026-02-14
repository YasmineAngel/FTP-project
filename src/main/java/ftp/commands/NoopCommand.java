package ftp.commands;

import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;
import ftp.exceptions.FTPException;
import java.io.PrintWriter;

public class NoopCommand implements FTPCommand {
	@Override
	public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
		sendResponse(out, new FtpResponse(FtpResponseCode.COMMAND_OK, "NOOP ok."));
	}

	private void sendResponse(PrintWriter out, FtpResponse response) {
		out.print(response.serialize());
		out.flush();
	}
}
