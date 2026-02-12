package ftp.model;
import java.io.IOException;

import  ftp.data.PassiveServer;
/**
 * Represents the state of a connected FTP client.
 * This object is created once per connection and shared
 * across command executions.
 */
public class UserSession {

    // Authentication state
    private boolean loggedIn = false;
    private String username;
    private PassiveServer passiveServer;
    private String renameFrom;

    // Navigation state
    private String currentDirectory = "/";

    // Transfer settings
    private TransferType transferType = TransferType.ASCII;

    // Connection state
    private boolean connected = true;


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassiveServer(PassiveServer passiveServer) {
        this.passiveServer = passiveServer;
    }
    
    public boolean hasUsername() {
        return this.username != null;
    }
    
    /* =======================
       Authentication
       ======================= */

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void login(String username) {
        this.username = username;
        this.loggedIn = true;
    }

    public void logout() {
        this.loggedIn = false;
        this.username = null;
        this.currentDirectory = "/";
    }

    public String getUsername() {
        return username;
    }

    public PassiveServer getPassiveServer() {
        return passiveServer;
    }

    /* =======================
       Directory Handling
       ======================= */

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    /* =======================
       Transfer Mode
       ======================= */

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    /* =======================
       Connection State
       ======================= */

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        this.connected = false;
    }

    public void clearPassiveServer() throws IOException, ftp.exceptions.ConnectionException {
        if (passiveServer != null) {
            passiveServer.close();
            passiveServer = null;
        }
    }

    public void setRenameFrom(String path) {
        this.renameFrom = path;
    }
    
    public String getRenameFrom() {
        return renameFrom;
    }
    
    public void clearRenameFrom() {
        this.renameFrom = null;
    }
    
    /* =======================
       Helper Enum
       ======================= */

    public enum TransferType {
        ASCII,
        BINARY
    }
}
