# FTP Server Implementation With Java
# Yasmine Kaouther Boudour, Hanan Miftah Agraw, Blen Assefa Tilahun 14/02/26
![Java 17](https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)
![JUnit 5](https://img.shields.io/badge/Tested_with-JUnit_5-25A162?logo=java)
![JaCoCo](https://img.shields.io/badge/Coverage-77%25-yellowgreen)
![FTP Protocol](https://img.shields.io/badge/Protocol-FTP_(RFC_959)-blue)
## Introduction

This project implements an FTP server with a file structure starting at [ftp-root](ftp-root/) folder. The server follows most [RFC 959](http://abcdrfc.free.fr/rfc-vf/pdf/rfc959.pdf) protocols enabling a client to connect to it, list its file contents, traversing through it's file structure, uploading new contents, downloading its contents, creating new directories, deleting existing directories/files, abort connection or transfers, and many more. In addition, The server supports both PASV and ACTIVE connection transfer modes from the client. The project is implemented with Java programming language and uses maven for build and test automation.


## Build and Run Instructions

### Prerequisites
- Java Development Kit (JDK) version 17
- Apache Maven version 3.x

For all the following instructions make sure to be at the root level next to Readme.md and src/ folder

### Build the Project
```shell
mvn clean package
```

### Execute the Generated Jar File
```shell
java -jar target/ftp-server-1.0.0.jar
```

### Run Tests
```shell
mvn test
```
## Server Configuration

By default the server:
- Runs on port 2121
- Uses ftp_root/ as the root directory
- Supports anonymous login

You can configure runtime options using:
```shell
java -jar target/ftp-server-1.0.0.jar [--port <2121>]  [--log-level <INFO | DEBUG | Warning>]
```

## Project setup

### Folder Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ftpproject/
│   │   │       ├── Main.java                <-- Starts the Server
│   │   │       ├── core/                    <-- Yasmine's Domain
│   │   │       │   ├── FTPServer.java       <-- Socket Accept Loop
│   │   │       │   ├── ClientHandler.java   <-- Thread management
│   │   │       │   └── CommandExecutor.java <-- Command Dispatcher
│   │   │       ├── commands/                <-- Hanan's Domain
│   │   │       │   ├── FTPCommand.java      <-- Interface (The "Contract")
│   │   │       │   ├── UserCommand.java
│   │   │       │   ├── PassCommand.java
│   │   │       │   └── CwdCommand.java
│   │   │       ├── data/                    <-- Blen's Domain
│   │   │       │   ├── PassiveServer.java   <-- PASV Logic
│   │   │       │   ├── ListManager.java     <-- Directory Listing
│   │   │       │   └── TransferManager.java <-- File Upload/Download
│   │   │       ├── model/                   <-- Shared Data
│   │   │       │   └── UserSession.java     <-- Session State Tracking
│   │   │       ├── exceptions/              <-- Shared Logic
│   │   │       │   └── FTPException.java    <-- Custom Exceptions
│   │   │       └── utils/
│   │   │           └── PathUtils.java       
│   │   └── resources/
│   │       └── log4j2.xml                    <-- Logging Config
├── ftp_root/                                 <-- Server's file storage(root directory)
├── pom.xml                                   <-- Maven Configuration
└── README.md

```

## Supported Commands

USER, PASS, QUIT  
PWD, CWD, CDUP  
PASV, PORT  
LIST  
RETR, STOR, REST 
MKD, RMD, DELE  
RNFR, RNTO  
TYPE,   
REIN, ABOR  
HELP, NOOP, SYST  

## Code Samples

## UML diagram
![UML_diagram](docs/Class%20Diagram%20UML.png)

## Design Decisions

The entire development process took 4 milestones

**Milestone 1**:  Create an extremely simple FTP server 
- First packaging structure draft and core package implementation
- Logging feature added to verify recieved commands and sent responses
- User session model design and FTPCommand interface implementation

_Design choices at this milestone_
- The team decided to implement mainly 4 packages  
    - [core](src/main/java/ftp/core/) > serves as the front package that does interaction with the client requests/commands
    - [commands](src/main/java/ftp/commands/) > contains the FTPCommand interface and its implemenations
    - [model](src/main/java/ftp/model/) > contains the user session model
    - [data](src/main/java/ftp/data/) > concerns with file level implementations on the [ftp-root](ftp-root/) folder 
- The team decided to include transferType attribute in the UserSession model, to handle different type transfer requests from client
- Used ExecutorService in FTPServer to handle multiple clients concurrently in their own threads

**Milestone 2**: Implementation of major commands, and Exception interface
- CommandExecuter dispatcher implementation to handle commands 
- PathUtils function imlementaion to facilitate path related functinalites 
- More commands implemntations including PASV, LIST, STOR, RETR, CWD, CDUP, PWD
- First sample test implementation

_Design choice at this milestone_
- The team decided to start testing commands and implementations
- The ListManager and ListCommand had some issues when it comes to retriving file format. 
    - FileZila was expecting unix like structure and the implementation at the time didn't support that.
    - The team decided to do Filezila tests in addition to unit tests with mock libraries
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

**Milestone 3**: Implementation of additional commands, test coverage and asking for progress review
- More command implementation after thorough review of [RFC 959](http://abcdrfc.free.fr/rfc-vf/pdf/rfc959.pdf)
- Concurency issue handeling with [FileLockManager.java](src/main/java/ftp/utils/FileLockManager.java)
- More unit test implementation together with the [jacoco plugin](https://mvnrepository.com/artifact/org.jacoco/jacoco-maven-plugin) for analysing test coverage  of 77 %
- Active mode transfer implementation

_Design choice at this milestone_
- The team identified an issue that occures when more than one client is connected to the server and sends contradicting commands. (for example, when one is trying to downloand a file and the other client deletes it)
- The team decided to implement FileLockManager to create instance for each target path and critical actions (delete, rename, mkdir, transfer) will only be granted access once that instance is no longer locked


**Milestone 4**: code clean up, documentation
- Classify logging levels deeper to TRACE, DEBUG, INFO, WARN, ERROR
- Implement an Object for messages sent to clients
- Use [PicoCLI](https://picocli.info/) to accept user options like --debug, --verbose, --suppress-warning
- Implement FileLockManager to more critical sections
- Remove nested Exceptions
- Remove unnecessary comments
- Add Readme documentation
- Add Video demo

##  Task Classification
For all four milestones each team member contributed to the codebase. In general the structure of task classification is as follows:

**Yasmine:** Project Structure, Core Package, Exceptions, some commands, video demo

**Hanan:** Commands, Test Coverage, UserSession model, File lock manager

**Blen:** Logging, Data Package, PathUtils, Documentation, some commands

And each member was responsible for testing on filezila and writing unit tests for logics that they implemented.


