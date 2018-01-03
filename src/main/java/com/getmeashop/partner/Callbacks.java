package com.getmeashop.partner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;


/**
 * interface for callbacks when Get or Post request are created
 */
public interface Callbacks {

    public void postexecute(String url, int status);

    public void preexecute(String url);

    public void processResponse(HttpResponse response, String url);

    public HttpPost preparePostData(String url, HttpPost httpPost);

}
