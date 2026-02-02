package ftp;
import ftp.core.FTPServer;

public class Main {
    public static void main(String[] args) {
        int port = 2126;
        String rootPath = "./ftp-root"; 
        
        FTPServer server = new FTPServer(port, rootPath);
        server.start();
    }
}