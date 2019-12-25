package ru.beykerykt.android.filetransport.async;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.io.IOException;
import java.util.List;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.data.entity.FileEntity;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;

public class AsyncListFilesFromServerTask extends AsyncTask<Void, Void, List<FileEntity>> implements AsyncCallbackReceiver {

    private int mServerId;
    private String mPath;

    public AsyncListFilesFromServerTask(int serverId, String path) {
        this.mServerId = serverId;
        this.mPath = path;
    }

    @Override
    protected List<FileEntity> doInBackground(Void... voids) {
        FTPServerEntity mData = FileTransport.getApplicationManager().getServerManager().getServer(mServerId);
        List<FileEntity> list = null;
        try {
            list = FileTransport.getApplicationManager().getFTPConnectionManager().listFiles(mData, mPath);
        } catch (FTPConnectionClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<FileEntity> fileEntities) {
        super.onPostExecute(fileEntities);
        receiveData(fileEntities);
    }

    @Override
    public void receiveData(Object result) {

    }
}
