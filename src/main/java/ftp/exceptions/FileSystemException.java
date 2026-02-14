package ftp.exceptions;

import ftp.protocol.FtpResponseCode;

public class FileSystemException extends FTPException {
    
    public FileSystemException(String message) {
        super(FtpResponseCode.FILE_UNAVAILABLE, message);
    }
}
