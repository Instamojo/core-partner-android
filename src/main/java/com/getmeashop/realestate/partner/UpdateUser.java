package com.getmeashop.realestate.partner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.getmeashop.realestate.partner.database.DatabaseHandler;
import com.getmeashop.realestate.partner.database.User;
import com.getmeashop.realestate.partner.util.Constants;
import com.getmeashop.realestate.partner.util.Interfaces;
import com.getmeashop.realestate.partner.util.Parser;
import com.getmeashop.realestate.partner.util.PutRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class UpdateUser extends AppCompatActivity implements Interfaces.PutCallbacks {

    String id;
    User prevUser;
    String uri_update;
    Button btn_update;
    ProgressDialog dialog;
    SharedPreferences sp;
    String error_msg = "";
    SharedPreferences.Editor editor;
    EditText Username, Fname, Lname, Email, Password, C_Password, Contact;
    private Toolbar toolbar;
    CheckBox active;
    private EditText city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseHandler dbh = new DatabaseHandler(this);
        id = getIntent().getStringExtra("id");
        prevUser = dbh.getUserById(id);

        sp = getSharedPreferences(Constants.User_sp, Context.MODE_PRIVATE);
        editor = sp.edit();

        Username = (EditText) findViewById(R.id.username);
        Fname = (EditText) findViewById(R.id.fname);
        Lname = (EditText) findViewById(R.id.lname);
        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);
        C_Password = (EditText) findViewById(R.id.c_password);
        Contact = (EditText) findViewById(R.id.contact);
        city = (EditText) findViewById(R.id.city);
        btn_update = (Button) findViewById(R.id.update_button);
        active = (CheckBox) findViewById(R.id.active);

        Username.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(Username, InputMethodManager.SHOW_IMPLICIT);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Updating user profile, Please wait...");
        dialog.setCancelable(false);

        uri_update = Constants.base_uri1 + prevUser.getR_uri();
        Username.setText(prevUser.getUsername());
        Fname.setText(prevUser.getFname());
        Lname.setText(prevUser.getLname());
        Email.setText(prevUser.getEmail());
        Password.setText(prevUser.getPassword());
        Password.setTypeface(Typeface.DEFAULT);
        Password.setTransformationMethod(new PasswordTransformationMethod());
        C_Password.setTypeface(Typeface.DEFAULT);
        C_Password.setTransformationMethod(new PasswordTransformationMethod());
        C_Password.setText(prevUser.getPassword());
        Contact.setText(prevUser.getContact());
        city.setText(prevUser.getCity());
        active.setChecked(!prevUser.getIsActv().equalsIgnoreCase("false"));

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid()) {
                    new PutRequest((Interfaces.PutCallbacks) UpdateUser.this, uri_update, getApplicationContext());
                }
            }
        });

    }


    @Override
    public void postexecute(String url, int status) {
        if (url.equalsIgnoreCase(uri_update) && status == 200) {
            dialog.dismiss();
            Utils.showToast("User updated successfully", getApplicationContext());
            finish();
        } else {
            if (!error_msg.equalsIgnoreCase("")) {
                Utils.showToast(error_msg, getApplicationContext());
                error_msg = "";
            } else {
                Utils.showToast("Something went wrong, Please try again later", getApplicationContext());
            }
            dialog.dismiss();
        }

    }

    @Override
    public void preexecute(String url) {
        dialog.show();
    }

    @Override
    public void processResponse(HttpResponse response, String url) {
        if (response.getStatusLine().getStatusCode() == 200 && url.equalsIgnoreCase(uri_update)) {
            InputStream inputStream = null;
            try {
                inputStream = response.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);

                System.out.println("Login server response__" + responseString);
                Parser.update_user_db(responseString, getApplicationContext(), prevUser.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            InputStream inputStream = null;
            try {
                inputStream = response.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);

                System.out.println("Login server response__" + responseString);
                JSONObject error = null;
                try {
                    error = new JSONObject(responseString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                error_msg = error.optJSONObject("error").optString("message");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public HttpPut preparePutData(String url, HttpPut httpPost) {

        User user = new User(Fname.getText().toString(), Lname.getText().toString(), prevUser.getPid(), Username.getText().toString(),
                Email.getText().toString(), Password.getText().toString(), prevUser.getR_uri(),
                Contact.getText().toString(), "", active.isChecked() + "", "", "", city.getText().toString());


        Parser.setUserData(true, httpPost, user);
        return null;
    }

    public boolean valid() {
        if (Username.getText().toString().length() == 0) {
            Utils.showToast("Please Enter a Username", getApplicationContext());
        } else if (Fname.getText().toString().length() == 0) {
            Utils.showToast("Please Enter a fast name", getApplicationContext());
        } else if (Lname.getText().toString().length() == 0) {
            Utils.showToast("Please Enter a last name", getApplicationContext());
        } else if (Email.getText().toString().length() == 0) {
            Utils.showToast("Please Enter an Email", getApplicationContext());
        } else if (prevUser.getPassword().length() != 0 && Password.getText().length() == 0
                && C_Password.getText().toString().length() == 0) {
            Utils.showToast("Password will remain unchanged", getApplicationContext());
            return true;
        } else if (Password.getText().length() != 0 && Password.getText().toString().length() < 4) {
            Utils.showToast("Please enter a valid password (at least 4 characters)", getApplicationContext());
        } else if (!Password.getText().toString().equalsIgnoreCase(C_Password.getText().toString())) {
            Utils.showToast("Password do not match, Please check", getApplicationContext());
        } else if (Contact.getText().toString().length() > 0 && Contact.getText().toString().length() != 10) {
            Utils.showToast("Please enter a valid contact number (10 digits)", getApplicationContext());
        } else {
            return true;
        }
        return false;
    }
}
