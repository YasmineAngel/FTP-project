package ftp.commands;
import ftp.model.UserSession;
import ftp.exceptions.FTPException;
import java.io.PrintWriter;

public class NoopCommand implements FTPCommand {
	@Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
        out.print("200 NOOP ok.\r\n");
        out.flush();
    }
}
