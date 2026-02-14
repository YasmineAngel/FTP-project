package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;

import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

public class SystCommand implements FTPCommand {

    @Override
    public void execute(String argument, UserSession session, PrintWriter out) {
        FtpResponse response = new FtpResponse(FtpResponseCode.SYSTEM_TYPE);
        out.print(response.serialize());
        out.flush();
    }
}