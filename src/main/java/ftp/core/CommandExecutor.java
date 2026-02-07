package ftp.core;

import ftp.commands.*;
import ftp.exceptions.FTPException;
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
    	try {
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
	    } catch (FTPException e) {
	        // catches ALL our custom exceptions (501, 503, 530, 550, etc.)
	        logger.error("Protocol Error ({}): {}", e.getStatusCode(), e.getMessage());
	        
	        // We send the code and message directly to the client
	        out.print(e.getStatusCode() + " " + e.getMessage() + "\r\n");
	        out.flush();
	        
	    } catch (Exception e) {
	        // catches unexpected system crashes (NullPointer, etc.)
	        logger.error("Internal Server Error", e);
	        out.print("451 Local error in processing.\r\n");
	        out.flush();
	    }
	}
}