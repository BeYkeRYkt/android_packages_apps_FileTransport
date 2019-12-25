package ru.beykerykt.android.filetransport;

import android.app.Application;
import android.content.Context;

import ru.beykerykt.android.filetransport.data.ApplicationManager;

public class FileTransport extends Application {

    private static ApplicationManager mManager;
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mAppContext = this.getApplicationContext();
        this.mManager = new ApplicationManager(this.getApplicationContext());
    }

    public static Context getAppContext() {
        return mAppContext;
    }

    public static ApplicationManager getApplicationManager() {
        return mManager;
    }
}