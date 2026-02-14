package ftp.protocol;

public class FtpResponse {

    private final FtpResponseCode code;
    private final String message;

    // 1️⃣ Constructor with custom message
    public FtpResponse(FtpResponseCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public FtpResponse(FtpResponseCode code) {
        this.code = code;
        this.message = code.getDefaultMessage();
    }

    public String serialize() {
        return code.getCode() + " " + message + "\r\n";
    }
}
