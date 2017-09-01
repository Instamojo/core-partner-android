package com.getmeashop.realestate.partner;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.getmeashop.realestate.partner.database.Cheque;
import com.getmeashop.realestate.partner.util.Constants;
import com.getmeashop.realestate.partner.util.GetRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class UserChequeList extends AppCompatActivity implements Callbacks {

    private static ArrayList<Cheque> cheques;
    RecyclerView list;
    UserChequeListAdapter adapter;
    ProgressDialog dialog;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    SwipeRefreshLayout swipeLayout;
    Boolean refreshing = false;
    boolean request_in_progress = false;
    TextView noResult;
    String cheques_next;
    FloatingActionButton fab;
    Toolbar mtoolbar;
    String id;

    public static void update(Cheque cheque, int Position) {
        if (cheques.size() == 0 || Position == -1)
            cheques.add(cheque);
        else
            cheques.set(Position, cheque);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.payment_list);

        mtoolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        list = (RecyclerView) findViewById(R.id.my_recycler_view);
        noResult = (TextView) findViewById(R.id.no_result);
        id = getIntent().getStringExtra("id");
        cheques = new ArrayList<Cheque>();
        cheques_next = Constants.uri_pay_info + "?format=json&user_id=" + id;
        fab = (FloatingActionButton) findViewById(R.id.fab);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorSchemeResources(
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                new GetRequest((Callbacks) UserChequeList.this, cheques_next, UserChequeList.this);
            }
        });
        //  list.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        list.setHasFixedSize(true);
        final LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent to_update = new Intent(UserChequeList.this, EditPayment.class);
                to_update.putExtra("user_id", id);
                startActivity(to_update);
            }
        });
        sp = getSharedPreferences(Constants.User_sp, Context.MODE_PRIVATE);
        editor = sp.edit();

//        DatabaseHandler dbh = new DatabaseHandler(getActivity());
//        cheques = dbh.getAllCheques();
//
//        adapter = new UserChequeListAdapter(getActivity(), cheques, this);
//
//        list.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Getting cheque profiles, Please wait...");
        dialog.setCancelable(false);

        new GetRequest((Callbacks) UserChequeList.this, cheques_next, getApplicationContext());


//
//        list.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                visibleItemCount = recyclerView.getChildCount();
//                totalItemCount = llm.getItemCount();
//                firstVisibleItem = llm.findFirstVisibleItemPosition();
//
//
//                if ((totalItemCount - visibleItemCount)
//                        <= firstVisibleItem) {
//                    // End has been reached
//                    cheques_next = sp.getString("cheque_next","null");
//                   if (!cheques_next.equalsIgnoreCase("")/* && search_text.getText().toString().length() == 0*/
//                            && !cheques_next.equalsIgnoreCase("null")
//                            && !request_in_progress && cheques.size() > 0 && Utils.isConnectingToInternet(getActivity())) {
//                       Log.d("end ", "grid end " + cheques_next);
//                       cheques.add(new Cheque());
//                       cheques.get(cheques.size() - 1).setChequename("load+more");
//                        Log.d("end out ", "grid end " + cheques_next + request_in_progress);
//                        adapter.notifyDataSetChanged();
//                        request_in_progress = true;
//                       uri_next = Constants.base_uri + cheques_next ;
//                        new GetRequest((Callbacks)UserChequeList.this, uri_next, getActivity());
//                    }
//                }
//            }
//        });

    }

    @Override
    public void postexecute(String url, int status) {

        if (status == 200) {
            if (url.equalsIgnoreCase(cheques_next)) {

                if (cheques.size() == 0) {
                    noResult.setVisibility(View.VISIBLE);
                } else {
                    noResult.setVisibility(View.GONE);
                    adapter = new UserChequeListAdapter(this, cheques);
                    list.setAdapter(adapter);
                }
                request_in_progress = false;
                dialog.dismiss();
            }

        } else if(status != 404){
            request_in_progress = false;
            Utils.showToast("Failed to load cheque profiles, please try again later", getApplicationContext());
            dialog.dismiss();
        } else {
            noResult.setVisibility(View.VISIBLE);
            request_in_progress = false;
            dialog.dismiss();
        }

        refreshing = false;
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void preexecute(String url) {
        if (!refreshing)
            dialog.show();
    }

    @Override
    public void processResponse(HttpResponse response, String url) {

        if (response.getStatusLine().getStatusCode() == 200 ||
                response.getStatusLine().getStatusCode() == 201) {
            try {
                InputStream inputStream = response.getEntity().getContent();
                String responseString = Utils
                        .convertInputStreamToString(inputStream);
                JSONObject jsonResponse1 = new JSONObject(responseString);
                JSONArray jarray = jsonResponse1.getJSONArray("objects");
                cheques = new ArrayList<Cheque>();
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject jsonResponse = jarray.getJSONObject(i);
                    Cheque temp = new Cheque();
                    temp.setDeposited(jsonResponse.getString("is_cheque_deposited"));
                    temp.setBank_name(jsonResponse.getString("bank_name"));
                    temp.setNumber(jsonResponse.getString("cheque_number"));
                    temp.setR_uri(jsonResponse.getString("resource_uri"));
                    temp.setId(jsonResponse.getString("id"));
                    temp.setMonths(jsonResponse.optString("months"));
                    temp.setAmount(jsonResponse.optString("amount_paid"));
                    temp.setPlan(jsonResponse.optString("plan"));

                    String datewa = jsonResponse.getString("start_date");
                    //datewa.substring(0, datewa.indexOf("T"));
                    temp.setStartDate(datewa);
                    temp.setImage("null");
                    if (!jsonResponse.getString("cheque_image").equalsIgnoreCase("") && !jsonResponse.getString("cheque_image").equalsIgnoreCase("null")) {
                        Calendar calendar = Calendar.getInstance();
                        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
                        temp.setImage(Constants.base_uri1 + jsonResponse.getString("cheque_image") + "?time=" + URLEncoder.encode(currentTimestamp.toString(), "UTF-8"));
                    }
                    cheques.add(temp);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.notifyDataSetChanged();
        else if (cheques != null && cheques.size() > 0) {
            adapter = new UserChequeListAdapter(this, cheques);
            list.setAdapter(adapter
            );
        }
    }

}
