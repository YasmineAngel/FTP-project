package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

public class QuitCommand implements FTPCommand {

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
        FtpResponse response = new FtpResponse(FtpResponseCode.SERVICE_CLOSING);
        out.print(response.serialize());
        out.flush();
        session.disconnect();
    }
}
