package ru.beykerykt.android.filetransport.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import ru.beykerykt.android.filetransport.R;
import ru.beykerykt.android.filetransport.fragments.AddServerFragment;
import ru.beykerykt.android.filetransport.fragments.ExplorerFragment;
import ru.beykerykt.android.filetransport.fragments.ServerListFragment;
import ru.beykerykt.android.filetransport.fragments.SettingsFragment;
import ru.beykerykt.android.filetransport.utils.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Menu menuList;
    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.text_navigation_drawer_open, R.string.text_navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // set main_menu
        Utils.actToFragment(this, ServerListFragment.class, null, false, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);

            // instanceof is very slow, replace to something fastest
            if (fragment instanceof ServerListFragment) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, R.string.text_toast_exit, Toast.LENGTH_SHORT).show();

                mHandler.postDelayed(mRunnable, 2000);
            } else if (fragment instanceof ExplorerFragment) {
                ExplorerFragment explorerFragment = (ExplorerFragment) fragment;
                if (explorerFragment.onBackPressed()) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(Utils.FRAGMENT_TAG);
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.menu_action_autocheck_checkbox);
        checkable.setChecked(Utils.autoCheck);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //TODO: change menu name
        this.menuList = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_autocheck_checkbox:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                Utils.autoCheck = isChecked;
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (item.isChecked()) return true;

        Class fragmentClass = null;

        switch (id) {
            case R.id.nav_servers:
                fragmentClass = ServerListFragment.class;
                break;
            case R.id.nav_add_server:
                fragmentClass = AddServerFragment.class;
                break;
            case R.id.nav_settings:
                fragmentClass = SettingsFragment.class;
                break;
            default:
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        final Class finalFragmentClass = fragmentClass;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean backStack = true;
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    backStack = true;
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                if (finalFragmentClass.getName().equals(ServerListFragment.class.getName())) {
                    return;
                }

                Utils.actToFragment(MainActivity.this, finalFragmentClass, null, backStack, true);
            }
        }, 250);
        return true;
    }

    public void hideMenu() {
        if (menuList != null) {
            MenuItem item = menuList.findItem(R.id.menu_action_autocheck_checkbox);
            item.setVisible(false);
        }
    }

    public void showMenu() {
        if (menuList != null) {
            MenuItem item = menuList.findItem(R.id.menu_action_autocheck_checkbox);
            item.setVisible(true);
        }
    }
}
