package ru.beykerykt.android.filetransport.database.entity;

import ru.beykerykt.android.filetransport.data.AuthStatus;
import ru.beykerykt.android.filetransport.data.ServerStatus;

public class FTPServerEntity extends DatabaseEntity {

    private String name;
    private String host;
    private boolean isAnonymous;
    private String login;
    private String password;
    private int port;
    private boolean activeMode;

    // temp for UI
    public ServerStatus serverStatus = ServerStatus.STATUS_UNKNOWN;
    public AuthStatus authStatus = AuthStatus.AUTH_UNKNOWN;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isActiveMode() {
        return activeMode;
    }

    public void setActiveMode(boolean mode) {
        this.activeMode = mode;
    }
}
