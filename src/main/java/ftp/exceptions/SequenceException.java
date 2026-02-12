package ftp.exceptions;

public class SequenceException extends FTPException {

    public SequenceException(String message) {
        super(503, message);
    }
}
