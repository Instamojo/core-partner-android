package com.getmeashop.realestate.partner;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.getmeashop.realestate.partner.database.User;
import com.getmeashop.realestate.partner.util.Constants;
import com.getmeashop.realestate.partner.util.Parser;
import com.getmeashop.realestate.partner.util.PostRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddUser extends Fragment implements Callbacks {
    String uri_add;
    ProgressDialog dialog;
    SharedPreferences sp;

    SharedPreferences.Editor editor;
    EditText Username, Fname, Lname, Email, Password, C_Password, Contact;
    Button add_user;
    CheckBox active;
    private String responseString;
    private EditText city;

    public AddUser() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_user, container, false);
        Username = (EditText) v.findViewById(R.id.username);
        Fname = (EditText) v.findViewById(R.id.fname);
        Lname = (EditText) v.findViewById(R.id.lname);
        Email = (EditText) v.findViewById(R.id.email);
        Password = (EditText) v.findViewById(R.id.password);
        Password.setTransformationMethod(new PasswordTransformationMethod());
        C_Password = (EditText) v.findViewById(R.id.c_password);
        C_Password.setTransformationMethod(new PasswordTransformationMethod());
        Contact = (EditText) v.findViewById(R.id.contact);
        city = (EditText) v.findViewById(R.id.city);
        add_user = (Button) v.findViewById(R.id.update_button);
        active = (CheckBox) v.findViewById(R.id.active);


        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Adding user profile, Please wait...");
        dialog.setCancelable(false);

        uri_add = Constants.uri_send_user;

        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid()) {
                    new PostRequest((Callbacks) AddUser.this, uri_add, getActivity());
                }
            }
        });
        return v;
    }


    @Override
    public void postexecute(String url, int status) {
        if (url.equalsIgnoreCase(uri_add) && status == 201) {
            Utils.showToast("User added successfully", getActivity());
            resetfields();
            Username.requestFocus();
            dialog.dismiss();
        } else {
            if (!responseString.equalsIgnoreCase("")) {
                Utils.showToast(responseString, getActivity());
                responseString = "";
            } else {
                Utils.showToast("Something went wrong, Please try again later", getActivity());
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
        if (response.getStatusLine().getStatusCode() == 201 && url.equalsIgnoreCase(uri_add)) {
            InputStream inputStream = null;
            try {
                inputStream = response.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);

                System.out.println("Login server response__" + responseString);
                Parser.add_to_user_db(responseString, getActivity(), editor, false, 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            InputStream inputStream = null;
            try {
                inputStream = response.getEntity().getContent();
                responseString = Utils
                        .convertInputStreamToString(inputStream);

                System.out.println("Login server response__" + responseString);
                JSONObject error = null;
                try {
                    error = new JSONObject(responseString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                responseString = error.optJSONObject("error").optString("message");


                //Parser.reset_user_db(responseString, getActivity(), editor, true, url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {
        User user = new User(Fname.getText().toString(), Lname.getText().toString(), "", Username.getText().toString(),
                Email.getText().toString(), Password.getText().toString(), "",
                Contact.getText().toString(), "", active.isChecked() + "", "", "", city.getText().toString());

        return Parser.setUserData(getActivity(), httpPost, user);
    }


    public boolean valid() {
        if (Username.getText().toString().length() == 0) {
            Utils.showToast("Please Enter a Username", getActivity());
        } else if (Fname.getText().toString().length() == 0) {
            Utils.showToast("Please Enter a fast name", getActivity());
        } else if (Lname.getText().toString().length() == 0) {
            Utils.showToast("Please Enter a last name", getActivity());
        } else if (Email.getText().toString().length() == 0) {
            Utils.showToast("Please Enter an Email", getActivity());
        } else if (city.getText().toString().length() == 0) {
            Utils.showToast("Please Enter City", getActivity());
        } else if (Password.getText().length() < 4) {
            Utils.showToast("Please enter a valid password (at least 4 characters)", getActivity());
        } else if (Contact.getText().toString().length() > 0 && Contact.getText().toString().length() != 10) {
            Utils.showToast("Please enter a valid contact number (10 digits)", getActivity());
        } else if (!Password.getText().toString().equalsIgnoreCase(C_Password.getText().toString())) {
            Utils.showToast("Password do not match, Please check", getActivity());
        } else {
            return true;
        }
        return false;
    }

    public void resetfields() {
        Username.setText("");
        Fname.setText("");
        Lname.setText("");
        Password.setText("");
        C_Password.setText("");
        Email.setText("");
        Contact.setText("");
        city.setText("");
        active.setChecked(true);
    }

}
