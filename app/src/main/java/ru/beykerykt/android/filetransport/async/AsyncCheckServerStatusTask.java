package ru.beykerykt.android.filetransport.async;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.data.ServerStatus;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;
import ru.beykerykt.android.filetransport.recyclerview.ServerItemViewHolder;

/**
 * Class for checking status
 */
public class AsyncCheckServerStatusTask extends AsyncTask<Void, Void, ServerStatus> implements AsyncCallbackReceiver {

    private final WeakReference<ServerItemViewHolder> mHolder;
    private final WeakReference<FTPServerEntity> mFtpServerInfo;

    public AsyncCheckServerStatusTask(ServerItemViewHolder holder, FTPServerEntity ftpServerInfo) {
        this.mHolder = new WeakReference(holder);
        this.mFtpServerInfo = new WeakReference(ftpServerInfo);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mFtpServerInfo.get() != null) {
            mFtpServerInfo.get().serverStatus = ServerStatus.STATUS_UNKNOWN;
        }

        if (mHolder.get() != null) {
            mHolder.get().mIcon.setColorFilter(ContextCompat.getColor(mHolder.get().itemView.getContext(), android.R.color.holo_orange_light));

            LinearLayout statusLayout = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status_layout);
            statusLayout.setVisibility(View.VISIBLE);

            ProgressBar statusProgressBar = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status_progressBar);
            statusProgressBar.setVisibility(View.VISIBLE);

            mHolder.get().mStatus.setVisibility(View.VISIBLE);
            mHolder.get().mStatus.setText(R.string.text_server_list_server_checking);
        }
    }

    @Override
    protected ServerStatus doInBackground(Void... args) {
        ServerStatus status = ServerStatus.STATUS_OFFLINE;
        if (mHolder.get() != null) {
            Log.e("TAG", "adapter: " + mHolder.get().getAdapterPosition());
        }

        if (mFtpServerInfo.get() != null) {
            try {
                if (FileTransport.getApplicationManager().getFTPConnectionManager().isPortOpen(mFtpServerInfo.get().getHost(), mFtpServerInfo.get().getPort())) {
                    status = ServerStatus.STATUS_ONLINE;
                } else {
                    status = ServerStatus.STATUS_OFFLINE;
                }
            } catch (ConnectException ex) {
                ex.printStackTrace();
                status = ServerStatus.STATUS_CONNECT_FAILED;
            } catch (SocketTimeoutException ex) { // timeout
                ex.printStackTrace();
                status = ServerStatus.STATUS_CONNECT_TIMEOUT;
            } catch (UnknownHostException ex) { // wrong host
                ex.printStackTrace();
                status = ServerStatus.STATUS_CONNECT_WRONG_HOST;
            } catch (FTPConnectionClosedException ex) { // for disconnect
                ex.printStackTrace();
                status = ServerStatus.STATUS_CONNECT_FAILED;
            } catch (IOException e) {
                e.printStackTrace();
                status = ServerStatus.STATUS_CONNECT_FAILED;
            }
        }
        return status;
    }

    @Override
    protected void onPostExecute(ServerStatus result) {
        super.onPostExecute(result);
        Log.e("TAG", "result: " + result);

        if (mFtpServerInfo != null) {
            mFtpServerInfo.get().serverStatus = result;
        }

        if (mHolder.get() != null) {
            if (result == ServerStatus.STATUS_ONLINE) {
                mHolder.get().mIcon.setColorFilter(ContextCompat.getColor(mHolder.get().itemView.getContext(), R.color.colorPrimary));
            } else {
                mHolder.get().mIcon.setColorFilter(ContextCompat.getColor(mHolder.get().itemView.getContext(), android.R.color.holo_red_dark));
            }

            switch (result) {
                case STATUS_ONLINE:
                    mHolder.get().mStatus.setText(R.string.text_server_list_server_available);
                    break;
                case STATUS_OFFLINE:
                    mHolder.get().mStatus.setText(R.string.text_server_list_server_not_available);
                    break;
                case STATUS_CONNECT_FAILED:
                    mHolder.get().mStatus.setText(R.string.text_server_list_server_server_connect_failed);
                    break;
                case STATUS_CONNECT_TIMEOUT:
                    mHolder.get().mStatus.setText(R.string.text_server_list_server_server_connect_timeout);
                    break;
                case STATUS_CONNECT_WRONG_HOST:
                    mHolder.get().mStatus.setText(R.string.text_server_list_server_server_connect_wrong_host);
                    break;
            }

            ProgressBar statusProgressBar = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status_progressBar);
            statusProgressBar.setVisibility(View.GONE);
        }

        receiveData(result);
    }

    @Override
    public void receiveData(Object result) {
        // nothing
    }
}
