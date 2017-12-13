package com.getmeashop.realestate.partner;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.getmeashop.realestate.partner.database.DatabaseHandler;
import com.getmeashop.realestate.partner.database.User;
import com.getmeashop.realestate.partner.util.Constants;
import com.getmeashop.realestate.partner.util.GetRequest;
import com.getmeashop.realestate.partner.util.Interfaces;
import com.getmeashop.realestate.partner.util.Parser;
import com.getmeashop.realestate.partner.util.PatchRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserList extends Fragment implements Callbacks, Interfaces.shouldNotify, Interfaces.archive, Interfaces.SearchResultCount, Interfaces.PutCallbacks {

    ArrayList<User> users;
    User dlt_user;
    RecyclerView list;
    UserListAdapter adapter;
    ProgressDialog dialog;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    SwipeRefreshLayout swipeLayout;
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

    public UserList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserList.
     */
    // TODO: Rename and change types and number of parameters
    public static UserList newInstance(boolean archived) {
        UserList fragment = new UserList();
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
        View view = inflater.inflate(R.layout.user_list, container, false);

        list = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        no_result = (TextView) view.findViewById(R.id.no_result);
        adduser = (FloatingActionButton) view.findViewById(R.id.fab);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        btnSearch = (Button) view.findViewById(R.id.btnsearch);
        mtxt = (EditText) view.findViewById(R.id.edSearch);

        swipeLayout.setColorSchemeResources(
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);
        final DatabaseHandler dbh = new DatabaseHandler(getActivity());
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                if (mtxt.getText().toString().length() > 0)
                    mtxt.setText("");
                uri_get_user = Constants.uri_send_user + "?format=json&order_by=-modified";
                new GetRequest((Callbacks) UserList.this, uri_get_user, getActivity());
            }
        });
        //  list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
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


        users = dbh.getAllUsers();

        adapter = new UserListAdapter(getActivity(), users, this);

        list.setAdapter(adapter);

        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Getting user profiles, Please wait...");
        dialog.setCancelable(false);

        if (dbh.getAllUsers().size() == 0) {
            new GetRequest((Callbacks) UserList.this, Constants.uri_get_user, getActivity());
        }


        list.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = llm.getItemCount();
                firstVisibleItem = llm.findFirstVisibleItemPosition();


                if ((totalItemCount - visibleItemCount)
                        <= firstVisibleItem) {
                    // End has been reached
                    DatabaseHandler dbh = new DatabaseHandler(getActivity());
                    users_next = dbh.getMinModified();
                    if (!users_next.equalsIgnoreCase("") && mtxt.getText().toString().length() == 0
                            && sp.getBoolean("users_more", false)
                            && !request_in_progress && users.size() > 0 && Utils.isConnectingToInternet(getActivity())) {
                        Log.d("end ", "grid end " + users_next);
                        users.add(new User());
                        users.get(users.size() - 1).setUsername("load+more");
                        Log.d("end out ", "grid end " + users_next + request_in_progress);
                        adapter.notifyDataSetChanged();
                        request_in_progress = true;
                        uri_next = Constants.uri_get_user + "&modified__lte=" + users_next;
                        new GetRequest((Callbacks) UserList.this, uri_next, getActivity());
                    }
                }
            }
        });

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
                if (adapter != null)
                    adapter.setFilter(mtxt.getText().toString());
            }
        });


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mtxt.getText().toString().length() != 0 && previous_query != uri_get_db_search + mtxt.getText().toString())
                    new GetRequest((Callbacks) UserList.this, uri_get_db_search + mtxt.getText().toString(), getActivity());
                else {
                    Utils.showToast("no result found", getActivity());
                }
            }
        });

        return view;
    }


    @Override
    public void postexecute(String url, int status) {

        if (status == 200 || status == 202) {
            if (url.equalsIgnoreCase(uri_next)) {
                notifyData(true);
                request_in_progress = false;
            } else if (url.equalsIgnoreCase(Constants.uri_get_user) || url.equalsIgnoreCase(uri_get_user)) {
                notifyData(false);
                request_in_progress = false;
            } else if (url.equalsIgnoreCase(uri_get_db_search + mtxt.getText().toString())) {
                DatabaseHandler dbh = new DatabaseHandler(getActivity());
                users = dbh.getAllUsers();
                adapter.setFilter(mtxt.getText().toString(), users);
                request_in_progress = false;
            } else if (url.equalsIgnoreCase(delete_url)) {
                Utils.showToast("User updated successfully.", getActivity());
                DatabaseHandler dbh = new DatabaseHandler(getActivity());
                dlt_user = dbh.getAllUsers().get(dlt_positon);
                users.set(dlt_positon, dlt_user);
                adapter.notifyDataSetChanged();
            }
        } else if (url.equalsIgnoreCase(delete_url) && status == 400) {
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
        swipeLayout.setRefreshing(false);
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
        InputStream inputStream = null;
        try {

            inputStream = response.getEntity().getContent();
            String responseString = Utils
                    .convertInputStreamToString(inputStream);

            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 202) {

                Log.e("server response", responseString);
                if (url.equalsIgnoreCase(uri_get_user) || url.equalsIgnoreCase(Constants.uri_get_user))
                    Parser.reset_user_db(responseString, getActivity(), editor, false, 0, true);
                else if (url.equalsIgnoreCase(uri_next))
                    Parser.reset_user_db(responseString, getActivity(), editor, false, 0, false);
                else if (url.equalsIgnoreCase(uri_get_db_search + mtxt.getText().toString())) {
                    Parser.reset_user_db(responseString, getActivity(), editor, false, 1, false);
                    search_done = true;
                    previous_query = url;
                } else if (url.equalsIgnoreCase(delete_url)) {

                    Log.e("Server response__", responseString);
                    Parser.update_user_db(responseString, getActivity(), dlt_user.getId());

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public HttpPut preparePutData(String url, HttpPut httpPost) {

        if (url.equalsIgnoreCase(delete_url)) {
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
        if (url.equalsIgnoreCase(delete_url)) {
            User user = dlt_user;
            if (user.getIsActv().equalsIgnoreCase("true"))
                user.setIsActv("false");
            else
                user.setIsActv("true");

            httpPost = Parser.setUserDeleteData(false, httpPost, user);
        }
        return httpPost;
    }


    private void notifyData(Boolean update) {
        if (getActivity() != null) {
            DatabaseHandler dbh = new DatabaseHandler(getActivity());
            if (!update) {
                users = dbh.getAllUsers();
                adapter = new UserListAdapter(getActivity(), users, this);
                list.setAdapter(adapter);
            } else {
                ArrayList<User> temp = dbh.getAllUsers();
                int i = users.size() - 1;
                users.remove(i);
                for (; i < temp.size(); i++) {
                    users.add(i, temp.get(i));
                }
                adapter.notifyDataSetChanged();
            }

            if (users.size() == 0) {
                no_result.setVisibility(View.VISIBLE);
            } else {
                no_result.setVisibility(View.GONE);
            }
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
        adapter.resetUsers(users);
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


    private void createDeleteDialog(final User user, final int pos) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String action = "";
        if (user.getIsActv().equalsIgnoreCase("true")) {
            action = "Deactivate";
        } else {
            action = "Activate";
        }

        builder.setTitle(action + " " + user.getUsername());
        builder.setMessage("Do you really want to " + action.toLowerCase() + " user ?");
        builder.setPositiveButton(action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dlt_positon = pos;
                dlt_user = user;
                delete_url = Constants.base_uri1 + user.getR_uri();
                new PatchRequest(UserList.this, Constants.base_uri1 + user.getR_uri(), getActivity());
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
