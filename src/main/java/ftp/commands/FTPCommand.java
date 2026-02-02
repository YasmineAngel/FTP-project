package ftp.commands;

import ftp.model.UserSession;
import java.io.PrintWriter;

public interface FTPCommand {

    /**
     * Execute an FTP command.
     *
     * @param args    Arguments after the command (may be null)
     * @param session Current user session
     * @param out     Control connection writer
     */
    void execute(String args, UserSession session, PrintWriter out);
}
