package ru.beykerykt.android.filetransport.async;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.data.AuthStatus;
import ru.beykerykt.android.filetransport.data.ServerStatus;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;
import ru.beykerykt.android.filetransport.recyclerview.ServerItemViewHolder;

/**
 * Class for auth
 */
public class AsyncAuthStatusTask extends AsyncTask<Void, Void, AuthStatus> implements AsyncCallbackReceiver {
    private final WeakReference<ServerItemViewHolder> mHolder;
    private final WeakReference<FTPServerEntity> mFtpServerEntity;

    private final int cardElevation = 25;
    private float defaultElevation = 0;

    public AsyncAuthStatusTask(ServerItemViewHolder holder, FTPServerEntity ftpServerInfo) {
        this.mHolder = new WeakReference(holder);
        this.mFtpServerEntity = new WeakReference(ftpServerInfo);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mFtpServerEntity.get() != null) {
            mFtpServerEntity.get().authStatus = AuthStatus.AUTH_UNKNOWN;
        }

        if (mHolder.get() != null) {
            // disable touches
            if (mHolder.get().itemView.isAttachedToWindow()) {
                Window window = ((Activity) mHolder.get().itemView.getContext()).getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            // layout
            LinearLayout authLayout = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status_layout);
            authLayout.setVisibility(View.VISIBLE);

            ProgressBar authProgressBar = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status_progressBar);
            authProgressBar.setVisibility(View.VISIBLE);

            mHolder.get().mIcon.setColorFilter(ContextCompat.getColor(mHolder.get().itemView.getContext(), android.R.color.holo_orange_light));
            TextView authStatus = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status);
            authStatus.setText(R.string.text_server_list_server_auth_login);
            authStatus.setVisibility(View.VISIBLE);

            // change material card for indication
            MaterialCardView materialCardView = (MaterialCardView) mHolder.get().itemView;
            defaultElevation = materialCardView.getCardElevation();
            ObjectAnimator animator = ObjectAnimator.ofFloat(materialCardView, "cardElevation", defaultElevation, cardElevation);
            animator.start();
        }
    }

    @Override
    protected AuthStatus doInBackground(Void... args) {
        AuthStatus status = AuthStatus.AUTH_FAILED;
        if (mFtpServerEntity != null) {
            try {
                FTPClient ftpClient = FileTransport.getApplicationManager().getFTPConnectionManager().openConnection(mFtpServerEntity.get());
                if (ftpClient.isConnected()) {
                    boolean isAnonymous = mFtpServerEntity.get().isAnonymous();
                    String login = !isAnonymous ? mFtpServerEntity.get().getLogin() : "anonymous";
                    String password = !isAnonymous ? mFtpServerEntity.get().getPassword() : "anonymous";

                    if (FileTransport.getApplicationManager().getFTPConnectionManager().login(ftpClient, login, password)) {
                        status = AuthStatus.AUTH_SUCCESS;
                    } else {
                        status = AuthStatus.AUTH_FAILED;
                    }

                    // TODO: Remove in release version
                    //FileTransport.getApplicationManager().getFTPConnectionManager().logout(ftpClient);
                    //FileTransport.getApplicationManager().getFTPConnectionManager().disconnect(mFtpServerEntity.get());
                } else {
                    status = AuthStatus.AUTH_CONNECT_FAILED;
                    mFtpServerEntity.get().serverStatus = ServerStatus.STATUS_OFFLINE;
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
                mFtpServerEntity.get().serverStatus = ServerStatus.STATUS_CONNECT_WRONG_HOST;
            } catch (FTPConnectionClosedException ex) { // for disconnect
                ex.printStackTrace();
                status = AuthStatus.AUTH_DISCONNECTED;
            } catch (IOException ex) {
                ex.printStackTrace();
                status = AuthStatus.AUTH_CONNECT_FAILED;
                mFtpServerEntity.get().serverStatus = ServerStatus.STATUS_CONNECT_FAILED;
            }
        }
        return status;
    }

    @Override
    protected void onPostExecute(AuthStatus result) {
        super.onPostExecute(result);
        Log.e("TAG", result.name());

        if (mHolder.get() != null) {
            ProgressBar statusProgressBar = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status_progressBar);
            statusProgressBar.setVisibility(View.GONE);

            TextView authStatus = mHolder.get().itemView.findViewById(R.id.server_list_card_view_server_status);

            if (mFtpServerEntity.get() != null) {
                if (mFtpServerEntity.get().serverStatus == ServerStatus.STATUS_ONLINE) {
                    mHolder.get().mIcon.setColorFilter(ContextCompat.getColor(mHolder.get().itemView.getContext(), R.color.colorPrimary));
                } else {
                    mHolder.get().mIcon.setColorFilter(ContextCompat.getColor(mHolder.get().itemView.getContext(), android.R.color.holo_red_dark));
                }
            }

            switch (result) {
                case AUTH_SUCCESS:
                    authStatus.setText(R.string.text_server_list_server_auth_success);
                    break;
                case AUTH_FAILED:
                    authStatus.setText(R.string.text_server_list_server_auth_failed);
                    break;
                case AUTH_CONNECT_FAILED:
                    authStatus.setText(R.string.text_server_list_server_auth_connect_failed);
                    break;
                case AUTH_CONNECT_TIMEOUT:
                    authStatus.setText(R.string.text_server_list_server_auth_connect_timeout);
                    break;
                case AUTH_DISCONNECTED:
                    authStatus.setText(R.string.text_server_list_server_auth_disconnected);
                    break;
                default:
                    break;
            }

            MaterialCardView materialCardView = (MaterialCardView) mHolder.get().itemView;
            ObjectAnimator animator = ObjectAnimator.ofFloat(materialCardView, "cardElevation", cardElevation, defaultElevation);
            animator.start();

            // enable touches
            if (mHolder.get().itemView.isAttachedToWindow()) {
                Window window = ((Activity) mHolder.get().itemView.getContext()).getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
        receiveData(result);
    }

    @Override
    public void receiveData(Object result) {
        // nothing
    }
}
