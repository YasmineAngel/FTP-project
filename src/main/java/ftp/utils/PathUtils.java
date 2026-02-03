package ftp.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

    private final Path rootDir;

    public PathUtils(String rootDir) {
        this.rootDir = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    /**
     * Resolves a user-visible FTP path against the server root.
     * Ensures the resolved path stays within the root directory (security).
     *
     * @param currentDir FTP path (e.g. "/", "/docs")
     * @param userInput argument from client (e.g. "..", "files")
     * @return resolved filesystem path, or null if path escapes root directory
     */
    public Path resolve(String currentDir, String userInput) {
        // Handle empty or null currentDir
        if (currentDir == null || currentDir.isEmpty()) {
            currentDir = "/";
        }
        
        // Remove leading '/' and resolve base path
        String dirPath = currentDir.startsWith("/") ? currentDir.substring(1) : currentDir;
        Path base = rootDir.resolve(dirPath);

        Path target;
        if (userInput == null || userInput.isEmpty()) {
            target = base;
        } else {
            target = base.resolve(userInput);
        }

        Path normalized = target.normalize();
        
        // Security check: ensure path is within root directory
        if (!normalized.startsWith(rootDir)) {
            return null; // Path traversal attempt detected
        }
        
        return normalized;
    }

    /**
     * Converts a filesystem path back to an FTP-visible path.
     */
    public String toFtpPath(Path fsPath) {
        if (fsPath == null) {
            return "/";
        }
        
        try {
            Path relative = rootDir.relativize(fsPath);
            String ftpPath = "/" + relative.toString().replace("\\", "/");
            // Handle root directory case
            return ftpPath.equals("/.") ? "/" : ftpPath;
        } catch (IllegalArgumentException e) {
            // Path is not relative to root, return root
            return "/";
        }
    }

    /**
     * Validates that a path is within the root directory.
     *
     * @param path the path to validate
     * @return true if path is within root, false otherwise
     */
    public boolean isWithinRoot(Path path) {
        if (path == null) {
            return false;
        }
        Path normalized = path.normalize();
        return normalized.startsWith(rootDir);
    }

    /**
     * Checks if a path exists in the filesystem.
     *
     * @param path the path to check
     * @return true if path exists, false otherwise
     */
    public boolean exists(Path path) {
        return path != null && Files.exists(path);
    }

    /**
     * Checks if a path is a directory.
     *
     * @param path the path to check
     * @return true if path is a directory, false otherwise
     */
    public boolean isDirectory(Path path) {
        return path != null && Files.isDirectory(path);
    }

    /**
     * Checks if a path is a regular file.
     *
     * @param path the path to check
     * @return true if path is a file, false otherwise
     */
    public boolean isFile(Path path) {
        return path != null && Files.isRegularFile(path);
    }

    /**
     * Gets the parent directory of a path.
     * Useful for CDUP command.
     *
     * @param path the path
     * @return parent path, or rootDir if already at root, or null if invalid
     */
    public Path getParent(Path path) {
        if (path == null) {
            return null;
        }
        
        Path normalized = path.normalize();
        
        // If already at root, return root
        if (normalized.equals(rootDir)) {
            return rootDir;
        }
        
        Path parent = normalized.getParent();
        
        // Ensure parent is still within root
        if (parent != null && isWithinRoot(parent)) {
            return parent;
        }
        
        return rootDir; // Default to root if parent is outside
    }

    /**
     * Resolves a path relative to root (for absolute FTP paths like "/files/doc.txt").
     *
     * @param ftpPath absolute FTP path (must start with "/")
     * @return resolved filesystem path, or null if invalid or outside root
     */
    public Path resolveAbsolute(String ftpPath) {
        if (ftpPath == null || !ftpPath.startsWith("/")) {
            return null;
        }
        
        String relativePath = ftpPath.substring(1); // Remove leading '/'
        Path target = rootDir.resolve(relativePath).normalize();
        
        // Security check
        if (!isWithinRoot(target)) {
            return null;
        }
        
        return target;
    }

    public Path getRootDir() {
        return rootDir;
    }
}