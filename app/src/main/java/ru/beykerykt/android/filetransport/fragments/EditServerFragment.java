package ru.beykerykt.android.filetransport.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.transition.TransitionManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ru.beykerykt.android.filetransport.FileTransport;
import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.activity.MainActivity;
import ru.beykerykt.android.filetransport.data.AuthStatus;
import ru.beykerykt.android.filetransport.data.ServerStatus;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;

public class EditServerFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragments_edit_server, container,
                false);
        return rootView;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.text_menu_edit_server);

        // set item in navigationView
        NavigationView navigationView = view.getRootView().findViewById(R.id.nav_view);
        navigationView.setCheckedItem(-1);

        // hide menu
        MainActivity activity = (MainActivity) getActivity();
        activity.hideMenu();

        // animate FAB
        FloatingActionButton fab = view.getRootView().findViewById(R.id.fab);
        fab.hide();


        // load data
        int serverId = getArguments().getInt("serverId");
        final FTPServerEntity data = FileTransport.getApplicationManager().getServerManager().getServer(serverId);

        // server name
        TextInputEditText serverName = view.findViewById(R.id.layout_edit_server_name);
        serverName.setText(data.getName());

        // server address
        TextInputEditText serverAddress = view.findViewById(R.id.layout_edit_server_address);
        serverAddress.setText(data.getHost());

        // server port
        TextInputEditText serverPort = view.findViewById(R.id.layout_edit_server_port);
        serverPort.setText(String.valueOf(data.getPort()));

        // anonymous
        CheckBox anonymous = view.findViewById(R.id.layout_edit_server_anonymous_checkbox);
        anonymous.setChecked(data.isAnonymous());

        // username
        TextInputEditText username = view.findViewById(R.id.layout_edit_server_username);
        username.setText(data.getLogin());

        // password
        TextInputEditText password = view.findViewById(R.id.layout_edit_server_password);
        password.setText(data.getPassword());

        // active mode
        CheckBox activeModeCheckbox = view.findViewById(R.id.layout_edit_server_active_mode_checkbox);
        activeModeCheckbox.setChecked(data.isActiveMode());

        // hide
        if (data.isAnonymous()) {
            // username
            TextInputLayout usernameLayout = view.findViewById(R.id.layout_edit_server_username_layout);
            // password
            TextInputLayout passwordLayout = view.findViewById(R.id.layout_edit_server_password_layout);

            // for animate
            usernameLayout.setVisibility(data.isAnonymous() ? View.GONE : View.VISIBLE);
            passwordLayout.setVisibility(data.isAnonymous() ? View.GONE : View.VISIBLE);
        }

        // anonymous checkbox
        final CheckBox checkBox = view.findViewById(R.id.layout_edit_server_anonymous_checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = checkBox.isChecked();
                // username
                TextInputLayout username = view.findViewById(R.id.layout_edit_server_username_layout);
                // password
                TextInputLayout password = view.findViewById(R.id.layout_edit_server_password_layout);

                // for animate
                TransitionManager.beginDelayedTransition((ViewGroup) view.getRootView());
                username.setVisibility(checked ? View.GONE : View.VISIBLE);
                password.setVisibility(checked ? View.GONE : View.VISIBLE);
            }
        });

        // update
        MaterialButton button = view.findViewById(R.id.layout_edit_server_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // server name
                TextInputEditText serverName = view.findViewById(R.id.layout_edit_server_name);
                String mServerName = !serverName.getText().toString().equals("") ? serverName.getText().toString() : "FTP Server";

                // server address
                TextInputEditText serverAddress = view.findViewById(R.id.layout_edit_server_address);
                if (TextUtils.isEmpty(serverAddress.getText())) {
                    serverAddress.setError("Please Enter a server address!");
                    return;
                }
                String mServerAddress = serverAddress.getText().toString();

                // server port
                TextInputEditText serverPort = view.findViewById(R.id.layout_edit_server_port);
                int mServerPort = Integer.parseInt(!serverPort.getText().toString().equals("") ? serverPort.getText().toString() : "21");

                // anonymous
                CheckBox checkBox = view.findViewById(R.id.layout_edit_server_anonymous_checkbox);
                boolean isAnonymous = checkBox.isChecked();

                // username
                TextInputEditText username = view.findViewById(R.id.layout_edit_server_username);
                String mUsername = username.getText().toString();

                // password
                TextInputEditText password = view.findViewById(R.id.layout_edit_server_password);
                String mPassword = password.getText().toString();

                // active mode
                CheckBox activeModeCheckbox = view.findViewById(R.id.layout_edit_server_active_mode_checkbox);
                boolean activeMode = activeModeCheckbox.isChecked();

                if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
                    isAnonymous = true;
                }

                // update server info
                updateServerInfo(data, mServerName, mServerAddress, mServerPort, isAnonymous, mUsername, mPassword, activeMode);

                Snackbar.make(getActivity().findViewById(android.R.id.content), "Server '" + mServerName + "' updated to server list", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();

                // move back to main_menu screen
                FragmentManager fm = getActivity().getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });
    }

    public void updateServerInfo(FTPServerEntity ftpServerInfo, String name, String address, int port, boolean isAnonymous, String username, String password, boolean activeMode) {
        ftpServerInfo.setName(name);
        ftpServerInfo.setHost(address);
        ftpServerInfo.setPort(port);
        ftpServerInfo.setIsAnonymous(isAnonymous);
        ftpServerInfo.setLogin(username);
        ftpServerInfo.setPassword(password);
        ftpServerInfo.setActiveMode(activeMode);
        Log.e("update", "" + ftpServerInfo.getId());
        FileTransport.getApplicationManager().getServerManager().updateServerToDB(ftpServerInfo);

        // reset statutes
        ftpServerInfo.serverStatus = ServerStatus.STATUS_UNKNOWN;
        ftpServerInfo.authStatus = AuthStatus.AUTH_UNKNOWN;
    }
}