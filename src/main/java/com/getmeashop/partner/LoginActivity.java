package com.getmeashop.partner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getmeashop.partner.util.Constants;
import com.getmeashop.partner.util.GetRequest;
import com.getmeashop.partner.util.PostRequest;
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


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements Callbacks {


    ProgressDialog dialog;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String uri_login, uri_csrf;
    private boolean loginStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        //populateAutoComplete();
        uri_csrf = Constants.base_uri + "partner/login/?request=csrf";
        uri_login = Constants.base_uri + "partner/login/?format=json";

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Authenticating please wait...");
        dialog.setCancelable(false);


        sp = this.getSharedPreferences(Constants.User_sp, Context.MODE_PRIVATE);
        editor = sp.edit();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the User entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the User login attempt.

            new GetRequest((Callbacks) LoginActivity.this, uri_csrf, getApplicationContext());

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }


    @Override
    public void postexecute(String url, int status) {
        if (url.equalsIgnoreCase(uri_csrf)) {
            new PostRequest((Callbacks) LoginActivity.this, uri_login, getApplicationContext());
        } else if (url.equalsIgnoreCase(uri_login)) {
            dialog.dismiss();
            if (status != 200) {
                Toast.makeText(getApplicationContext(), "Invalid combination of username and password",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                goToDash();
            }
        } else if (url.equalsIgnoreCase("failed")) {
            Toast.makeText(getApplicationContext(),
                    "Your connection is too slow, Please try again later",
                    Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }

    @Override
    public void preexecute(String url) {
        if (url.equalsIgnoreCase(uri_csrf)) {
            dialog.setMessage("Initializing secure connection...");
            dialog.show();
        } else if (url.equalsIgnoreCase(uri_login)) {
            dialog.setMessage("Authenticating please wait...");
            dialog.show();
        }
    }

    @Override
    public void processResponse(HttpResponse response, String url) {

        try {
            if (response.getStatusLine().getStatusCode() == 200) {
                if (url.equalsIgnoreCase(uri_login)) {
                    InputStream inputStream = response.getEntity().getContent();
                    String responseString = Utils
                            .convertInputStreamToString(inputStream);
                    System.out.println("Login server response__" + responseString);

                    JSONObject myObject = new JSONObject(responseString);

                    editor.putString("userName", mEmailView.getText().toString().toLowerCase());
                    editor.putString("password", mPasswordView.getText()
                            .toString());

                    editor.putString("partner_id", myObject.optString("partner_id"));
                    editor.commit();
                    /*String result = myObject.get("status").toString();
                    if (result.equals("success")) {

                         editor.commit();
                        loginStatus = true;
                    }*/
                }
            } else {
                InputStream inputStream = response.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);
                System.out.println("Login server response__" + responseString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            loginStatus = false;
            e.printStackTrace();
        }

    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {
        if (url.equalsIgnoreCase(uri_login)) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("csrfmiddlewaretoken",
                    new PersistentCookieStore(getApplicationContext()).getCookies().get(0).getValue()));
            nameValuePairs.add(new BasicNameValuePair("email", mEmailView
                    .getText().toString().toLowerCase()));
            nameValuePairs.add(new BasicNameValuePair("password", mPasswordView
                    .getText().toString()));
            nameValuePairs.add(new BasicNameValuePair("device_id", Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID)));

            GCMRegistrar.checkDevice(LoginActivity.this);
            nameValuePairs.add(new BasicNameValuePair("registration_id", GCMRegistrar.getRegistrationId(LoginActivity.this)));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return httpPost;
        }
        return null;
    }

    public void goToDash() {
        Utils.showToast(getApplicationContext(), "Login successful");
        Intent to_dash = new Intent(this, MainActivity.class);
        to_dash.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(to_dash);
    }
}

