package com.getmeashop.realestate.partner.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.getmeashop.realestate.partner.SplashScreen;
import com.getmeashop.realestate.partner.PersistentCookieStore;
import com.getmeashop.realestate.partner.Utils;
import com.getmeashop.realestate.partner.util.Constants;
import com.google.android.gcm.GCMRegistrar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ServerUtilities {
    private static final String TAG = "Push GCM";
    private static Context context1;
    private static List<Cookie> loginCookies;
    private static SharedPreferences sp;
    private static String SERVER_URL;
    private static String regId1 = "";
    private static String username = "";

    /**
     * Register this account/device pair within the server.
     */
    public static void register(final Context context, String name,
                                String email, final String regId) {
        Log.i(TAG, "registering device (regId = " + regId + ")");
        regId1 = regId;
        context1 = context;
        username = name;
        SERVER_URL = Constants.base_uri + "partner/register-device/";
        new update_gcm_AsyncTask().execute();
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregister(final Context context, final String regId) {
        Log.i(TAG, "unregistering device (regId = " + regId + ")");
        String serverUrl = SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            String message = "unregistered from server";
            displayMessage(context, message);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            String message = "error unregistering form server" + e.getMessage();
            displayMessage(context, message);
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params   request parameters.
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {

        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            Log.e("URL", "> " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static void displayMessage(Context c, String msg) {// Toast.makeText(c,
        // msg,
        // Toast.LENGTH_SHORT).show();
        Log.d("gcm", msg);
    }

    private static class update_gcm_AsyncTask extends
            AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... arg0) {

            try {

                // HttpClient httpclient = new DefaultHttpClient();

                // HttpPost request = new HttpPost(URI.create(SERVER_URL));
                // HttpResponse response = httpclient.execute(request);
                HttpParams httpParameters = new BasicHttpParams();
                HttpContext localContext = new BasicHttpContext();

                try {

                    BasicCookieStore cookieStore2 = new BasicCookieStore();
                    cookieStore2.addCookie(new PersistentCookieStore(context1).getCookies().get(0));
                    // cookieStore2.addCookie(loginCookies.get(1));
                    // Log.d("tag","cookie"+loginCookies.get(0));
                    // Log.d("tag","cookie"+loginCookies.get(1));
                    localContext.setAttribute(ClientContext.COOKIE_STORE,
                            cookieStore2);
                } catch (Exception e) {

                }


                // Setup timeouts
                HttpConnectionParams
                        .setConnectionTimeout(httpParameters, 15000);
                HttpConnectionParams.setSoTimeout(httpParameters, 15000);

                HttpClient httpclient = new DefaultHttpClient(httpParameters);

                HttpPost httpPost = new HttpPost(SERVER_URL);
                httpPost.setHeader("Referer", SERVER_URL);
                httpPost.setHeader("X-CSRFToken",
                        new PersistentCookieStore(context1).getCookies().get(0).getValue());
                try {
                    sp = context1.getSharedPreferences("Users",
                            Context.MODE_PRIVATE);
                    httpPost.addHeader("Cookie", sp.getString("sessionid", ""));
                } catch (Exception e) {

                }
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("app", "partner-app"));
                nameValuePairs.add(new BasicNameValuePair("registration_id",
                        regId1));
                nameValuePairs.add(new BasicNameValuePair("device_id", Settings.Secure.getString(context1.getContentResolver(),
                        Settings.Secure.ANDROID_ID)));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse httpResponse = httpclient.execute(httpPost,
                        localContext);
                InputStream inputStream = httpResponse.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);
                System.out.println("The update time status code: "
                        + responseString.toString());

                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    GCMRegistrar.setRegisteredOnServer(context1, true);
                    String message = "registered on server";
                    displayMessage(context1, message);
                    SplashScreen.goToMain();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {

            // dialog.show();
        }

    }
}
