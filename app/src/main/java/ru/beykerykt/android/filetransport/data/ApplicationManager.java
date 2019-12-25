package ru.beykerykt.android.filetransport.data;

import android.content.Context;

import ru.beykerykt.android.filetransport.database.ServerManager;

public class ApplicationManager {

    private Context context;
    private ServerManager serverManager;
    private FileManager fileManager;
    private FTPConnectionManager ftpConnectionManager;


    public ApplicationManager(Context context) {
        this.context = context;
    }

    public ServerManager getServerManager() {
        if (this.serverManager == null) {
            this.serverManager = new ServerManager(context.getApplicationContext());
        }
        return this.serverManager;
    }

    public FileManager getFileManager() {
        if (this.fileManager == null) {
            this.fileManager = new FileManager();
        }
        return this.fileManager;
    }

    public FTPConnectionManager getFTPConnectionManager() {
        if (this.ftpConnectionManager == null) {
            this.ftpConnectionManager = new FTPConnectionManager();
        }
        return this.ftpConnectionManager;
    }
}
