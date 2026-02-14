package ftp.exceptions;

//For missing arguments

import ftp.protocol.FtpResponseCode;

public class SyntaxException extends FTPException {
 private static final long serialVersionUID = 1L;
 public SyntaxException(String message) { super(FtpResponseCode.SYNTAX_ERROR_IN_ARGUMENTS, message); }
}