package ftp.exceptions;

public class FileSystemException extends FTPException {

    public FileSystemException(String message, int statusCode) {
        super(statusCode, message);
    }
}
