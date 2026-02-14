package ftp.exceptions;

// For USER/PASS failures

import ftp.protocol.FtpResponseCode;

public class AuthException extends FTPException {
    private static final long serialVersionUID = 1L;
    public AuthException(String message) { super(FtpResponseCode.NOT_LOGGED_IN, message); }
}
