package com.getmeashop.realestate.partner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateApp extends AppCompatActivity {


    SharedPreferences sp;
    SharedPreferences.Editor editor;
    TextView text_status, whatsNew;
    ProgressBar progressBar, loading_spinner;
    Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);

        text_status = (TextView) findViewById(R.id.text_status);
        progressBar = (ProgressBar) findViewById(R.id.download_progress);
        loading_spinner = (ProgressBar) findViewById(R.id.progressBar);
        retry = (Button) findViewById(R.id.retry);
        whatsNew = (TextView) findViewById(R.id.whatsnew);

        sp = getSharedPreferences("Users", Context.MODE_PRIVATE);

        int versionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(sp.getInt("update_version", 0) > versionCode) {
                whatsNew.setText(sp.getString("update_whatsnew", ""));
            if(sp.getString("update_type","apk").equalsIgnoreCase("apk")) {
                UpdateApplication updateTask = new UpdateApplication();
                updateTask.execute(sp.getString("update_url", ""));
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sp.getString("update_url", "")));
                startActivity(browserIntent);
            }
        } else {
            Intent goToSplash = new Intent(this, SplashScreen.class);
            goToSplash.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToSplash);
        }




        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getString("update_type","apk").equalsIgnoreCase("apk")) {
                    UpdateApplication retry = new UpdateApplication();
                    retry.execute(sp.getString("update_url", ""));
                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sp.getString("update_url", "")));
                    startActivity(browserIntent);
                }
            }
        });
    }



    class UpdateApplication extends AsyncTask<String,Integer,Void> {
        boolean success = false;
        @Override
        protected Void doInBackground(String... arg0) {
            try {


                String PATH = "/mnt/sdcard/Download/.MbPartner/";
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, "update.apk");

                try {
                    if (outputFile.exists()) {
                        final PackageManager pm = getPackageManager();
                        PackageInfo info = pm.getPackageArchiveInfo(outputFile.getAbsolutePath(), 0);
                        if (info.versionCode >= sp.getInt("update_version", 0)) {
                            success = true;
                            return null;
                        }
                        outputFile.delete();

                        outputFile = new File(file, "update.apk");
                    }
                }catch (Exception e){
                    outputFile.delete();
                    outputFile = new File(file, "update.apk");
                }


                URL url = new URL(arg0[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                //connection.setDoOutput(true);
                connection.connect();

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                while ((len1 = is.read(buffer)) != -1) {

                    total += len1;
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));

                    fos.write(buffer, 0, len1);

                }
                fos.close();
                is.close();

                success = true;


            } catch (Exception e) {
                Log.d("UpdateAPP", "Update error! " + e.getMessage());
                success = false;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            //mProgressDialog.setIndeterminate(false);
            //mProgressDialog.setMax(100);
            //mProgressDialog.setProgress(progress[0]);
            if(progress[0] > 0) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setMax(100);
                progressBar.setProgress(progress[0]);
            }
            System.out.println("download progress " + progress);
        }

        @Override
        protected void onPostExecute(Void v) {
            // showDialog("Downloaded " + result + " bytes");
            text_status.setText("Please update to latest version");
            loading_spinner.setVisibility(View.GONE);
            if(success) {
                Utils.showToast(UpdateApp.this, "Please press update button");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File("/mnt/sdcard/Download/.MbPartner/update.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                startActivity(intent);
            } else {
                text_status.setText("failed to download, retry after some time");
                retry.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onPreExecute() {
            text_status.setText("Downloading Latest Version");
            retry.setVisibility(View.GONE);
        }


    }
}
