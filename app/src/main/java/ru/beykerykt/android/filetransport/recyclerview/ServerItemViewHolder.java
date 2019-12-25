package ru.beykerykt.android.filetransport.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.async.AsyncAuthStatusTask;
import ru.beykerykt.android.filetransport.async.AsyncCheckServerStatusTask;

public class ServerItemViewHolder extends RecyclerView.ViewHolder {

    public TextView mTitle;
    public TextView mAddress;
    public TextView mStatus;
    public ImageView mIcon;
    public ImageView mDots;
    public AsyncCheckServerStatusTask mUpdateStatusTask;
    public AsyncAuthStatusTask mAuthTask;

    public ServerItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mTitle = itemView.findViewById(R.id.server_list_card_view_server_name);
        this.mAddress = itemView.findViewById(R.id.server_list_card_view_server_address);
        this.mStatus = itemView.findViewById(R.id.server_list_card_view_server_status);
        this.mIcon = itemView.findViewById(R.id.server_list_card_view_icon);
        this.mDots = itemView.findViewById(R.id.server_list_card_view_dots);
    }
}
