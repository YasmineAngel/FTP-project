package ftp.exceptions;

//For Path Violations and missing files

import ftp.protocol.FtpResponseCode;

public class UnavailableFileException extends FTPException {
 private static final long serialVersionUID = 1L;
 public UnavailableFileException(String message) { super(FtpResponseCode.FILE_UNAVAILABLE, message); }
}