package ru.beykerykt.android.filetransport.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.activity.MainActivity;
import ru.beykerykt.android.filetransport.async.AsyncAuthLogoutTask;
import ru.beykerykt.android.filetransport.async.AsyncDownloadFileTask;
import ru.beykerykt.android.filetransport.async.AsyncListFilesFromServerTask;
import ru.beykerykt.android.filetransport.async.AsyncNoopTask;
import ru.beykerykt.android.filetransport.async.AsyncReconnectTask;
import ru.beykerykt.android.filetransport.async.AsyncUploadFileTask;
import ru.beykerykt.android.filetransport.data.AuthStatus;
import ru.beykerykt.android.filetransport.data.entity.FileEntity;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;
import ru.beykerykt.android.filetransport.model.FileViewModel;
import ru.beykerykt.android.filetransport.recyclerview.ExplorerRecyclerAdapter;
import ru.beykerykt.android.filetransport.utils.FTPCommands;
import ru.beykerykt.android.filetransport.utils.Utils;

import static android.app.Activity.RESULT_OK;

public class ExplorerFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FTPServerEntity mData;
    private FTPClient ftpClient;
    private int mServerId;

    // folders
    private String mFolderPath = "/"; // default
    private List<FileViewModel> mListFiles = new CopyOnWriteArrayList<>();
    private String localFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "FileTransport" + File.separator;

    // tasks
    private AsyncDownloadFileTask mDownloadTask;
    private AsyncListFilesFromServerTask mTask;
    private AsyncUploadFileTask mUploadTask;
    private AsyncNoopTask mNoopTask;
    private AsyncReconnectTask mReAuthTask;
    private Handler mHandler = new Handler();

    // CardView
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ExplorerRecyclerAdapter mAdapter;

    // Swipe for refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // File target
    private FileEntity fileEntityTarget;

    // DEBUG
    private final static String TAG = "ExplorerFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragments_explorer, container,
                false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        // hide menu
        MainActivity activity = (MainActivity) getActivity();
        activity.hideMenu();

        // FAB
        FloatingActionButton fab = view.getRootView().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_fab_upload);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                chooseFile.setType("*/*");
                Intent intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, Utils.ACTIVITY_CHOOSE_FILE_UPLOAD);
            }
        });
        fab.show();

        // load data
        mServerId = getArguments().getInt("serverId");
        mData = FileTransport.getApplicationManager().getServerManager().getServer(mServerId);
        ftpClient = FileTransport.getApplicationManager().getFTPConnectionManager().getActiveConnection(mData);
        getActivity().setTitle(mData.getName());

        // swipe
        mSwipeRefreshLayout = view.getRootView().findViewById(R.id.fragments_explorer_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        // CardView
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        mRecyclerView = view.findViewById(R.id.fragments_explorer_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ExplorerRecyclerAdapter(this, mListFiles);
        mRecyclerView.setAdapter(mAdapter);

        // noop test
        sendNOOP();
        mHandler.postDelayed(mRunnable, mDelay);

        // go update
        updateListFiles();
    }

    private int mDelay = 1000 * 10; // 10 sec
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            boolean busy = false;

            if (mDownloadTask != null && (mDownloadTask.getStatus() == AsyncTask.Status.RUNNING)) {
                busy = true;
            }

            if (mTask != null && (mTask.getStatus() == AsyncTask.Status.RUNNING)) {
                busy = true;
            }

            if (mUploadTask != null && (mUploadTask.getStatus() == AsyncTask.Status.RUNNING)) {
                busy = true;
            }

            if (mNoopTask != null && (mNoopTask.getStatus() == AsyncTask.Status.RUNNING)) {
                busy = true;
            }

            if (mReAuthTask != null && (mReAuthTask.getStatus() == AsyncTask.Status.RUNNING)) {
                busy = true;
            }

            if (!busy) {
                sendNOOP();
            }
            mHandler.postDelayed(this, mDelay);
        }
    };

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView");
        super.onDestroyView();

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        // cancel tasks
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
        }

        if (mTask != null) {
            mTask.cancel(true);
        }

        if (mUploadTask != null) {
            mUploadTask.cancel(true);
        }

        if (mNoopTask != null) {
            mNoopTask.cancel(true);
        }

        if (mReAuthTask != null) {
            mReAuthTask.cancel(true);
        }

        // remove adapter for fix memory leak
        mRecyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Log.e("TAG", "onViewDetachedFromWindow");
                if (!isVisible()) {
                    Log.e("TAG", "Removing");
                    mRecyclerView.setLayoutManager(null);
                    mRecyclerView.setAdapter(null);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        // cancel task
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
        }

        if (mTask != null) {
            mTask.cancel(true);
        }

        if (mUploadTask != null) {
            mUploadTask.cancel(true);
        }

        if (mNoopTask != null) {
            mNoopTask.cancel(true);
        }

        if (mReAuthTask != null) {
            mReAuthTask.cancel(true);
        }

        // go task logout
        new AsyncAuthLogoutTask(mServerId).execute();

        mRecyclerView.setLayoutManager(null);
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onRefresh() {
        showProgressBar(true);
        int delay = mListFiles.size() / 100;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateListFiles();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, delay);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        } else {
            Uri uri = data.getData();
            File file = new File(Utils.getPathFromUri(getContext(), uri));
            final FileEntity entity = FileTransport.getApplicationManager().getFileManager().fileToEntity(file);
            if (requestCode == Utils.ACTIVITY_CHOOSE_FILE_UPLOAD) {
                Log.e(TAG, "Path: " + entity.getPath());
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle(R.string.text_alertdialog_upload_title);
                String message = getString(R.string.text_alertdialog_upload_message).replace("%PATH%", getFolderPath());
                builder.setMessage(message);
                builder.setPositiveButton(R.string.text_default_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setFileEntityTarget(entity);

                        if (mUploadTask != null) {
                            mUploadTask.cancel(true);
                        }

                        // show dialog
                        final ProgressDialog progressDialog = new ProgressDialog(getContext());
                        progressDialog.setMessage(getString(R.string.text_alertdialog_upload_progress_message).replace("%FILE%", entity.getName()).replace("%FOLDER%", getFolderPath()));
                        progressDialog.setTitle(getString(R.string.text_alertdialog_upload_title));
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setCancelable(false);
                        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.text_default_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUploadTask.cancel(true);
                                progressDialog.dismiss();
                                Snackbar.make(getActivity().findViewById(android.R.id.content), "Upload canceled", Snackbar.LENGTH_LONG).show();
                            }
                        });

                        progressDialog.show();

                        mUploadTask = new AsyncUploadFileTask(ftpClient, entity) {
                            @Override
                            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                                int percent = (int) (totalBytesTransferred * 100 / streamSize);
                                // update your progress bar with this percentage
                                progressDialog.setProgress(percent);
                            }

                            @Override
                            public void receiveData(Object result) {
                                // close progress dialog
                                progressDialog.dismiss();
                                handleReplyCode(true);
                                int replyCode = (int) result;
                                if (replyCode != FTPCommands.FTP_CMD_CONNECT_DISCONNECT_BY_SERVER) {
                                    if (replyCode == FTPCommands.FTP_CMD_CONNECT_FILE_ACTION_SUCCESSFUL) {
                                        String s_replyCode = ftpClient.getReplyString().substring(0, ftpClient.getReplyString().length() - 1);
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), s_replyCode, Snackbar.LENGTH_LONG).show();
                                    }
                                    showProgressBar(true);
                                    updateListFiles();
                                }
                            }
                        };
                        mUploadTask.execute();
                    }
                });
                builder.setNegativeButton(R.string.text_default_no, null);
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.WRITE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Granted.
                    // create folder
                    File file = new File(localFolder);
                    file.mkdirs();

                    if (mDownloadTask != null) {
                        mDownloadTask.cancel(true);
                    }

                    // show dialog
                    final ProgressDialog progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage(getString(R.string.text_alertdialog_download_progress_message).replace("%FILE%", getFileEntityTarget().getName()).replace("%FOLDER%", localFolder));
                    progressDialog.setTitle(getString(R.string.text_alertdialog_download_title));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(false);
                    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.text_default_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDownloadTask.cancel(true);
                            progressDialog.dismiss();
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Download canceled", Snackbar.LENGTH_LONG).show();
                        }
                    });
                    progressDialog.show();

                    mDownloadTask = new AsyncDownloadFileTask(ftpClient, getFileEntityTarget(), localFolder) {
                        @Override
                        public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                            int percent = (int) (totalBytesTransferred * 100 / streamSize);
                            // update your progress bar with this percentage
                            progressDialog.setProgress(percent);
                        }

                        @Override
                        public void receiveData(Object result) {
                            // close progress dialog
                            progressDialog.dismiss();

                            int replyCode = (int) result;
                            if (replyCode == FTPCommands.FTP_CMD_CONNECT_FILE_ACTION_SUCCESSFUL) {
                                String s_replyCode = ftpClient.getReplyString().substring(0, ftpClient.getReplyString().length() - 1);
                                Snackbar.make(getActivity().findViewById(android.R.id.content), s_replyCode, Snackbar.LENGTH_LONG).show();
                            }
                            handleReplyCode(true);
                        }
                    };
                    mDownloadTask.execute();
                }
                break;
        }
    }

    public FileEntity getFileEntityTarget() {
        return fileEntityTarget;
    }

    public void setFileEntityTarget(FileEntity fileEntityTarget) {
        this.fileEntityTarget = fileEntityTarget;
    }

    public void sendNOOP() {
        if (mNoopTask != null) {
            mNoopTask.cancel(true);
        }

        mNoopTask = new AsyncNoopTask(mServerId) {
            @Override
            public void receiveData(Object result) {
                boolean flag = (boolean) result;
                if (!flag) {
                    showProgressBar(true);
                    reAuth();
                }
            }
        };
        mNoopTask.execute();
    }

    public void reAuth() {
        if (mReAuthTask != null) {
            mReAuthTask.cancel(true);
        }

        mReAuthTask = new AsyncReconnectTask(mServerId) {
            @Override
            public void receiveData(Object result) {
                AuthStatus status = (AuthStatus) result;
                Log.e(TAG, "status: " + status);
                switch (status) {
                    case AUTH_DISCONNECTED:
                        goBackToServerList();
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Kicked by Server", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        break;
                    case AUTH_SUCCESS:
                        mData = FileTransport.getApplicationManager().getServerManager().getServer(mServerId);
                        ftpClient = FileTransport.getApplicationManager().getFTPConnectionManager().getActiveConnection(mData);
                        getActivity().setTitle(mData.getName());
                        showProgressBar(true);
                        updateListFiles();
                        break;
                    default:
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Server is no response", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                        goBackToServerList();
                        break;
                }
            }
        };
        mReAuthTask.execute();
    }

    /**
     * UI Methods
     **/
    public void goBackToServerList() {
        // fall back
        FragmentManager fm = getActivity().getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void handleReplyCode(boolean notification) {
        String handleText = getString(R.string.app_name);
        int replyCode = ftpClient.getReplyCode();
        String s_replyCode = ftpClient.getReplyString().substring(0, ftpClient.getReplyString().length() - 1);
        switch (replyCode) {
            case FTPCommands.FTP_CMD_CONNECT_DISCONNECT_BY_SERVER:
                handleText = "Kicked by Server";
                Snackbar.make(getActivity().findViewById(android.R.id.content), s_replyCode, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                goBackToServerList();
                break;
            case FTPCommands.FTP_CMD_CONNECT_FILE_ACTION_SUCCESSFUL:
                // create notification
                handleText = "Success";
                //Snackbar.make(getActivity().findViewById(android.R.id.content), s_replyCode, Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                break;
            case FTPCommands.FTP_CMD_AUTH_NOT_LOGGEN_IN:
            case FTPCommands.FTP_CMD_AUTH_REQUEST_LOG_IN:
                handleText = "Auth error";
                goBackToServerList();
                Snackbar.make(getActivity().findViewById(android.R.id.content), s_replyCode, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            case FTPCommands.FTP_CMD_FILESYSTEM_FILE_NOT_AVAILABLE:
            case FTPCommands.FTP_CMD_FILESYSTEM_NOT_ENOUGH_MEMORY_ALLOCATED:
            case FTPCommands.FTP_CMD_FILESYSTEM_NOT_INVALID_FILE_NAME:
                // create notification
                handleText = "Failed";
                Snackbar.make(getActivity().findViewById(android.R.id.content), s_replyCode, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
            default:
                break;
        }
        if (notification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Utils.createNotificationChannel(getContext());
            }
            updateNotification(getContext(), handleText, s_replyCode);
        }
    }

    public void updateListFiles() {
        if (mTask != null) {
            mTask.cancel(true);
        }

        mTask = new AsyncListFilesFromServerTask(mServerId, mFolderPath) {

            @Override
            public void receiveData(Object result) {
                handleReplyCode(false);

                // update result
                List<FileEntity> list = (List<FileEntity>) result;
                if (list == null) {
                    sendNOOP();
                    return;
                }

                mListFiles.clear();

                if (!mFolderPath.equals("/")) {
                    FileViewModel model = new FileViewModel();
                    model.setName("..");
                    model.setDirectory(true);
                    int lastIndex = mFolderPath.lastIndexOf("/");
                    if (lastIndex != 0) {
                        model.setPath(mFolderPath.substring(0, lastIndex));
                    } else {
                        model.setPath(mFolderPath.substring(0, lastIndex + 1));
                    }
                    mListFiles.add(model);
                }

                for (int i = 0; i < list.size(); i++) {
                    mListFiles.add(new FileViewModel(list.get(i)));
                }

                // remove loading spinner
                showProgressBar(false);

                // tell adapter to update
                mAdapter.notifyDataSetChanged();
            }
        };
        mTask.execute();
    }

    public void showProgressBar(boolean flag) {
        View view = getView();

        FrameLayout progressBar = view.findViewById(R.id.fragments_explorer_progressBar_layout);
        progressBar.setVisibility(flag ? View.VISIBLE : View.GONE);

        FrameLayout recycleView = view.findViewById(R.id.fragments_explorer_recycler_view_layout);
        recycleView.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    public String getFolderPath() {
        return mFolderPath;
    }

    public void setFolderPath(String newFolderPath) {
        mFolderPath = newFolderPath;

        TextView textView = getView().findViewById(R.id.fragments_explorer_folder_path_path);
        textView.setText(mFolderPath);

        // update ?
        showProgressBar(true);
        updateListFiles();
    }

    public boolean onBackPressed() {
        if (getFolderPath().equals("/")) {
            return true;
        }

        // get back folder
        FileViewModel model = mListFiles.get(0);
        setFolderPath(model.getPath());
        return false;
    }

    /**
     * Main body for notifications
     */
    private static Notification getNotification(Context context, String title, String text) {
        Notification notification = new NotificationCompat.Builder(context, Utils.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setSmallIcon(R.drawable.ic_fab_upload)
                .build();
        return notification;
    }

    private void updateNotification(Context context, String title, String text) {
        NotificationManager manager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            manager = context.getSystemService(NotificationManager.class);
        } else {
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        manager.notify(Utils.NOTIFICATION_NOTIFY_ID, getNotification(context, title, text));
    }
}
