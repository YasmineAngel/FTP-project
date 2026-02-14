# Data Package

FTP uses two connections:
- **Control connection**: Commands and responses (default port 2126)
- **Data connection**: File transfers and directory listings (dynamic port)

The three data classes handle the data connection operations:
- `PassiveServer`: Manages passive mode data connection setup
- `ListManager`: Sends directory listings over data connection
- `TransferManager`: Handles file uploads (STOR) and downloads (RETR)

_active mode architecture flow_
```
Client                    Control Connection                         Data Connection
  |                              |                                        |
  |--- PORT -------------------->|                                        |
  |   (client IP + data port)    |-- Store address, clear PASV            |
  |<--- 200 PORT successful -----|                                        |
  |                              |                                        |
  |--- LIST -------------------->|                                        |
  |                              |-- Connect to client (address:port) --->|
  |                              |-- ListManager.sendDirectoryListing()   |
  |<-- 150 Opening data mode ----|                                        |
  |                              |<-- Directory listing sent -------------|
  |<-- 226 Transfer complete ----|                                        |
```

_passive mode architecture flow_
```
Client                    Control Connection                         Data Connection
  |                              |                                        |
  |--- PASV -------------------->|                                        |
  |                              |-- Create PassiveServer                 |
  |                              |-- Open random port                     |
  |<--- 227 Entering PASV -------|                                        |
  |                              |                                        |
  |                              |<-- Client connects --------------------|
  |--- LIST -------------------->|                                        |
  |                              |-- Accept connection                    |
  |                              |-- ListManager.sendDirectoryListing()   |
  |<-- 150 Opening data mode ----|                                        |
  |                              |<-- Directory listing sent -------------|
  |<-- 226 Transfer complete ----|                                        |
```