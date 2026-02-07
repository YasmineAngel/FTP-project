package ftp.exceptions;

//For Path Violations and missing files
public class UnavailableFileException extends FTPException {
 private static final long serialVersionUID = 1L;
 public UnavailableFileException(String message) { super(550, message); }
}