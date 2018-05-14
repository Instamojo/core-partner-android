package com.getmeashop.partner;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.getmeashop.partner.database.DatabaseHandler;
import com.getmeashop.partner.database.User;
import com.getmeashop.partner.util.Constants;
import com.getmeashop.partner.util.Interfaces;
import com.getmeashop.partner.util.Parser;
import com.getmeashop.partner.util.PutRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArchivedUserList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArchivedUserList extends Fragment implements Callbacks, Interfaces.shouldNotify, Interfaces.archive, Interfaces.SearchResultCount, Interfaces.PutCallbacks {

    ArrayList<User> users;
    User dlt_user;
    RecyclerView list;
    UserListAdapter adapter;
    ProgressDialog dialog;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Boolean refreshing = false;
    Boolean notify = false;
    String delete_url, id;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    boolean request_in_progress = false;
    String users_next, uri_next;
    FloatingActionButton adduser;
    int dlt_positon;
    EditText mtxt;
    Button btnSearch;

    TextView no_result;

    String uri_get_db_search = "", previous_query = "", uri_get_user;

    boolean firstSearch = true, search_done = false, failed = false, dialog_shown = false;

    public ArchivedUserList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserList.
     */
    // TODO: Rename and change types and number of parameters
    public static ArchivedUserList newInstance() {
        ArchivedUserList fragment = new ArchivedUserList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.archived_user_list, container, false);


        list = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        no_result = (TextView) view.findViewById(R.id.no_result);
        adduser = (FloatingActionButton) view.findViewById(R.id.fab);

        btnSearch = (Button) view.findViewById(R.id.btnsearch);
        mtxt = (EditText) view.findViewById(R.id.edSearch);


        final DatabaseHandler dbh = new DatabaseHandler(getActivity());
        list.setHasFixedSize(true);

        adduser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                ((Interfaces.goToFrag) getActivity()).gotoFragment(Constants.FragAddUser);
            }
        });

        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);
        uri_get_db_search = Constants.uri_db_search + mtxt.getText().toString();

        sp = getActivity().getSharedPreferences(Constants.User_sp, Context.MODE_PRIVATE);
        editor = sp.edit();


        users = dbh.getAllArchivedUsers();
        if (users.size() == 0) {
            no_result.setVisibility(View.VISIBLE);
        } else {
            no_result.setVisibility(View.GONE);

            adapter = new UserListAdapter(getActivity(), users, this);

            list.setAdapter(adapter);
        }

        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Getting user profiles, Please wait...");
        dialog.setCancelable(false);





        mtxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(adapter != null)
                    adapter.setFilter(mtxt.getText().toString());
            }
        });
        

        return view;
    }


    @Override
    public void postexecute(String url, int status) {

        if (status == 200) {
            if (url.equalsIgnoreCase(uri_next)) {
                notifyData(true);
                request_in_progress = false;
            } else if (url.equalsIgnoreCase(Constants.uri_get_user) || url.equalsIgnoreCase(uri_get_user)) {
                notifyData(false);
                request_in_progress = false;
            } else if (url.equalsIgnoreCase(uri_get_db_search + mtxt.getText().toString())) {
                DatabaseHandler dbh = new DatabaseHandler(getActivity());
                users = dbh.getAllArchivedUsers();
                adapter.setFilter(mtxt.getText().toString(), users);
                request_in_progress = false;
            } else if (url.equalsIgnoreCase(delete_url)) {
                Utils.showToast("User updated successfully.", getActivity());
                DatabaseHandler dbh = new DatabaseHandler(getActivity());
                dlt_user = dbh.getAllArchivedUsers().get(dlt_positon);
                users.set(dlt_positon, dlt_user);
                adapter.notifyDataSetChanged();
            }
        }  else if(url.equalsIgnoreCase(delete_url) && status == 400){
            Utils.showToast("Something went wrong, please refresh userlist.", getActivity());
        } else if (url.equalsIgnoreCase("failed")) {
            notifyData(false);
            failed = true;
            Utils.showToast("Connection problem, please try again later", getActivity());
        } else {
            Utils.showToast("Connection problem, please try again later", getActivity());
        }
        if (!refreshing && !url.equalsIgnoreCase(uri_next))
            dialog.dismiss();
        refreshing = false;
    }

    @Override
    public void preexecute(String url) {
        if (!refreshing && !url.equalsIgnoreCase(uri_next)) {
            dialog.setMessage("Getting user list please wait");
            dialog.show();
        }
        if (url.equalsIgnoreCase(delete_url)) {
            dialog.setMessage("Updating user, please wait");
            dialog.show();
        }
    }

    @Override
    public void processResponse(HttpResponse response, String url) {

        if (response.getStatusLine().getStatusCode() == 200) {
            InputStream inputStream = null;
            try {

                inputStream = response.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);

                System.out.println("Login server response__" + responseString);
                if (url.equalsIgnoreCase(uri_get_user) || url.equalsIgnoreCase(Constants.uri_get_user))
                    Parser.reset_user_db(responseString, getActivity(), editor, false, 0, true);
                else if (url.equalsIgnoreCase(uri_next))
                    Parser.reset_user_db(responseString, getActivity(), editor, false, 0, false);
                else if (url.equalsIgnoreCase(uri_get_db_search + mtxt.getText().toString())) {
                    Parser.reset_user_db(responseString, getActivity(), editor, true, 1, false);
                    search_done = true;
                    previous_query = url;
                }else if(url.equalsIgnoreCase(delete_url)){

                    System.out.println("Login server response__" + responseString);
                    Parser.update_user_db(responseString, getActivity(), dlt_user.getId());

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    @Override
    public HttpPut preparePutData(String url, HttpPut httpPost) {

        if(url.equalsIgnoreCase(delete_url)) {
            User user = dlt_user;
            if (user.getIsActv().equalsIgnoreCase("true"))
                user.setIsActv("false");
            else
                user.setIsActv("true");

            Parser.setUserData(false, httpPost, user);
        }
        return httpPost;
    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {
        return null;
    }


    private void notifyData(Boolean update) {

        DatabaseHandler dbh = new DatabaseHandler(getActivity());
        if (!update) {
            users = dbh.getAllArchivedUsers();
            adapter = new UserListAdapter(getActivity(), users, this);
            list.setAdapter(adapter);
        } else {
            ArrayList<User> temp = dbh.getAllArchivedUsers();
            int i = users.size() - 1;
            users.remove(i);
            for (; i < temp.size(); i++) {
                users.add(i, temp.get(i));
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notify)
            notifyData(false);
        notify = false;
    }

    @Override
    public void shouldNotify() {
        notify = true;
    }

    @Override
    public void archive(User user) {

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(user.getUsername())) {
                users.remove(i);
                break;
            }
        }
        //users.remove(user);
        if (users.size() == 0) {
            no_result.setVisibility(View.VISIBLE);
        } else {
            no_result.setVisibility(View.GONE);
        }
    }

    @Override
    public void delete(User user, int pos) {
        createDeleteDialog(user, pos);
    }

    private void createDeleteDialog(final User user, final int pos){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String action = "";
        if(user.getIsActv().equalsIgnoreCase("true")){
            action = "Deactivate";
        }else{
            action = "Activate";
        }

        builder.setTitle(action +  " " + user.getUsername());
        builder.setMessage("Do you really want to " + action.toLowerCase() + " user ?");
        builder.setPositiveButton(action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dlt_positon = pos;
                dlt_user = user;
                delete_url = Constants.base_uri1 + user.getR_uri();
                new PutRequest(ArchivedUserList.this, Constants.base_uri1 + user.getR_uri(), getActivity());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }
    @Override
    public void SearchItemsCount(int num) {
        if (num == 0) {
            if (!search_done && !failed && !dialog_shown) {
                Utils.showToast(getActivity(), "No Product found in local DataBase, click Search icon to search whole database");
                dialog_shown = true;
            } else if (search_done) {
                search_done = false;
                failed = false;
                Utils.showToast("No results found", getActivity());
            }
        } else if (firstSearch) {
            Utils.showToast("Click Search icon to search whole database", getActivity());
            firstSearch = false;
        }
    }

}
