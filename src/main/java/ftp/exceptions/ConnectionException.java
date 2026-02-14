package ftp.exceptions;

//For PASV/PORT failures

import ftp.protocol.FtpResponseCode;

public class ConnectionException extends FTPException {
 private static final long serialVersionUID = 1L;
 public ConnectionException(String message) { super(FtpResponseCode.CANNOT_OPEN_DATA, message); }
}
