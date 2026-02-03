package ftp.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

    private final Path rootDir;

    public PathUtils(String rootDir) {
        this.rootDir = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    /**
     * Resolves a user-visible FTP path against the server root.
     *
     * @param currentDir FTP path (e.g. "/", "/docs")
     * @param userInput argument from client (e.g. "..", "files")
     * @return resolved filesystem path
     */
    public Path resolve(String currentDir, String userInput) {
        Path base = rootDir.resolve(currentDir.substring(1)); // remove leading '/'

        Path target;
        if (userInput == null || userInput.isEmpty()) {
            target = base;
        } else {
            target = base.resolve(userInput);
        }

        return target.normalize();
    }

    /**
     * Converts a filesystem path back to an FTP-visible path.
     */
    public String toFtpPath(Path fsPath) {
        Path relative = rootDir.relativize(fsPath);
        return "/" + relative.toString().replace("\\", "/");
    }

    public Path getRootDir() {
        return rootDir;
    }
}