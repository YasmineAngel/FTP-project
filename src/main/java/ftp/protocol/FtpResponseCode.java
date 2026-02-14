package ftp.protocol;

public enum FtpResponseCode {
    DATA_CONNECTION_START(150, "Opening data connection"),
    DATA_TRANSFER_COMPLETE(226, "Data transfer complete"),
    SYSTEM_TYPE(215, "UNIX Type: L8"),
   
    COMMAND_OK(200, "Command okay."),
    SERVICE_READY(220, "Service ready for new user."),
    SERVICE_CLOSING(221, "Service closing control connection."),
    FILE_ACTION_OK(250, "Requested file action okay, completed."),
    PATHNAME_CREATED(257, "Pathname created."),
    ENTERING_PASSIVE(227, "Entering Passive Mode"),

    USERNAME_OK(331, "User name okay, need password."),
    NEED_ACCOUNT(332, "Need account for login."),
    RESTART_POSITION_MARKED(350, "Restart position accepted"),

    SERVICE_NOT_AVAILABLE(421, "Service not available."),
    CANNOT_OPEN_DATA(425, "Can't open data connection."),
    CONNECTION_CLOSED(426, "Connection closed; transfer aborted."),
    FILE_ACTION_NOT_TAKEN(450, "Requested file action not taken."),
    LOCAL_ERROR(451, "Local error in processing."),
    INSUFFICIENT_STORAGE(452, "Requested action not taken. Insufficient storage space in system."), 
    INITIALIZE_TRANSFER_MODE_FIRST(425, "Use PORT or PASV first."),

    SYNTAX_ERROR(500, "Syntax error, command unrecognized."),
    SYNTAX_ERROR_IN_ARGUMENTS(501, "Syntax error in parameters or arguments."),
    INVALID_ARGUMENTS(501, "Incorrect argument"),
    INVALID_TYPE(504, "Type not supported"),
    COMMAND_NOT_IMPLEMENTED(502, "Command not implemented."),
    BAD_SEQUENCE(503, "Bad sequence of commands."),

    NOT_LOGGED_IN(530, "Not logged in."),
    FILE_UNAVAILABLE(550, "Requested action not taken. File unavailable.");

    private final int code;
    private final String defaultMessage;

    FtpResponseCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
