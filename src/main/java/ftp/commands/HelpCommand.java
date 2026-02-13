package ftp.commands;

import ftp.model.UserSession;
import ftp.exceptions.FTPException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class HelpCommand implements FTPCommand {
	private static final Map<String, String> COMMAND_DESCS = new TreeMap<>();

    static {
        COMMAND_DESCS.put("USER", "USER <username> - Specify user identity");
        COMMAND_DESCS.put("PASS", "PASS <password> - Specify user password");
        COMMAND_DESCS.put("PWD",  "PWD - Print current working directory");
        COMMAND_DESCS.put("CWD",  "CWD <path> - Change working directory");
        COMMAND_DESCS.put("CDUP", "CDUP - Change to parent directory");
        COMMAND_DESCS.put("LIST", "LIST [<path>] - List files in directory");
        COMMAND_DESCS.put("PASV", "PASV - Enter passive mode");
        COMMAND_DESCS.put("PORT", "PORT <address> - Enter active mode");
        COMMAND_DESCS.put("RETR", "RETR <filename> - Retrieve (download) a file");
        COMMAND_DESCS.put("STOR", "STOR <filename> - Store (upload) a file");
        COMMAND_DESCS.put("APPE", "APPE <filename> - Append to a file");
        COMMAND_DESCS.put("REST", "REST <marker> - Set restart marker for transfer");
        COMMAND_DESCS.put("TYPE", "TYPE <mode> - Set transfer type (A=ASCII, I=Binary)");
        COMMAND_DESCS.put("SYST", "SYST - Return system type");
        COMMAND_DESCS.put("QUIT", "QUIT - Terminate the session");
        COMMAND_DESCS.put("REIN", "REIN - Reinitialize the session");
        COMMAND_DESCS.put("ABOR", "ABOR - Abort the current file transfer");
        COMMAND_DESCS.put("MKD",  "MKD <path> - Make directory");
        COMMAND_DESCS.put("RMD",  "RMD <path> - Remove directory");
        COMMAND_DESCS.put("DELE", "DELE <filename> - Delete file");
        COMMAND_DESCS.put("RNFR", "RNFR <path> - Rename from (source)");
        COMMAND_DESCS.put("RNTO", "RNTO <path> - Rename to (destination)");
        COMMAND_DESCS.put("NOOP", "NOOP - No operation");
    }
    @Override
    public void execute(String args, UserSession session, PrintWriter out) throws FTPException {
    	if (args != null && !args.trim().isEmpty()) {
            String cmd = args.trim().toUpperCase();
            if (COMMAND_DESCS.containsKey(cmd)) {
                out.print("214 " + COMMAND_DESCS.get(cmd) + "\r\n");
            } else {
                out.print("504 Help not available for: " + cmd + "\r\n");
            }
        } else {
            out.print("214-The following commands are recognized:\r\n");
            out.print("   Access:   USER, PASS, REIN, QUIT\r\n");
            out.print("   Transfer: PASV, PORT, TYPE, MODE, STRU\r\n");
            out.print("   File I/O: RETR, STOR, APPE, REST, ABOR\r\n");
            out.print("   Filesys:  CWD, CDUP, PWD, LIST, MKD, RMD, DELE, RNFR, RNTO\r\n");
            out.print("   Misc:     HELP, NOOP, SYST\r\n");
            out.print("214 Help OK.\r\n");
        }
        out.flush();
    }
}