package ftp.exceptions;

public class TransferException extends FTPException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransferException(String message) {
        // Pass the 426 status code and the message to the parent
        super(426, message); 
    }
}