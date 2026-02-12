package ftp.model;
import java.io.IOException;
import java.net.InetAddress;

import ftp.data.PassiveServer;
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

    private InetAddress portModeAddress;
    private int portModePort;

    private long restartMarker = 0;

    // Navigation state
    private String currentDirectory = "/";

    // Transfer settings
    private TransferType transferType = TransferType.ASCII;

    // Connection state
    private boolean connected = true;


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPortMode(int portModePort, InetAddress portModePortAddress) {
        this.portModePort = portModePort;
        this.portModeAddress = portModePortAddress;
    }

    public void setRestartMarker(long restartMarker) {
        this.restartMarker = restartMarker;
    }

    public void setPassiveServer(PassiveServer passiveServer) {
        this.passiveServer = passiveServer;
    }
    
    public boolean hasUsername() {
        return this.username != null;
    }

    public boolean hasPortMode() {
        return this.portModeAddress != null && portModePort > 0;
    }

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

    public int getPortMode() {
        return portModePort;
    }

    public InetAddress getPortModeAddress() {
        return portModeAddress;
    }


    public PassiveServer getPassiveServer() {
        return passiveServer;
    }

    public long getRestartMarker() {
        return restartMarker;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

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

    public void clearPortMode() {
        this.portModeAddress = null;
        this.portModePort = 0;
    }
    
    public void clearRestartMarker() {
        this.restartMarker = 0;
    }

    public enum TransferType {
        ASCII,
        BINARY
    }
}
