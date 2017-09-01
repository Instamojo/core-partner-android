package com.getmeashop.realestate.partner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getmeashop.realestate.partner.gcm.ServerUtilities;
import com.getmeashop.realestate.partner.util.Constants;
import com.google.android.gcm.GCMBaseIntentService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Mainly to handle push notification
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";
    static String SENDER_ID = Constants.GCM_SENDER_ID;
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static int i = 0;
    SharedPreferences sp;

    public GCMIntentService() {
        super(SENDER_ID);
    }

    private static void generateNotification(Context context,
                                             JSONObject notif_data) {
        String title = null;
        String message = null;
        String type = "none";
        Log.d("notification data", "hello" + notif_data);
        try {
            title = notif_data.optString("title");
            message = notif_data.optString("message");
            type = notif_data.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Bitmap btm = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title).setContentText(message);
        mBuilder.setTicker(message);

        Uri alarmSound = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        long[] pattern = {10, 200, 10, 500, 200};
        mBuilder.setVibrate(pattern);
        // mBuilder.setLargeIcon(btm);
        mBuilder.setAutoCancel(true);// Their maintenance notice disappear
        Intent resultIntent = new Intent(context, SplashScreen.class);
        // Building a Intent
        if (type.equalsIgnoreCase("order")) {
            resultIntent.putExtra("goto", "order");
        } else if (type.equalsIgnoreCase("notification"))
            resultIntent.putExtra("goto", "notification");

        // A package of Intent

        if (!type.equalsIgnoreCase("menu") || !type.equalsIgnoreCase("testimonial")) {
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                    i, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            // Notice of intention to set the theme
            mBuilder.setContentIntent(resultPendingIntent);
            // Get notification manager object
            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(i, mBuilder.build());
            i++;
        }
    }

    /**
     * Method called on device registered
     */
    @Override
    protected void onRegistered(Context context, String registrationId) {
        sp = getSharedPreferences("Users", Context.MODE_PRIVATE);
        String username = sp.getString("userName", "test");
        Log.d(TAG, "Device registered: regId = " + registrationId);
        Log.d("NAME", username);
        ServerUtilities.register(context, username, username, registrationId);
        // SplashScreen.registerOnServer(registrationId);
    }

    /**
     * Method called on device un registred
     */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        // ServerUtilities.unregister(context, registrationId);
    }


    private void BroadCast(int versionCode, boolean mandatory, String whatsNew, String url, String type) {
        Log.d(TAG, "entered DisplayLoggingInfo");
        sp = getSharedPreferences("Users", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = sp.edit();

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

    /**
     * Method called on Receiving a new message
     */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");

        try {
            String type = intent.getExtras().getString("type");
            if (type.equalsIgnoreCase("offer")) {
                SharedPreferences.Editor editor = context.getSharedPreferences("Users", Context.MODE_PRIVATE).edit();
                editor.putBoolean("offers_updated", false);
                editor.commit();
            } else if (type.equalsIgnoreCase("testimonial")) {
                SharedPreferences.Editor editor = context.getSharedPreferences("Users", Context.MODE_PRIVATE).edit();
                editor.putBoolean("testimonial_updated", false);
                editor.commit();
            } else if (type.equalsIgnoreCase("menu")) {
                SharedPreferences.Editor editor = context.getSharedPreferences("Users", Context.MODE_PRIVATE).edit();
                editor.putBoolean("menu_updated", false);
                editor.commit();
            } else if (type.equalsIgnoreCase("menuimages")) {
                SharedPreferences.Editor editor = context.getSharedPreferences("Users", Context.MODE_PRIVATE).edit();
                editor.putBoolean("menu_image_updated", false);
                editor.commit();
            }else if(intent.getExtras().getString("type").equals("block"))
            {
                sp = getSharedPreferences("Users", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor;
                editor = sp.edit();

                editor.putBoolean("block", true);
                editor.putString("block_reason", intent.getExtras().getString("message"));

                editor.commit();

                Intent toUpdate = new Intent(this, SplashScreen.class);
                toUpdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                this.startActivity(toUpdate);
            }
            else if(intent.getExtras().getString("type").equals("unblock"))
            {
                sp = getSharedPreferences("Users", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor;
                editor = sp.edit();

                editor.putBoolean("block", false);
                editor.putString("block_reason", "");

                editor.commit();

                Intent toUpdate = new Intent(this, SplashScreen.class);
                toUpdate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                this.startActivity(toUpdate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message = intent.getExtras().getString("message");

        // notifies user
        if (message != null && !message.equalsIgnoreCase("")) {
            JSONObject notification_data = new JSONObject();
            try {if(intent.getExtras().getString("type").equals("update")) {
                notification_data.put("message","New update Available");

                try {
                    JSONObject jsonMsg = new JSONObject(message);
                    BroadCast(Integer.parseInt(jsonMsg.getString("versioncode")), Boolean.parseBoolean(jsonMsg.getString("mandatory")), "Bug Fixes", jsonMsg.getString("url"), jsonMsg.getString("type"));
                } catch (JSONException e) {
                }
            }else {
                notification_data.put("message",
                        intent.getExtras().getString("message"));
            }
                notification_data.put("title", intent.getExtras()
                        .getString("title"));
                notification_data.put("type", intent.getExtras().getString("type"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            generateNotification(context, notification_data);
        }
    }

    /**
     * Method called on receiving a deleted message
     */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        // notifies user
        JSONObject notification_data = new JSONObject();
        try {
            notification_data.put("message",
                    getString(R.string.gcm_deleted, total));
            notification_data.put("title", "");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        generateNotification(context, notification_data);
    }

    /**
     * Method called on Error
     */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

}