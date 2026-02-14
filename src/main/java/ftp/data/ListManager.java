package ftp.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import ftp.exceptions.ConnectionException;
import ftp.exceptions.UnavailableFileException;
import ftp.utils.PathUtils;

public class ListManager {

    private static final DateTimeFormatter LIST_DATE =
            DateTimeFormatter.ofPattern("MMM dd HH:mm").withLocale(Locale.US).withZone(java.time.ZoneId.systemDefault());

    private static final String FALLBACK_OWNER = "owner";
    private static final String FALLBACK_GROUP = "group";

    /**
     * Line format: Unix-style (permissions, links, owner, group, size, date, name).
     *
     * @param dir        directory to list (must exist and be a directory)
     * @param dataSocket socket to write the listing to
     * @param pathUtils  optional; when non-null, used to validate dir (e.g. from command layer)
     * @throws UnavailableFileException if dir is null, does not exist, or is not a directory
     * @throws ConnectionException      if the socket is closed or write fails
     */
    public static void sendDirectoryListing(Path dir, Socket dataSocket, PathUtils pathUtils)
            throws UnavailableFileException, ConnectionException {

        if (pathUtils != null) {
            if (!pathUtils.isExistingDirectory(dir)) {
                throw new UnavailableFileException("Invalid directory path");
            }
        } else {
            if (dir == null || !Files.exists(dir) || !Files.isDirectory(dir)) {
                throw new UnavailableFileException("Invalid directory path");
            }
        }

        List<String> lines = buildDirectoryListing(dir);

        try (BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()))) {
            for (String line : lines) {
                writer.write(line);
            }
            writer.flush();
        } catch (IOException e) {
            throw new ConnectionException("Data connection error: " + e.getMessage());
        }
    }


    public static void sendDirectoryListing(Path dir, Socket dataSocket)
            throws UnavailableFileException, ConnectionException {
        sendDirectoryListing(dir, dataSocket, null);
    }

    private static List<String> buildDirectoryListing(Path dir) {
        try (Stream<Path> paths = Files.list(dir)) {
            return paths
                    .map(ListManager::formatListingLine)
                    .filter(line -> line != null)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

 
    private static String formatListingLine(Path path) {
        try {
            boolean isDir = Files.isDirectory(path);
            String permissions = getPermissions(path, isDir);
            long size = isDir ? 0 : Files.size(path);
            String name = path.getFileName().toString();
            String dateStr = formatModificationTime(Files.getLastModifiedTime(path));
            String owner = getOwnerName(path);
            String group = getGroupName(path);
            return String.format("%s 1 %s %s %10d %s %s\r\n",
                    permissions, owner, group, size, dateStr, name);
        } catch (IOException e) {
            return null;
        }
    }

    private static String getPermissions(Path path, boolean isDir) {
        try {
            PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);
            String posix = PosixFilePermissions.toString(attrs.permissions());
            return (isDir ? "d" : "-") + posix;
        } catch (IOException | UnsupportedOperationException e) {
            return isDir ? "drwxr-xr-x" : "-rw-r--r--";
        }
    }

    private static String formatModificationTime(FileTime fileTime) {
        return LIST_DATE.format(fileTime.toInstant());
    }

    private static String getOwnerName(Path path) {
        try {
            FileOwnerAttributeView view =
                    Files.getFileAttributeView(path, FileOwnerAttributeView.class);
            if (view != null) {
                String name = view.getOwner().getName();
                return name != null ? name : FALLBACK_OWNER;
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            // fall through
        }
        return FALLBACK_OWNER;
    }

    private static String getGroupName(Path path) {
        try {
            PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);
            String name = attrs.group().getName();
            return name != null ? name : FALLBACK_GROUP;
        } catch (IOException | UnsupportedOperationException e) {
            return FALLBACK_GROUP;
        }
    }
}
