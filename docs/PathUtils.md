# PathUtils Usage Guide

Utility class for secure FTP path resolution and validation.

## Initialization

```java
PathUtils pathUtils = new PathUtils("/path/to/ftp/root");
```

## Core Methods

### Resolve Relative Paths (CWD, LIST, RETR, STOR)

```java
// Resolve user input relative to current directory
Path fsPath = pathUtils.resolve(session.getCurrentDirectory(), userInput);

if (fsPath == null) {
    // Path traversal attempt - reject
    out.print("550 Invalid path.\r\n");
} else if (!pathUtils.exists(fsPath)) {
    out.print("550 File not found.\r\n");
} else if (pathUtils.isDirectory(fsPath)) {
    // Valid directory
    session.setCurrentDirectory(pathUtils.toFtpPath(fsPath));
} else {
    // Valid file
}
```

### Resolve Absolute Paths

```java
// For absolute FTP paths like "/files/doc.txt"
Path fsPath = pathUtils.resolveAbsolute("/files/doc.txt");
```

## Validation Methods

```java
pathUtils.isWithinRoot(path)  // Security check
pathUtils.exists(path)         // File exists
pathUtils.isDirectory(path)    // Is directory
pathUtils.isFile(path)         // Is regular file
```

## Security

All methods automatically prevent directory traversal attacks. Paths outside the root directory return `null` or fail validation.
