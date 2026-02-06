package ftp.exceptions;

//For missing arguments
public class SyntaxException extends FTPException {
 private static final long serialVersionUID = 1L;
 public SyntaxException(String message) { super(501, message); }
}