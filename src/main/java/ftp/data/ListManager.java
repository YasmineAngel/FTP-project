package ftp.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import ftp.exceptions.ConnectionException;
import ftp.exceptions.UnavailableFileException;

public class ListManager {

    /**
     * Sends a directory listing over the data socket.
     * Line format: one char (d or -), spaces, size (number), spaces, filename.
     * @throws UnavailableFileException if dir is null, does not exist, or is not a directory
     * @throws ConnectionException if the socket is closed or write fails
     */
    public static void sendDirectoryListing(Path dir, Socket dataSocket) throws UnavailableFileException, ConnectionException {

        if (dir == null || !Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new UnavailableFileException("Invalid directory path");
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
                    } catch (IOException ignored) {
                        // Skip entry on error; outer catch handles socket/connection errors
                    }
                });
            }

            writer.flush();
        } catch (IOException e) {
            throw new ConnectionException("Data connection error: " + e.getMessage());
        }
    }
}