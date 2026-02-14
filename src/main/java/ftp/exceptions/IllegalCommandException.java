package ftp.exceptions;

//For sequence errors

import ftp.protocol.FtpResponseCode;

public class IllegalCommandException extends FTPException {
 private static final long serialVersionUID = 1L;
 public IllegalCommandException(String message) { super(FtpResponseCode.BAD_SEQUENCE, message); }
}
