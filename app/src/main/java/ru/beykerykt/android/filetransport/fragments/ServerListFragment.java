package ru.beykerykt.android.filetransport.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.activity.MainActivity;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;
import ru.beykerykt.android.filetransport.recyclerview.ServerListRecyclerAdapter;
import ru.beykerykt.android.filetransport.utils.Utils;

public class ServerListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // CardView
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ServerListRecyclerAdapter mAdapter;
    private Handler mHandler = null;

    private final static String TAG = "ServerListFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragments_server_list, container,
                false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.text_menu_servers);

        // set item in navigationView
        NavigationView navigationView = view.getRootView().findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_servers);

        // hide menu
        MainActivity activity = (MainActivity) getActivity();
        activity.showMenu();

        // swipe
        mSwipeRefreshLayout = view.getRootView().findViewById(R.id.fragments_server_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        // CardView
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        mRecyclerView = view.findViewById(R.id.fragments_server_list_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        List<FTPServerEntity> myDataset = FileTransport.getApplicationManager().getServerManager().getServers();
        mAdapter = new ServerListRecyclerAdapter(getActivity(), myDataset);
        mRecyclerView.setAdapter(mAdapter);

        // FAB
        FloatingActionButton fab = view.getRootView().findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.actToFragment(getActivity(), AddServerFragment.class, null, true, true);
            }
        });
        fab.show();

        // handler
        HandlerThread thread = new HandlerThread("ServerListQueue");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @Override
    public void onDestroyView() {
        mRecyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                Log.e(TAG, "onViewDetachedFromWindow");
                if (!isVisible()) {
                    Log.e(TAG, "Removing");
                    mRecyclerView.setLayoutManager(null);
                    mRecyclerView.setAdapter(null);
                }
            }
        });
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        mRecyclerView.setLayoutManager(null);
        mRecyclerView.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        int delay = FileTransport.getApplicationManager().getServerManager().getServers().size() / 100;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FileTransport.getApplicationManager().getServerManager().invalidateServerStatus();
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, delay);
    }

    public Handler getHandler() {
        return mHandler;
    }
}
