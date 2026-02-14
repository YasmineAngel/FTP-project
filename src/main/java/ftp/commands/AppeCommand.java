package ftp.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

import ftp.data.TransferManager;
import ftp.exceptions.ConnectionException;
import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;
import ftp.utils.PathUtils;

public class AppeCommand implements FTPCommand {

	private final PathUtils pathUtils;

	public AppeCommand(PathUtils pathUtils) {
		this.pathUtils = pathUtils;
	}

	@Override
	public void execute(String args, UserSession session, PrintWriter out) throws ftp.exceptions.FTPException {
		if (!session.isLoggedIn()) {
			sendResponse(out, new FtpResponse(FtpResponseCode.NOT_LOGGED_IN));
			return;
		}

		if (args == null || args.trim().isEmpty()) {
			sendResponse(out, new FtpResponse(FtpResponseCode.SYNTAX_ERROR_IN_ARGUMENTS));
			return;
		}

		boolean isPassiveMode = session.getPassiveServer() != null;
		boolean isPortMode = session.hasPortMode();

		if (!isPassiveMode && !isPortMode) {
			sendResponse(out, new FtpResponse(FtpResponseCode.CANNOT_OPEN_DATA));
			return;
		}

		Path filePath = pathUtils.resolve(session.getCurrentDirectory(), args);

		if (filePath == null || !pathUtils.isWithinRoot(filePath)) {
			sendResponse(out, new FtpResponse(FtpResponseCode.FILE_UNAVAILABLE, "Invalid path."));
			return;
		}

		String mode = session.getTransferType() == UserSession.TransferType.ASCII ? "ASCII" : "BINARY";
		out.print("150 Opening " + mode + " mode data connection for " + args + " (append).\r\n");
		out.flush();
		boolean socketOpened = false;
		Socket dataSocket = null;
		try {
			if (isPassiveMode) {
				dataSocket = session.getPassiveServer().getServerSocket().accept();
			} else {
				dataSocket = new Socket(session.getPortModeAddress(), session.getPortMode());
			}
			socketOpened = true;
			session.setActiveDataSocket(dataSocket);

			TransferManager.receiveFile(filePath, dataSocket, session.getRestartMarker(), true);
			sendResponse(out, new FtpResponse(FtpResponseCode.FILE_ACTION_OK, "Transfer complete."));

		} catch (IOException e) {
			if (socketOpened && session.getActiveDataSocket() == null) {
				sendResponse(out, new FtpResponse(FtpResponseCode.CONNECTION_CLOSED));
			} else {
				sendResponse(out,
						new FtpResponse(FtpResponseCode.CANNOT_OPEN_DATA, "Data connection error: " + e.getMessage()));
			}
			throw new ConnectionException("Append interrupted: " + e.getMessage());
		} finally {
			try {
				if (dataSocket != null) {
					dataSocket.close();
				}
				session.setActiveDataSocket(null);
				if (isPassiveMode) {
					session.clearPassiveServer();
				} else {
					session.clearPortMode();
				}
			} catch (IOException | ConnectionException e) {
			}
		}
	}

	private void sendResponse(PrintWriter out, FtpResponse response) {
		out.print(response.serialize());
		out.flush();
	}
}