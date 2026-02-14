package ftp.exceptions;

//For storage/write errors during STOR

import ftp.protocol.FtpResponseCode;

public class StorageException extends FTPException {
 private static final long serialVersionUID = 1L;
 public StorageException(String message) { super(FtpResponseCode.INSUFFICIENT_STORAGE, message); }
}