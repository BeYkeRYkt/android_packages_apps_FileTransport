package ru.beykerykt.android.filetransport.async;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;

public class AsyncAuthLogoutTask extends AsyncTask<Void, Void, Void> implements AsyncCallbackReceiver {

    private int mServerId;

    private final String TAG = "AsyncAuthLogoutTask";

    public AsyncAuthLogoutTask(int serverId) {
        this.mServerId = serverId;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.e(TAG, "Logout");
        FTPServerEntity mFtpServerEntity = FileTransport.getApplicationManager().getServerManager().getServer(mServerId);
        FTPClient ftpClient = FileTransport.getApplicationManager().getFTPConnectionManager().getActiveConnection(mFtpServerEntity);
        try {
            FileTransport.getApplicationManager().getFTPConnectionManager().logout(ftpClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileTransport.getApplicationManager().getFTPConnectionManager().disconnect(mFtpServerEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // for customs
        receiveData(aVoid);
    }

    @Override
    public void receiveData(Object result) {
    }
}
