package com.getmeashop.partner.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;


import com.getmeashop.partner.Callbacks;
import com.getmeashop.partner.PersistentCookieStore;
import com.getmeashop.partner.Utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * handles post request
 */
public class PatchRequest extends HttpPost {
    Boolean success = true;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    public static final String METHOD_PATCH = "PATCH";
    private int statusCode;

    /**
     * constructor to get url and callback object and context
     * <p>
     * hits a get request to url and calls callback functions
     *
     * @param url
     *            Where request is to be made
     * @param callback
     *            object of callback
     */
    public PatchRequest(final Callbacks callback, final String url, final Context context) {

        class postrequest extends AsyncTask<String, Void, Integer> {

            @Override
            protected Integer doInBackground(String... arg0) {
                try {

                    sp = context.getSharedPreferences(Constants.User_sp,
                            Context.MODE_PRIVATE);
                    HttpParams httpParameters = new BasicHttpParams();
                    BasicCookieStore cookieStore2 = new BasicCookieStore();
                    PersistentCookieStore cookieStore = new PersistentCookieStore(context);
                    //cookieStore2.addCookie(cookieStore.getCookies().get(0));

                    // cookieStore2.addCookie(loginCookies.get(1));
                    HttpContext localContext = new BasicHttpContext();
                    localContext.setAttribute(ClientContext.COOKIE_STORE,
                            cookieStore2);

                    // Setup timeouts
                    HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                    HttpConnectionParams.setSoTimeout(httpParameters, 15000);
                    HttpClient httpclients = new DefaultHttpClient(
                            httpParameters);

                    PatchRequest httpPost = new PatchRequest(url);
                    httpPost.setHeader("Referer", url);
                    httpPost.setHeader("X-CSRFToken", cookieStore.getCookies()
                            .get(0).getValue());
                    try {
                        if(sp.getString("sessionid", "").length() !=0)
                            httpPost.addHeader("Cookie",
                                    sp.getString("sessionid", "").substring(0, sp.getString("sessionid", "").indexOf("; ") +1 ));
                        else
                            httpPost.addHeader("Cookie", cookieStore.getCookies().get(0).getName() + "=" + cookieStore.getCookies().get(0).getValue() + ";");
                    } catch (Exception e) {

                    }
                    PatchRequest httpPost1 = (PatchRequest) callback
                            .preparePostData(url, httpPost);
                    if (httpPost1 != null) {
                        httpPost = httpPost1;
                    }
                    HttpResponse response = httpclients.execute(httpPost,
                            localContext);
                    List<Cookie> cookies =  cookieStore2.getCookies();
                    for (Cookie cookie: cookies) {
                        if(cookie.getName().equals("csrftoken")){
                            cookieStore.clear();
                            cookieStore.addCookie(cookie);
                        }
                    }
                    statusCode = response.getStatusLine().getStatusCode();
                    editor = sp.edit();
                    try {
                        if (url.contains("login") || url.contains("registration")) {
                            Header[] head = response.getHeaders("Set-Cookie");
                            if (head != null) {
                                for (int i = 0; i < head.length; i++) {
                                    if (head[i].getValue().contains("sessionid")) {
                                        editor.putString("sessionid",
                                                head[i].getValue().substring(0,head[i].getValue().indexOf("; ") +1 ) );
                                        editor.commit();
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                    }

                    callback.processResponse(response, url);
                } catch (ClientProtocolException e) {
                    statusCode = GetRequest.CLIENT_PROTOCOL_ERROR;
                    success = false;
                    e.printStackTrace();
                }catch (ConnectTimeoutException e){
                    statusCode = GetRequest.IO_ERROR;
                    success = false;
                    e.printStackTrace();
                } catch (SocketTimeoutException e) {
                    statusCode = GetRequest.SOCKET_ERROR;
                    success = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    statusCode = GetRequest.IO_ERROR;
                    success = false;
                    e.printStackTrace();
                }
                return statusCode;
            }

            @Override
            protected void onPreExecute() {
                callback.preexecute(url);
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Integer status) {
                if (success)
                    callback.postexecute(url,status);
                else {
                    callback.postexecute("failed", status);
                }
            }
        }
        if (Utils.isConnectingToInternet(context))
            new postrequest().execute();
    }

    public PatchRequest(final String url) {
        super(url);
    }
    @Override
    public String getMethod() {
        return METHOD_PATCH;
    }

}
