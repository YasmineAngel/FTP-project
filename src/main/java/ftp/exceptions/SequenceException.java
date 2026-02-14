package ftp.exceptions;

import ftp.protocol.FtpResponseCode;

public class SequenceException extends FTPException {

    public SequenceException(String message) {
        super(FtpResponseCode.BAD_SEQUENCE, message);
    }
}
