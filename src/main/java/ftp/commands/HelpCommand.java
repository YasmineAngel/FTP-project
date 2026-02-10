package ftp.commands;

import ftp.model.UserSession;
import ftp.exceptions.FTPException;
import java.io.PrintWriter;

public class HelpCommand implements FTPCommand {
    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        out.print("214- The following commands are recognized:\r\n");
        out.print(" USER PASS PWD CWD CDUP LIST PASV RETR\r\n");
        out.print(" STOR TYPE SYST QUIT REIN ABOR HELP\r\n");
        out.print("214 Help OK.\r\n");
        out.flush();
    }
}