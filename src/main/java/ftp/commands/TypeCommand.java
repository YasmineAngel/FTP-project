package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;

public class TypeCommand implements FTPCommand {

    @Override
    public void execute(String argument, UserSession session, PrintWriter out) {
        if (argument == null || argument.isEmpty()) {
            out.println("501 Syntax error in parameters.");
            return;
        }

        switch (argument.toUpperCase()) {
            case "A" -> {
                session.setTransferType(UserSession.TransferType.ASCII);
                out.println("200 Type set to ASCII.");
            }

            case "I" -> {
                session.setTransferType(UserSession.TransferType.BINARY);
                out.println("200 Type set to BINARY.");
            }

            default -> out.println("504 Type not supported.");
        }
        out.flush();
    }
}