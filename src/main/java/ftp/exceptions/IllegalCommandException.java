package ftp.exceptions;

//For sequence errors
public class IllegalCommandException extends FTPException {
 private static final long serialVersionUID = 1L;
 public IllegalCommandException(String message) { super(503, message); }
}
