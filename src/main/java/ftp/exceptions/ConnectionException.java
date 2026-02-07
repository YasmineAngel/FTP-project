package ftp.exceptions;

//For PASV/PORT failures
public class ConnectionException extends FTPException {
 private static final long serialVersionUID = 1L;
 public ConnectionException(String message) { super(425, message); }
}
