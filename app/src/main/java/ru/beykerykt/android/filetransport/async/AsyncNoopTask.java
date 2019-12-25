package ru.beykerykt.android.filetransport.async;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;

public class AsyncNoopTask extends AsyncTask<Void, Void, Boolean> implements AsyncCallbackReceiver {
    private int mServerId;

    public AsyncNoopTask(int serverId) {
        this.mServerId = serverId;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        FTPServerEntity ftpServerEntity = FileTransport.getApplicationManager().getServerManager().getServer(mServerId);
        FTPClient ftpClient = FileTransport.getApplicationManager().getFTPConnectionManager().getActiveConnection(ftpServerEntity);
        try {
            return ftpClient.sendNoOp();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        receiveData(aBoolean);
    }

    @Override
    public void receiveData(Object result) {
    }
}
