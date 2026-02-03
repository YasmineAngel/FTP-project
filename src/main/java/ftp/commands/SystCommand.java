package ftp.commands;

import java.io.PrintWriter;

import ftp.model.UserSession;

public class SystCommand implements FTPCommand {

    @Override
    public void execute(String argument, UserSession session, PrintWriter out) {
        out.println("215 UNIX Type: L8");
        out.flush();
    }
}