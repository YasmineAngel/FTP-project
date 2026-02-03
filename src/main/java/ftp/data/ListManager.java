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
            logger.error("Cannot list directory: path is null, does not exist, or is not a directory");
            throw new IOException("Invalid directory path");
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()))) {
            try (Stream<Path> paths = Files.list(dir)) {
                paths.forEach(path -> {
                    try {
                        String type = Files.isDirectory(path) ? "d" : "-";
                        long size = Files.size(path);
                        String name = path.getFileName().toString();
                        writer.write(String.format("%s %10d %s\r\n", type, size, name));
                    } catch (IOException e) {
                        logger.error("Error processing file in directory listing: {}", path, e);
                    }
                });
            }
            writer.flush();
            logger.info("Directory listing sent successfully for: {}", dir);
        }
    }
}