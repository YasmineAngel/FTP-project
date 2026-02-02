package ftp.commands;

import ftp.model.UserSession;
import java.io.PrintWriter;

public class UserCommand implements FTPCommand {

    @Override
    public void execute(String args, UserSession session, PrintWriter out) {
        if (args == null || args.isBlank()) {
            out.println("501 Syntax error in parameters.");
            return;
        }

        session.logout(); // reset any previous state
        session.setUsername(args); // temporary login step (password comes next)

        out.println("331 Username ok, need password.");
    }
}
