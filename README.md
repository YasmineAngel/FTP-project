# FTP Server Implementation With Java
# Yasmine Kaouther Boudour, Hanan Miftah Agraw, Blen Assefa Tilahun 14/02/26

## Introduction

This project implements an FTP server with a file structure starting at [ftp-root](ftp-root/) folder. The server follows most [RFC 959](http://abcdrfc.free.fr/rfc-vf/pdf/rfc959.pdf) protocols enabling a client to connect to it, list its file contents, traversing through it's file structure, uploading new contents, downloading its contents, creating new directories, deleting existing directories/files, abort connection or transfers, and many more. In addition, The server supports both PASV and ACTIVE connection transfer modes from the client. The project is implemented with Java programming language and uses maven for build and test automation.


## Build and Run Instructions

### Prerequisites
- Java Development Kit (JDK) version 17
- Apache Maven version 3.x

### Build the Project
```bash
mvn clean package
```

### Execute the Generated Jar File
```bash
java -jar target/ftp-server-1.0.0.jar
```

