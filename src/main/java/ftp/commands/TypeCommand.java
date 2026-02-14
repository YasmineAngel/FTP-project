package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;
import ftp.protocol.FtpResponse;
import ftp.protocol.FtpResponseCode;

public class TypeCommand implements FTPCommand {

    @Override
    public void execute(String argument, UserSession session, PrintWriter out) {
        if (argument == null || argument.isEmpty()) {
            FtpResponse response = new FtpResponse(
                FtpResponseCode.SYNTAX_ERROR_IN_ARGUMENTS
            );

            out.print(response.serialize());
            out.flush();
            return;
        }
       
        FtpResponse response;
        switch (argument.toUpperCase()) {
            case "A" -> {
                session.setTransferType(UserSession.TransferType.ASCII);
                 response = new FtpResponse(
                    FtpResponseCode.COMMAND_OK,
                    "Type set to ASCII"
                );

            }

            case "I" -> {
                session.setTransferType(UserSession.TransferType.BINARY);
                response = new FtpResponse(
                    FtpResponseCode.COMMAND_OK,
                    "Type set to BINARY"
                );
            }

            default ->  response = new FtpResponse(
                    FtpResponseCode.INVALID_TYPE
                );
        }
         out.print(response.serialize());
        out.flush();
    }
}