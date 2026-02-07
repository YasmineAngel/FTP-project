package ftp.exceptions;

//For storage/write errors during STOR
public class StorageException extends FTPException {
 private static final long serialVersionUID = 1L;
 public StorageException(String message) { super(452, message); }
}