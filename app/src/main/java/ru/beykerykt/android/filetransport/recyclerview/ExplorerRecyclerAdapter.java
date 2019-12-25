package ru.beykerykt.android.filetransport.recyclerview;

import android.Manifest;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.fragments.ExplorerFragment;
import ru.beykerykt.android.filetransport.model.FileViewModel;
import ru.beykerykt.android.filetransport.utils.Utils;

/**
 * XML: explorer_list_item.xml
 */
public class ExplorerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FileViewModel> mFileQueue = null;
    private ExplorerFragment mFragment;

    public ExplorerRecyclerAdapter(ExplorerFragment fragment, List<FileViewModel> list) {
        this.mFragment = fragment;
        this.mFileQueue = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.explorer_list_item, parent, false);
        FileViewHolder appViewHolder = new FileViewHolder(itemView);
        return appViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        FileViewHolder fileViewHolder = (FileViewHolder) holder;
        final FileViewModel model = mFileQueue.get(position);

        if (model.isDirectory()) {
            fileViewHolder.icon.setImageResource(R.drawable.ic_folder);
            fileViewHolder.filePath.setText("");
        } else {
            fileViewHolder.icon.setImageResource(R.drawable.ic_file);
            fileViewHolder.filePath.setText("" + Utils.humanReadableByteCountSI(model.getSize()));
        }
        fileViewHolder.fileName.setText(model.getName());

        // register listeners
        View button = holder.itemView;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.isDirectory()) {
                    String nextFolderName = model.getPath();
                    mFragment.setFolderPath(nextFolderName);
                } else {
                    // TODO: DialogBox for downloading
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mFragment.getContext());
                    builder.setTitle(R.string.text_alertdialog_download_title);
                    builder.setMessage(R.string.text_alertdialog_download_message);
                    builder.setPositiveButton(R.string.text_default_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            mFragment.requestPermissions(permissions, Utils.WRITE_REQUEST_CODE);
                            mFragment.setFileEntityTarget(model.toFileEntity());
                        }
                    });
                    builder.setNegativeButton(R.string.text_default_no, null);
                    builder.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFileQueue.size();
    }

    private static class FileViewHolder extends RecyclerView.ViewHolder {
        public TextView fileName;
        public TextView filePath;
        public ImageView icon;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            this.fileName = itemView.findViewById(R.id.explorer_list_item_name);
            this.filePath = itemView.findViewById(R.id.explorer_list_item_path);
            this.icon = itemView.findViewById(R.id.explorer_list_item_icon);
        }
    }
}
