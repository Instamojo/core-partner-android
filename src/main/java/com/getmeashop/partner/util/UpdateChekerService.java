package com.getmeashop.partner.util;

/**
 * Created by naveenkumar on 05/11/15.
 */
import java.io.IOException;
import java.io.InputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


import com.getmeashop.partner.Callbacks;
import com.getmeashop.partner.UpdateApp;
import com.getmeashop.partner.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateChekerService extends Service implements Callbacks {
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "broadcast";
    Intent intent, serviceIntent;
    long timeout = 10000;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String whatsNew, type, versionCode = "0", urlJson, mandatory;

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(BROADCAST_ACTION);

        sp = getSharedPreferences("Users", Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        serviceIntent = intent;
        checkUpdate();
    }


    void checkUpdate(){
        if(Utils.isConnectingToInternetWithoutToast(UpdateChekerService.this)) {
            new GetRequest(UpdateChekerService.this, Constants.update_check_url, UpdateChekerService.this);
        } else if(timeout < 1000000){
            //Utils.showSnack(UpdateChekerService.this, "Unable to check for updates,Please connect to internet");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkUpdate();
                }
            }, timeout);
            timeout = timeout*10;
        }
    }
    private void BroadCast(int versionCode, boolean mandatory, String whatsNew, String url, String type) {
        Log.d(TAG, "entered DisplayLoggingInfo");
        editor.putInt("update_version", versionCode);
        editor.putBoolean("update_mandatory", mandatory);
        editor.putString("update_whatsnew", whatsNew);

        editor.putString("update_url", url);
        editor.putString("update_type", type);
        editor.commit();
        Intent toUpdate = new Intent(this, UpdateApp.class);

        if(mandatory)
            toUpdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        else
            toUpdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        this.startActivity(toUpdate);

//
//        intent.putExtra("update", versionCode);
//        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serviceIntent != null)
            stopService(serviceIntent);
    }

    @Override
    public void postexecute(String url, int statusCode) {
        if(statusCode == 200){
            int versionCode2 = 0;
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionCode2 = pInfo.versionCode;


            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if((Integer.parseInt(versionCode) > versionCode2)) {
                BroadCast(Integer.parseInt(versionCode), Boolean.parseBoolean(mandatory) , whatsNew, urlJson, type );
            }
            onDestroy();
        } else {
            onDestroy();
        }
    }

    @Override
    public void preexecute(String url) {

    }

    @Override
    public void processResponse(HttpResponse response, String url) {
        if(response.getStatusLine().getStatusCode() == 200) {
            try {
                InputStream inputStream = response.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);
                try {
                    JSONObject jsonResponce = new JSONObject(responseString);
                    whatsNew = jsonResponce.getString("whatsnew");
                    versionCode = jsonResponce.getString("versioncode");
                    urlJson = jsonResponce.getString("url");
                    type = jsonResponce.getString("type");
                    mandatory = jsonResponce.getString("mandatory");
                } catch (JSONException e) {

                }


            } catch (IOException e) {
                Log.e("error", e.toString());
            }
        }

    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {
        return null;
    }

    @Override
    public boolean stopService(Intent name) {
        // TODO Auto-generated method stub
        return super.stopService(name);

    }
}