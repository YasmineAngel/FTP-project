package ftp.exceptions;

// For USER/PASS failures
public class AuthException extends FTPException {
    private static final long serialVersionUID = 1L;
    public AuthException(String message) { super(530, message); }
}
