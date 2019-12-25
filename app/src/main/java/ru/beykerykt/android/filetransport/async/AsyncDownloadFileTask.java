package ru.beykerykt.android.filetransport.async;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ru.beykerykt.android.filetransport.data.entity.FileEntity;
import ru.beykerykt.android.filetransport.utils.FTPCommands;

public class AsyncDownloadFileTask extends AsyncTask<Void, Void, Integer> implements AsyncCallbackReceiver {
    private FileEntity mEntity;
    private FTPClient mFtpClient = null;
    private String mLocalFolder;
    private boolean mCancelTask;

    private final String TAG = "AsyncDownloadFileTask";

    public AsyncDownloadFileTask(FTPClient ftpClient, FileEntity entity, String localFolder) {
        this.mFtpClient = ftpClient;
        this.mEntity = entity;
        this.mLocalFolder = localFolder;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
            transferFile();
        } catch (FTPConnectionClosedException e) {
            e.printStackTrace();
            return FTPCommands.FTP_CMD_CONNECT_DISCONNECT_BY_SERVER;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFtpClient.getReplyCode();
    }

    private void transferFile() throws Exception {
        long fileSize = mEntity.getSize();
        Log.v(TAG, "fileSize: " + fileSize);
        if (fileSize > 0) {
            InputStream is = retrieveFileStream(mFtpClient, mEntity.getPath());
            downloadFile(is, fileSize);
            is.close();
            mFtpClient.completePendingCommand();
            if (mCancelTask) {
                if (mFtpClient.abort()) {
                    Log.e(TAG, "aborted");
                }
            }
        } else {
            //nosuch files
            if (!mFtpClient.completePendingCommand()) {
                throw new Exception("Pending command failed: " + mFtpClient.getReplyString());
            }
        }
    }

    private InputStream retrieveFileStream(FTPClient ftp, String filePath)
            throws Exception {
        InputStream is = ftp.retrieveFileStream(filePath);
        int reply = ftp.getReplyCode();
        if (is == null
                || (!FTPReply.isPositivePreliminary(reply)
                && !FTPReply.isPositiveCompletion(reply))) {
            throw new Exception(ftp.getReplyString());
        }
        return is;
    }

    private byte[] downloadFile(InputStream is, long fileSize) throws Exception {
        OutputStream os = new FileOutputStream(mLocalFolder + mEntity.getName());
        byte[] buffer = new byte[(int) fileSize];
        int bufferSize = 0;
        int readCount;
        try {
            while ((readCount = is.read(buffer)) > 0) {
                bufferSize += readCount;
                if (isCancelled()) {
                    mCancelTask = true;
                    break;
                }
                os.write(buffer, 0, readCount);
                bytesTransferred(bufferSize, readCount, fileSize);
            }
        } finally {
            os.close();
        }
        Log.i(TAG, "buffer = " + buffer);
        return buffer;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Log.e(TAG, "Code: " + mFtpClient.getReplyCode());
        if (result == FTPCommands.FTP_CMD_CONNECT_FILE_ACTION_SUCCESSFUL) {
            Log.e(TAG, "Downloaded");
        } else {
            Log.e(TAG, "Error");
            Log.e(TAG, "remote file name: " + mEntity.getName());
            Log.e(TAG, "local folder path: " + mLocalFolder);
        }
        receiveData(result);
    }

    // for dialogs
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
    }

    @Override
    public void receiveData(Object result) {
    }
}
