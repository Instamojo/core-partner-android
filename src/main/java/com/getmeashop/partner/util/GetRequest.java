package com.getmeashop.partner.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.getmeashop.partner.Callbacks;
import com.getmeashop.partner.LoginActivity;
import com.getmeashop.partner.PersistentCookieStore;
import com.getmeashop.partner.Utils;
import com.getmeashop.partner.database.DatabaseHandler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.SocketException;

/**
 * handles get request
 */
public class GetRequest {
    static final int CLIENT_PROTOCOL_ERROR = 90;
    static final int SOCKET_ERROR = 80;
    static final int IO_ERROR = 70;
    static final int FILE_NOT_FOUND = 60;

    Boolean success = true;
    PersistentCookieStore cookieStore;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    /**
     * constructor to get url and callback object and context
     * <p/>
     * hits a get request to url and calls callback functions
     *
     * @param url      Where request is to be made
     * @param callback object of callback
     * @param context
     */
    public GetRequest(final Callbacks callback, final String url,
                      final Context context) {

        sp = context.getSharedPreferences("Users",
                Context.MODE_PRIVATE);
        editor = sp.edit();
        class getrequest extends AsyncTask<String, Void, Integer> {

            @Override
            protected Integer doInBackground(String... arg0) {
                int statusCode = 0;
                try {
                    // HttpClient httpclient = new DefaultHttpClient();

                    HttpGet request = Parser.create_get_request(url);
                    try {
                        if(sp.getString("sessionid", "").length() !=0)
                            request.addHeader("Cookie",
                                    sp.getString("sessionid", "").substring(0, sp.getString("sessionid", "").indexOf("; ") +1 )/* + cookieStore.getCookies().get(0).getName() + "=" + cookieStore.getCookies().get(0).getValue() + ";"*/);
                        else
                            request.addHeader("Cookie", cookieStore.getCookies().get(0).getName() + "=" + cookieStore.getCookies().get(0).getValue() + ";");
                    } catch (Exception e) {

                    }
                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters,
                            10000);
                    HttpConnectionParams.setSoTimeout(httpParameters, 15000);
                    HttpClient httpclient = createHttpClient();
                    request.addHeader("X-Requested-From", "partner-app");
                    Log.e("url", url);
                    HttpResponse response = httpclient.execute(request);
                    statusCode = response.getStatusLine().getStatusCode();

                    cookieStore = Parser.store_cookies(httpclient, context);
                    success = true;

                    callback.processResponse(response, url);

                } catch (ClientProtocolException e) {
                    //error code 90
                    success = false;
                    statusCode = CLIENT_PROTOCOL_ERROR;
                    e.printStackTrace();
                } catch (ConnectTimeoutException e) {
                    success = false;
                    e.printStackTrace();
                } catch (SocketException e) {
                    success = false;
                    statusCode = SOCKET_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    // error code 70
                    success = false;
                    statusCode = IO_ERROR;
                    e.printStackTrace();
                } finally {
                    return statusCode;
                }
            }

            @Override
            protected void onPreExecute() {
                callback.preexecute(url);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Integer statusCode) {
                if (statusCode == 401) {
                    Utils.showToast("Your Session is expired, please Login again", context);
                    Intent to_reauthorize = new Intent(context, LoginActivity.class);
                    editor.clear();
                    editor.commit();
                    DatabaseHandler dbh = new DatabaseHandler(context);
                    dbh.deletedatabase();
                    to_reauthorize.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    to_reauthorize.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        to_reauthorize.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(to_reauthorize);
                } else if (success)
                    callback.postexecute(url, statusCode);
                else {
                    callback.postexecute("failed", statusCode);
                }

            }
        }
        if (Utils.isConnectingToInternet(context)) {
            new getrequest().execute();
        }
    }

    public static HttpClient createHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

        return new DefaultHttpClient(conMgr, params);
    }
}
