package ftp.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ListManager {

    private static final Logger logger = LogManager.getLogger(ListManager.class);

    public static void sendDirectoryListing(Path dir, Socket dataSocket) throws IOException {

        if (dir == null || !Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IOException("Invalid directory path");
        }
    
        try (BufferedWriter writer =
                 new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()))) {
    
            try (Stream<Path> paths = Files.list(dir)) {
                paths.forEach(path -> {
                    try {
                        boolean isDir = Files.isDirectory(path);
    
                        String permissions = isDir
                                ? "drwxr-xr-x"
                                : "-rw-r--r--";
    
                        long size = isDir ? 0 : Files.size(path);
                        String name = path.getFileName().toString();
    
                        String line = String.format(
                                "%s 1 owner group %10d Feb 10 15:30 %s\r\n",
                                permissions, size, name
                        );
    
                        writer.write(line);
    
                    } catch (IOException ignored) {}
                });
            }
    
            writer.flush();
        }
    }
    
}