package com.getmeashop.realestate.partner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.getmeashop.realestate.partner.util.Constants;
import com.getmeashop.realestate.partner.util.GetRequest;
import com.getmeashop.realestate.partner.util.UpdateChekerService;
import com.google.android.gcm.GCMRegistrar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;

public class SplashScreen extends AppCompatActivity implements Callbacks {

    static String uri_gcm, uri_csrf, regId;
    static Context currentContext;
    static Callbacks callbacks;
    static TextView text_status;
    static ProgressBar loading_spinner;
    static SharedPreferences sp;
    LinearLayout updateLayout;
    boolean registered = false;
    static boolean update_later = false;
    boolean isRegisteredId = false;
    static boolean again = false;
    static Handler mHandler;
    Button requestAccess;
    private static final int REQUEST_WRITE_STORAGE = 112;

    public static void changeStatus(String status) {
        text_status.setText(status);
    }

    public static void goToMain() {

        boolean hasPermission = (ContextCompat.checkSelfPermission(currentContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(currentContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            ActivityCompat.requestPermissions((Activity) currentContext,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_WRITE_STORAGE);
        } else {

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    normalRoutine();
                    if (!update_later) {
                        Intent serviceIntent = new Intent(currentContext, UpdateChekerService.class);
                        currentContext.startService(serviceIntent);
                    }
                }
            }, 2000);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Context ctx = this.getApplicationContext();

        // Use the Sentry DSN (client key) from the Project Settings page on Sentry
        String sentryDsn = "https://e81e26c80ef645d9a940dc06ea1139d4:7cc20af3fb894a0cbe312ddec175297e@watchdog.getmeashop.org/54";
        Sentry.init(sentryDsn, new AndroidSentryClientFactory(ctx));

        // Alternatively, if you configured your DSN in a `sentry.properties`
        // file (see the configuration documentation).
        Sentry.init(new AndroidSentryClientFactory(ctx));
        setContentView(R.layout.activity_splash_screen);


        mHandler = new Handler();
        text_status = (TextView) findViewById(R.id.text_status);
        loading_spinner = (ProgressBar) findViewById(R.id.progressBar);
        updateLayout = (LinearLayout) findViewById(R.id.updateLayout);
        requestAccess = (Button) findViewById(R.id.requestAccess);

        uri_csrf = Constants.base_uri + "partner/login/?type=csrf";
        currentContext = this;
        callbacks = this;
        again = getIntent().getBooleanExtra("again", false);


        sp = getSharedPreferences("Users", Context.MODE_PRIVATE);


        int versionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
            ((TextView) findViewById(R.id.vCode)).setText("Version : " + pInfo.versionName + "/" + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (sp.getInt("update_version", 0) > versionCode) {

            if (sp.getBoolean("update_mandatory", false)) {

                Intent toUpdate = new Intent(this, UpdateApp.class);
                toUpdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                this.startActivity(toUpdate);
            } else {
                //ask for update
                text_status.setText("A new update is available, Do you want to update now ?");
                updateLayout.setVisibility(View.VISIBLE);
            }
        } else {
            gcmTask();
        }


        requestAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ActivityCompat.requestPermissions((Activity) currentContext,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_WRITE_STORAGE);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void postexecute(String url, int statuCode) {
        if (statuCode == 200 && url.equalsIgnoreCase(uri_csrf)) {
           /* if (isRegisteredId)
                new PostRequest(SplashScreen.this, uri_gcm, SplashScreen.this);
            else {
                text_status.setText("Registering Device");
                GCMRegistrar.register(SplashScreen.this, Constants.GCM_SENDER_ID);
            }*/
            text_status.setText("Device Registered successfully, Loading homepage");
            afterRegistered();
        } else if (url.equalsIgnoreCase(uri_gcm)) {
            if (registered) {
                text_status.setText("Device Registered successfully, Loading homepage");
                afterRegistered();
            } else {
                text_status.setText("Something went wrong, Please try closing and reopening App");
            }
        }
    }

    // gcm registration

    @Override
    public void preexecute(String url) {
        if (url.equalsIgnoreCase(uri_gcm))
            text_status.setText("Registering Device");
    }

    @Override
    public void processResponse(HttpResponse response, String url) {

        try {

            InputStream inputStream = response.getEntity().getContent();
            String responseString = Utils
                    .convertInputStreamToString(inputStream);
            System.out.println("device id registration response: "
                    + responseString.toString());

            if (response.getStatusLine().getStatusCode() == 200 && url.equalsIgnoreCase(uri_gcm)) {

                JSONObject myObject = new JSONObject(responseString);
                String result = myObject.get("success").toString();
                if (result.equals("true")) {
                    GCMRegistrar.setRegisteredOnServer(SplashScreen.this, true);
                    registered = true;
                    // displayMessage(currentContext, message);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("registration_id",
                regId));
        nameValuePairs.add(new BasicNameValuePair("device_id", Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID)));
        nameValuePairs.add(new BasicNameValuePair("app", "partner-app"));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return httpPost;
    }

    /**
     * called to register device on GCM
     * <p/>
     * This method checks for the registration, registers on our servers as well
     * calls ok() on post execution which starts next activity
     */
    private void gcmTask() {

        uri_gcm = Constants.base_uri + "partner/register-device/";

        int k = 0;
        GCMRegistrar.checkDevice(SplashScreen.this);
        regId = GCMRegistrar.getRegistrationId(SplashScreen.this);
        // Check if regid already exists
        if (regId.equals("")) {
            // Registration is not present, register now with GCM
            if (k == 0) {
                isRegisteredId = false;
                new GetRequest(callbacks, uri_csrf, currentContext);
            }
            k = 1;

        } else {
            // Device is already registered on GCM
            if (!GCMRegistrar.isRegisteredOnServer(SplashScreen.this)) {
                isRegisteredId = true;
                new GetRequest(callbacks, uri_csrf, currentContext);
            } else {
                afterRegistered();

            }
        }

    }

    /**
     * called after execution of gcm_task()
     * <p/>
     * starts dashboard activity, also sets has_session flag true
     */
    public void afterRegistered() {
        goToMain();
    }

    public void updateNow(View v) {
        Intent toUpdate = new Intent(this, UpdateApp.class);
        toUpdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        this.startActivity(toUpdate);
    }

    public void updateLater(View v) {
        update_later = true;
        updateLayout.setVisibility(View.GONE);
        text_status.setText("Checking for registration details");
        gcmTask();
    }

    private static void normalRoutine() {

        if (!sp.getString("partner_id", "").equalsIgnoreCase("")) {
            Intent to_dash = new Intent(currentContext, MainActivity.class);
            to_dash.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            currentContext.startActivity(to_dash);
        } else {
            Intent to_login = new Intent(currentContext, LoginActivity.class);
            to_login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            currentContext.startActivity(to_login);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            normalRoutine();
                            if (!update_later) {
                                Intent serviceIntent = new Intent(currentContext, UpdateChekerService.class);
                                currentContext.startService(serviceIntent);
                            }
                        }
                    }, 2000);//reload my activity with permission granted or use the features what required the permission
                } else {
                    text_status.setText("Please grant write permissions to use application");
                    requestAccess.setVisibility(View.VISIBLE);
                }
            }
        }

    }

}
