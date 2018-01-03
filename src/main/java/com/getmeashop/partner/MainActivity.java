package com.getmeashop.partner;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.getmeashop.partner.database.DatabaseHandler;
import com.getmeashop.partner.util.Constants;
import com.getmeashop.partner.util.GetRequest;
import com.getmeashop.partner.util.Interfaces;
import com.google.android.gcm.GCMRegistrar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;


public class MainActivity extends AppCompatActivity implements Callbacks, Interfaces.goToFrag {

    int logout = 0;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    ProgressDialog dialog;
    Class fragmentClass;
    int limit = 10;
    Context currentContext;
    Activity activity;
    boolean backPressedToExitOnce = false;
    Handler mHandler;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private int currentFrag = 0;
    private Fragment fragment;
    private MenuItem mPreviousMenuItem;
    private Menu menu;
    private Callbacks callBack;
    private String page_first = "?format=json&offset=0&limit=" + limit,
            uri_logout, username, base_uri, uri_login, uri_get_user_details, mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DatabaseHandler dbh = new DatabaseHandler(this);
        Boolean everything_synced = true;
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);


        sp = getSharedPreferences(Constants.User_sp, Context.MODE_PRIVATE);
        editor = sp.edit();


        TextView nav_header = (TextView) nvDrawer.getHeaderView(0).findViewById(R.id.nav_user_name);
        nav_header.setText(sp.getString("userName", "default"));
        currentContext = this;
        username = sp.getString("userName", "test");
        activity = this;
        callBack = this;
        base_uri = Constants.base_uri;
        //base_uri1 = this.getString(R.string.base_uri1);
        uri_login = base_uri + "mobile/login/";

        mHandler = new Handler();
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        nav_header = (TextView) findViewById(R.id.nav_user_name);


        // Tie DrawerLayout events to the ActionBarToggle
        mDrawerLayout.setDrawerListener(drawerToggle);

        // Find our drawer view

        //gotoFragment(Constants.FragAddProduct);
        // Setup drawer view

        gotoFragment(Constants.FragManageUser);
        setupDrawerContent(nvDrawer);


        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);


        uri_logout = base_uri + "partner/logout/?registration_id=" + GCMRegistrar.getRegistrationId(this) + "&device_id=" + Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // creating urls based on usernames


        if (getIntent().hasExtra("again")) {
            //setLast_pager_pos(0);
            if (getIntent().getStringExtra("again").equalsIgnoreCase("false")) {

               /* Intent intent = new Intent("finish_activity");
                sendBroadcast(intent);*/
                editor.putBoolean("first_login", true);
                editor.commit();
                DatabaseHandler db = new DatabaseHandler(this);
                if (Utils.isConnectingToInternet(currentContext) && !db.errorsync()) {
                    // new GetRequest(this, uri_get_user_details,
                    //       currentContext);
                }
            } else if (getIntent().getStringExtra("again").equalsIgnoreCase(
                    "true")) {
                editor.putBoolean("first_login", false);
                editor.commit();

            }


        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.logout) {

                            logout = 1;
                            if (Utils.isConnectingToInternet(currentContext))
                                new GetRequest((Callbacks) currentContext, uri_logout, currentContext);
                            return true;
                        }


                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                LoadFragment();
                            }
                        }, 300);
                        selectDrawerItem(menuItem);
                        mDrawerLayout.closeDrawers();

                        return true;
                    }
                });


    }


    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        fragment = null;
        int prev = currentFrag;
        switch (menuItem.getItemId()) {
            case R.id.add_user:
                fragmentClass = AddUser.class;
                currentFrag = Constants.FragAddUser;
                break;
            case R.id.archived_user:
                fragmentClass = ArchivedUserList.class;
                currentFrag = Constants.FragArchivedUser;
                break;
            case R.id.manage_user:
                fragmentClass = UserList.class;
                currentFrag = Constants.FragManageUser;
                break;
        }


        menuItem.setCheckable(true);
        menuItem.setChecked(true);
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        mPreviousMenuItem = menuItem;
        setTitle(menuItem.getTitle());
        prev = currentFrag;

    }

    public void LoadFragment() {
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    public void CheckSync() {


        DatabaseHandler dbh = new DatabaseHandler(this);
//
//        if (dbh.checkSync()) {
//            hideSyncIndicator();
//        } else {
//            showSyncIndicator();
//        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (currentFrag == Constants.FragManageUser) {
            //  ((ManageCategory) fragment).updateView();
        } else if (currentFrag == Constants.FragAddUser) {
            // ((ManageProducts) fragment).showView(false);
        }
    }

    @Override
    public void postexecute(String url, int statusCode) {
        if (statusCode == 200 && url.equalsIgnoreCase(uri_logout)) {
            editor.clear();
            editor.commit();
            DatabaseHandler dbh = new DatabaseHandler(getApplicationContext());
            dbh.deletedatabase();
            Utils.showToast(getApplicationContext(), "Logout successful");
            Intent to_Login = new Intent(this, LoginActivity.class);
            to_Login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(to_Login);
        } else {
            dialog.dismiss();
            Utils.showToast(getApplicationContext(), "Logout failed, Please try again later");
        }
    }

    @Override
    public void preexecute(String url) {
        if (url.equalsIgnoreCase(uri_logout)) {
            dialog.setMessage("Logging out, Please wait..");
            dialog.show();
        }

    }

    @Override
    public void processResponse(HttpResponse response, String url) {

    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {
        return null;
    }

    /**
     * called via postExecute of PostRequest
     * <p/>
     * shows toast corressponding to satus code
     */
    public boolean onValidResponse(int statusCode, String url) {
        switch (statusCode) {
            case 400:   // bad request
            case 403:   // forbidden
            case 500:   // internal server error
                return false;
        }
        Log.e("onValidResponse", statusCode + "");
        return true;
    }


    /**
     * called on create menu
     * <p/>
     * contains relevant task according to the selected option
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return false;
    }


    /**
     * When pressed back
     * <p/>
     * if there exist any local change then asks for sync, otherwise goes to
     * mainactivity with exit flag
     */
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        else if (currentFrag == Constants.FragAddUser) {
            gotoFragment(Constants.FragManageUser);
        } else if (backPressedToExitOnce) {
            exit();
        } else {
            backPressedToExitOnce = true;
            Utils.showToast("Press again to exit", currentContext);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedToExitOnce = false;
                }
            }, 2000);

        }
    }

    @Override
    public void gotoFragment(int FragNum) {
        selectDrawerItem(nvDrawer.getMenu().getItem(FragNum));
        LoadFragment();
    }

    public void refresh(View v) {
        ((Interfaces.LoadStatistics) (fragment)).LoadStatstics();
    }

    public void exit() {
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}