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

## Command Flow Summary

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
