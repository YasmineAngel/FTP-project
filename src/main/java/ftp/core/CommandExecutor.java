package ftp.core;

import ftp.commands.*;
import ftp.model.UserSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ftp.utils.PathUtils;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private static final Logger logger = LogManager.getLogger(CommandExecutor.class);
    private final Map<String, FTPCommand> commands = new HashMap<>();
    private final PathUtils pathUtils ;

    public CommandExecutor(String rootPath) {
    	this.pathUtils = new PathUtils(rootPath);
        commands.put("USER", new UserCommand());
        commands.put("PASS", new PassCommand());
        commands.put("PWD", new PwdCommand());
        commands.put("SYST", new SystCommand());
        commands.put("TYPE", new TypeCommand());
        commands.put("LIST", new ListCommand(pathUtils));
        commands.put("PASV", new PasvCommand());
        commands.put("RETR", new RetrCommand(pathUtils));
        commands.put("STOR", new StorCommand(pathUtils));
        

    }

    public void execute(String line, UserSession session, PrintWriter out) {
        if (line == null) {
            session.disconnect();
            return;
        }

        logger.info("Received command: {}", line);

        // Split the line into command name and arguments (e.g., "USER anonymous")
        String[] parts = line.trim().split("\\s+", 2);
        String commandName = parts[0].toUpperCase();
        String args = (parts.length > 1) ? parts[1] : null;

        FTPCommand command = commands.get(commandName);

        if (command != null) {
            command.execute(args, session, out);
        } else {
            logger.warn("Unknown command: {}", commandName);
            out.print("502 Command not implemented.\r\n");
            out.flush();
        }
    }
}