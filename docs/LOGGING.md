# FTP Server Logging Specification

This document defines the required logging format for the FTP server.
All `System.out.println` statements must be replaced with Log4j2 logging
after branch merge.



## Logger Declaration (Required)

Each class that logs must declare a logger:

```java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

private static final Logger logger =
        LogManager.getLogger(ClassName.class);

```

## Usage
```java

// client connection
logger.info("Client connected from {}", socket.getRemoteSocketAddress());
logger.info("Client disconnected from {}", socket.getRemoteSocketAddress());

// FTP protocol Logs
logger.info("C -> S: {}", commandLine);
logger.info("S -> C: {}", response);

// Obvious but some security reminders
// DON'T LOG passwords
logger.info("C -> S: PASS ******"); 
```