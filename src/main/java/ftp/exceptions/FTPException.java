package ftp.exceptions;

public abstract class FTPException extends Exception {
	private static final long serialVersionUID = 1L;
    private final int statusCode;

    public FTPException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}