
package ftp;
import ftp.core.FTPServer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "ftp-server", mixinStandardHelpOptions = true)
public class Main implements Runnable {

    @Option(
        names = "--log-level",
        description = "Logging level: TRACE, DEBUG, INFO, WARN, ERROR",
        defaultValue = "INFO"
    )
    String logLevel;

    @Option(
        names = "--port",
        description = "Port to run the server on",
        defaultValue = "2126"
    )
    int port;
   
    public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
    }

    @Override
    public void run() {

        
        System.setProperty("logLevel", logLevel.toUpperCase());
        String rootPath = "./ftp-root"; 
        
        FTPServer server = new FTPServer(this.port, rootPath);
        server.start();
    }
}