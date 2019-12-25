package ru.beykerykt.android.filetransport.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.activity.MainActivity;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragments_settings, container,
                false);
        return rootView;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(R.string.text_menu_settings);

        // set item in navigationView
        NavigationView navigationView = view.getRootView().findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_settings);

        // hide menu
        MainActivity activity = (MainActivity) getActivity();
        activity.hideMenu();

        // hide FAB
        FloatingActionButton fab = view.getRootView().findViewById(R.id.fab);
        fab.hide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
