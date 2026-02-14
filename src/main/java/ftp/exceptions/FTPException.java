package ftp.exceptions;

import ftp.protocol.FtpResponseCode;

public abstract class FTPException extends Exception {

    private static final long serialVersionUID = 1L;

    private final FtpResponseCode responseCode;

    public FTPException(FtpResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public FtpResponseCode getResponseCode() {
        return responseCode;
    }
}
