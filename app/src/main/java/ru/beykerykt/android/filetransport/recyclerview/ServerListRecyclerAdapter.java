package ru.beykerykt.android.filetransport.recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.List;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.async.AsyncAuthStatusTask;
import ru.beykerykt.android.filetransport.async.AsyncCheckServerStatusTask;
import ru.beykerykt.android.filetransport.data.AuthStatus;
import ru.beykerykt.android.filetransport.data.ServerStatus;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;
import ru.beykerykt.android.filetransport.fragments.EditServerFragment;
import ru.beykerykt.android.filetransport.fragments.ExplorerFragment;
import ru.beykerykt.android.filetransport.utils.Utils;

public class ServerListRecyclerAdapter extends RecyclerView.Adapter<ServerItemViewHolder> {

    private List<FTPServerEntity> mDataset;
    private WeakReference<FragmentActivity> mFragmentActivityWeakReference;

    public ServerListRecyclerAdapter(FragmentActivity fragmentActivityWeakReference, List<FTPServerEntity> dataset) {
        this.mFragmentActivityWeakReference = new WeakReference(fragmentActivityWeakReference);
        this.mDataset = dataset;
    }

    @Override
    public ServerItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.server_list_item, parent, false);
        ServerItemViewHolder vh = new ServerItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ServerItemViewHolder holder, int position) {
        FTPServerEntity serverInfo = mDataset.get(position);
        holder.mTitle.setText(serverInfo.getName());
        holder.mTitle.setSelected(true);
        holder.mAddress.setText(serverInfo.getHost() + ":" + serverInfo.getPort());
    }

    @Override
    public void onViewAttachedToWindow(@NonNull final ServerItemViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (holder.mUpdateStatusTask != null) {
            holder.mUpdateStatusTask.cancel(true);
        }

        if (holder.mAuthTask != null) {
            holder.mAuthTask.cancel(true);
        }

        // set info from serverInfo
        final FTPServerEntity serverInfo = mDataset.get(holder.getAdapterPosition());

        if (serverInfo.serverStatus == ServerStatus.STATUS_UNKNOWN) {
            holder.mIcon.setColorFilter(null);
            holder.mStatus.setVisibility(View.GONE);

            // TODO: implement autocheck
            boolean autoCheck = Utils.autoCheck;
            if (autoCheck) {
                holder.mUpdateStatusTask = new AsyncCheckServerStatusTask(holder, serverInfo);
                holder.mUpdateStatusTask.execute();
            }
        } else {
            LinearLayout statusLayout = holder.itemView.findViewById(R.id.server_list_card_view_server_status_layout);
            statusLayout.setVisibility(View.VISIBLE);

            ProgressBar statusProgressBar = holder.itemView.findViewById(R.id.server_list_card_view_server_status_progressBar);
            statusProgressBar.setVisibility(View.GONE);

            holder.mStatus.setVisibility(View.VISIBLE);

            // check server status first
            switch (serverInfo.serverStatus) {
                case STATUS_ONLINE:
                    holder.mIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
                    holder.mStatus.setText(R.string.text_server_list_server_available);
                    break;
                case STATUS_OFFLINE:
                    holder.mIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                    holder.mStatus.setText(R.string.text_server_list_server_not_available);
                    break;
                case STATUS_CHECKING:
                    holder.mIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
                    holder.mStatus.setText(R.string.text_server_list_server_checking);
                    break;
                case STATUS_CONNECT_FAILED:
                    holder.mIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                    holder.mStatus.setText(R.string.text_server_list_server_server_connect_failed);
                    break;
                case STATUS_CONNECT_TIMEOUT:
                    holder.mIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                    holder.mStatus.setText(R.string.text_server_list_server_server_connect_timeout);
                    break;
                case STATUS_CONNECT_WRONG_HOST:
                    holder.mIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                    holder.mStatus.setText(R.string.text_server_list_server_server_connect_wrong_host);
                    break;
                default:
                    break;
            }

            // auth status
            switch (serverInfo.authStatus) {
                case AUTH_FAILED:
                    holder.mStatus.setText(R.string.text_server_list_server_auth_failed);
                    break;
                case AUTH_SUCCESS:
                    holder.mStatus.setText(R.string.text_server_list_server_auth_success);
                    break;
                case AUTH_DISCONNECTED:
                    holder.mStatus.setText(R.string.text_server_list_server_auth_disconnected);
                    break;
                case AUTH_CONNECT_TIMEOUT:
                    holder.mStatus.setText(R.string.text_server_list_server_auth_connect_timeout);
                    break;
                case AUTH_CONNECT_FAILED:
                    holder.mStatus.setText(R.string.text_server_list_server_auth_connect_failed);
                    break;
                default:
                    break;
            }
        }

        // expand/collapse cardview
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                switch (serverInfo.serverStatus) {
                    case STATUS_ONLINE:
                        holder.mAuthTask = new AsyncAuthStatusTask(holder, serverInfo) {
                            @Override
                            public void receiveData(Object result) {
                                AuthStatus status = (AuthStatus) result;

                                if (status == AuthStatus.AUTH_SUCCESS) {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("serverId", serverInfo.getId());

                                    Utils.actToFragment(mFragmentActivityWeakReference.get(), ExplorerFragment.class, bundle, true, true);
                                }
                            }
                        };
                        holder.mAuthTask.execute();
                        break;
                    default:
                        if (holder.mUpdateStatusTask != null) {
                            holder.mUpdateStatusTask.cancel(true);
                        }
                        holder.mUpdateStatusTask = new AsyncCheckServerStatusTask(holder, serverInfo);
                        holder.mUpdateStatusTask.execute();
                        break;
                }
            }
        });

        // popup
        holder.mDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, holder, serverInfo);
            }
        });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ServerItemViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.mDots.setOnClickListener(null);
        holder.itemView.setOnClickListener(null);
        if (holder.mUpdateStatusTask != null) {
            holder.mUpdateStatusTask.cancel(true);
        }
        if (holder.mAuthTask != null) {
            holder.mAuthTask.cancel(true);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void showPopup(final View v, final ServerItemViewHolder holder, final FTPServerEntity serverInfo) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.card_view_action_check:
                        AutoTransition autoTransition = new AutoTransition();
                        autoTransition.excludeChildren(R.id.server_list_card_view, true);
                        TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView.getRootView(), autoTransition);

                        if (holder.mUpdateStatusTask != null) {
                            holder.mUpdateStatusTask.cancel(true);
                        }
                        holder.mUpdateStatusTask = new AsyncCheckServerStatusTask(holder, serverInfo);
                        holder.mUpdateStatusTask.execute();
                        break;
                    case R.id.card_view_action_edit:
                        Bundle bundle = new Bundle();
                        bundle.putInt("serverId", serverInfo.getId());
                        if (mFragmentActivityWeakReference.get() != null) {
                            Utils.actToFragment(mFragmentActivityWeakReference.get(), EditServerFragment.class, bundle, true, true);
                        }
                        break;
                    case R.id.card_view_action_delete:
                        FileTransport.getApplicationManager().getServerManager().deleteServer(serverInfo.getId());
                        notifyItemRemoved(holder.getAdapterPosition());
                        if (mFragmentActivityWeakReference.get() != null) {
                            Snackbar.make(mFragmentActivityWeakReference.get().findViewById(android.R.id.content), "Server '" + serverInfo.getHost() + "' removed from server list", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.card_view_popup, popup.getMenu());
        popup.show();
    }
}