package ru.beykerykt.android.filetransport.async;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.data.AuthStatus;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;

public class AsyncReconnectTask extends AsyncTask<Void, Void, AuthStatus> implements AsyncCallbackReceiver {

    private int mServerId;

    private final String TAG = "AsyncReconnectTask";

    public AsyncReconnectTask(int serverId) {
        this.mServerId = serverId;
    }

    @Override
    protected AuthStatus doInBackground(Void... voids) {
        AuthStatus status = AuthStatus.AUTH_FAILED;
        FTPServerEntity mFtpServerEntity = FileTransport.getApplicationManager().getServerManager().getServer(mServerId);
        FTPClient ftpClient = FileTransport.getApplicationManager().getFTPConnectionManager().getActiveConnection(mFtpServerEntity);
        // reconnect
        try {
            FileTransport.getApplicationManager().getFTPConnectionManager().logout(ftpClient);
            FileTransport.getApplicationManager().getFTPConnectionManager().disconnect(mFtpServerEntity);

            ftpClient = FileTransport.getApplicationManager().getFTPConnectionManager().openConnection(mFtpServerEntity);
            if (FileTransport.getApplicationManager().getFTPConnectionManager().login(ftpClient, mFtpServerEntity.getLogin(), mFtpServerEntity.getPassword())) {
                status = AuthStatus.AUTH_SUCCESS;
            }
        } catch (ConnectException ex) {
            ex.printStackTrace();
            status = AuthStatus.AUTH_CONNECT_FAILED;
        } catch (SocketTimeoutException ex) { // timeout
            ex.printStackTrace();
            status = AuthStatus.AUTH_CONNECT_TIMEOUT;
        } catch (UnknownHostException ex) { // wrong host
            ex.printStackTrace();
            status = AuthStatus.AUTH_CONNECT_FAILED;
        } catch (FTPConnectionClosedException ex) { // for disconnect
            ex.printStackTrace();
            status = AuthStatus.AUTH_DISCONNECTED;
        } catch (IOException ex) {
            ex.printStackTrace();
            status = AuthStatus.AUTH_CONNECT_FAILED;
        }
        return status;
    }

    @Override
    protected void onPostExecute(AuthStatus authStatus) {
        super.onPostExecute(authStatus);
        // for customs
        receiveData(authStatus);
    }

    @Override
    public void receiveData(Object result) {
    }
}
