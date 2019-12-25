package ru.beykerykt.android.filetransport.async;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import ru.beykerykt.android.filetransport.data.entity.FileEntity;
import ru.beykerykt.android.filetransport.utils.FTPCommands;

public class AsyncUploadFileTask extends AsyncTask<Void, Void, Integer> implements AsyncCallbackReceiver {
    private FTPClient mFtpClient;
    private FileEntity mLocalFileEntity;
    private boolean mCancelTask;

    private final String TAG = "AsyncUploadFileTask";

    public AsyncUploadFileTask(FTPClient ftpClient, FileEntity localFileEntity) {
        this.mFtpClient = ftpClient;
        this.mLocalFileEntity = localFileEntity;
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
        long fileSize = mLocalFileEntity.getSize();
        Log.v(TAG, "fileSize: " + fileSize);
        if (fileSize > 0) {
            OutputStream os = storeFileStream(mFtpClient, mLocalFileEntity.getName());
            uploadFile(os, fileSize);
            os.close();
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

    private OutputStream storeFileStream(FTPClient ftp, String filePath)
            throws Exception {
        OutputStream os = ftp.storeFileStream(filePath);
        int reply = ftp.getReplyCode();
        if (os == null
                || (!FTPReply.isPositivePreliminary(reply)
                && !FTPReply.isPositiveCompletion(reply))) {
            throw new Exception(ftp.getReplyString());
        }
        return os;
    }

    private byte[] uploadFile(OutputStream os, long fileSize) throws Exception {
        InputStream is = new FileInputStream(mLocalFileEntity.getPath());
        byte[] buffer = new byte[4096];
        int bufferSize = 0;
        int readCount;
        try {
            while ((readCount = is.read(buffer)) != -1) {
                bufferSize += readCount;
                if (isCancelled()) {
                    mCancelTask = true;
                    break;
                }
                os.write(buffer, 0, readCount);
                bytesTransferred(bufferSize, readCount, fileSize);
            }
        } finally {
            is.close();
        }
        Log.i(TAG, "buffer = " + buffer);
        return buffer;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (result == FTPCommands.FTP_CMD_CONNECT_FILE_ACTION_SUCCESSFUL) {
            Log.e(TAG, "Code: " + result);
            Log.e(TAG, "Uploaded");
        } else {
            Log.e(TAG, "Error");
            Log.e(TAG, "Code: " + result);
            Log.e(TAG, "remote file name: " + mLocalFileEntity.getName());
            Log.e(TAG, "local file path: " + mLocalFileEntity.getPath());
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
