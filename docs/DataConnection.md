# Data Package

FTP uses two connections:
- **Control connection**: Commands and responses (port 2126)
- **Data connection**: File transfers and directory listings (dynamic port)

The three data classes handle the data connection operations:
- `PassiveServer`: Manages passive mode data connection setup
- `ListManager`: Sends directory listings over data connection
- `TransferManager`: Handles file uploads (STOR) and downloads (RETR)

## Architecture Flow

```
Client                    Control Connection              Data Connection
  |                              |                              |
  |-- PASV -------------------->|                              |
  |                              |-- Create PassiveServer       |
  |                              |-- Open random port           |
  |<-- 227 Entering PASV -------|                              |
  |                              |                              |
  |                              |<-- Client connects ----------|
  |-- LIST -------------------->|                              |
  |                              |-- Accept connection          |
  |                              |-- ListManager.sendDirectoryListing()
  |<-- 150 Opening data mode ----|                              |
  |                              |<-- Directory listing sent ---|
  |<-- 226 Transfer complete ----|                              |
```

## 1. UserSession Extension

First, extend `UserSession` to store the passive server instance:

```java
// In UserSession.java, add:

import ftp.data.PassiveServer;

// Add field:
private PassiveServer passiveServer;

// Add methods:
public PassiveServer getPassiveServer() {
    return passiveServer;
}

public void setPassiveServer(PassiveServer passiveServer) {
    this.passiveServer = passiveServer;
}

public void clearPassiveServer() {
    if (passiveServer != null) {
        try {
            passiveServer.close();
        } catch (IOException e) {
            // Log error if needed
        }
        passiveServer = null;
    }
}
```

## 2. PASV Command Implementation

The PASV command sets up passive mode for data connections.

**Location**: `src/main/java/ftp/commands/PasvCommand.java`



**Register in CommandExecutor**:
```java
commands.put("PASV", new PasvCommand());
```

## 3. LIST Command Implementation

The LIST command sends directory contents over the data connection.

**Location**: `src/main/java/ftp/commands/ListCommand.java`

```java

```

**Register in CommandExecutor** (requires PathUtils instance):
```java
// In ClientHandler, pass rootPath to CommandExecutor
// Or create PathUtils in CommandExecutor constructor
PathUtils pathUtils = new PathUtils(rootPath);
commands.put("LIST", new ListCommand(pathUtils));
```

## 4. RETR Command Implementation

The RETR command downloads a file from the server.

**Location**: `src/main/java/ftp/commands/RetrCommand.java`


## 5. STOR Command Implementation

The STOR command uploads a file to the server.

**Location**: `src/main/java/ftp/commands/StorCommand.java`



## 6. Complete CommandExecutor Integration

Update `CommandExecutor` to accept `PathUtils` and register all commands:


## 7. Command Flow Summary

### PASV → LIST Flow:
1. Client sends `PASV`
2. Server creates `PassiveServer`, opens port, responds with `227`
3. Client sends `LIST`
4. Server accepts data connection, calls `ListManager.sendDirectoryListing()`
5. Server closes data connection, responds `226`

### PASV → RETR Flow:
1. Client sends `PASV`
2. Server creates `PassiveServer`, responds `227`
3. Client sends `RETR filename`
4. Server validates path with `PathUtils`
5. Server accepts data connection, calls `TransferManager.sendFile()`
6. Server closes data connection, responds `226`

### PASV → STOR Flow:
1. Client sends `PASV`
2. Server creates `PassiveServer`, responds `227`
3. Client sends `STOR filename`
4. Server validates path with `PathUtils`
5. Server accepts data connection, calls `TransferManager.receiveFile()`
6. Server closes data connection, responds `226`

## 8. Important Notes

- **Always clean up**: Call `session.clearPassiveServer()` after each data transfer
- **Path validation**: Always use `PathUtils` to resolve and validate paths (security)
- **Error handling**: Always close data socket in `finally` block
- **Logging**: All operations are logged automatically by the data classes
- **Thread safety**: Each `UserSession` is per-client, so no thread safety issues

## 9. Testing

Test the integration:

```bash
# Start server
mvn exec:java

# In another terminal, test with FTP client
ftp localhost 2126
> USER anonymous
> PASS password
> PASV
> LIST
> RETR file.txt
> STOR upload.txt
> QUIT
```
