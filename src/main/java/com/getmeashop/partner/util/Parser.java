package com.getmeashop.partner.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.getmeashop.partner.PersistentCookieStore;
import com.getmeashop.partner.database.DatabaseHandler;
import com.getmeashop.partner.database.User;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Parser {
//
//    /**
//     * extracts cat info from response and resets the cat db
//     *
//     * @param HttpResponse
//     * @param context
//     */
//
//    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
//    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
//
//    public static void reset_category_db(HttpResponse response, Context context, Editor editor) {
//        String base_uri = Constants.base_uri1;
//
//        try {
//            InputStream inputStream = response.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            JSONObject jsonResponse = new JSONObject(responseString);
//            JSONObject jsonmeta = jsonResponse.getJSONObject("meta");
//            editor.putString("total_cat", jsonmeta.getString("total_count"));
//            editor.commit();
//
//            JSONArray objects = jsonResponse.getJSONArray("objects");
//            System.out.println(objects.length() + "jsonres of get cat"
//                    + responseString);
//            DatabaseHandler dbh = new DatabaseHandler(context);
//
//            if (doesDatabaseExist(context, "productsmanager")) {
//                dbh.removeAllcat();
//            }
//
//            for (int i = 0; i < objects.length(); i++) {
//
//                JSONObject object = objects.getJSONObject(i);
//                String temp_img = "null";
//                String parent_category = "None";
//                if (!object.getString("image").equalsIgnoreCase("null")) {
//                    //  temp_img = base_uri + object.getString("image");
//                    Calendar calendar = Calendar.getInstance();
//                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
//                    temp_img = base_uri + object.getString("image") + "?time=" + currentTimestamp;
//                }
//
//                if (!object.getString("parent_category").equalsIgnoreCase(
//                        "null")) {
//                    parent_category = object.getString("parent_category");
//                }
//
//                dbh.addcategory(new Categories(object.getString("id"), object
//                        .getString("name"), temp_img, parent_category, object
//                        .getString("is_featured"), "true"));
//            }
//        } catch (JSONException e) {
//            Log.d("error", "error pasrsing json" + e.toString());
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//    }
//

    /**
     * creates get request
     *
     * @param url
     */
    public static HttpGet create_get_request(String url) {

        HttpGet request = new HttpGet(URI.create(url));
        return request;
    }


    /**
     * checks if database exists
     */
    static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    /**
     * extracts cookies from httpclient returns cookies extracted
     *
     * @param httpclient containing cookies
     * @param context
     */
    public static PersistentCookieStore store_cookies(HttpClient httpclient,
                                                      Context context) {
        List<Cookie> cookies = ((AbstractHttpClient) httpclient)
                .getCookieStore().getCookies();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        for (Cookie c : cookies) {
            if (c.getName().equalsIgnoreCase("csrftoken")) {
                cookieStore.clear();
                cookieStore.addCookie(c);
            }
        }
        return cookieStore;
    }


    /**
     * //     * extracts product info from response and resets the product db, also
     * //     * stores page info
     * //     *
     * //     * @param HttpResponse
     * //     * @param context
     * //
     */
    public static void reset_user_db(String responseString, Context context,
                                     Editor editor, Boolean nonArchived, int meta, Boolean removeDb) {
        try {

            System.out.println("jsonres of get product in splash"
                    + responseString);
            JSONObject jsonResponse = new JSONObject(responseString);

            DatabaseHandler dbh = new DatabaseHandler(context);

            if (removeDb)
                dbh.removeAllusers();

            JSONArray objects = jsonResponse.getJSONArray("objects");

            for (int i = 0; i < objects.length(); i++) {
                JSONObject object = objects.getJSONObject(i);
                User user1 = new User(object.getString("first_name"), object.getString("last_name"), object.getString("id"),
                        object.getString("username"), object.getString("email"), "", object.getString("resource_uri"),
                        object.getString("contact_number"), object.getString("address"), object.optString("is_active"), object.optString("created"),
                        object.optString("modified"), object.optString("city"), object.optString("storeinfo_id"));
                if (nonArchived) {
                    user1.setIsArchv(false);
                    dbh.ArchiveUser(user1.getPid());
                }
                dbh.updateUser(user1);
            }

            if (meta == 0) {
                JSONObject jsonmeta = jsonResponse.getJSONObject("meta");
                if (!jsonmeta.getString("next").equalsIgnoreCase("null")) {
                    editor.putBoolean("users_more", true);
                } else
                    editor.putBoolean("users_more", false);

            }
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("error", "error pasrsing json" + e.toString());
        }

    }


    /**
     * //     * extracts product info from response and resets the product db, also
     * //     * stores page info
     * //     *
     * //     * @param HttpResponse
     * //     * @param context
     * //
     */
    public static void add_to_user_db(String responseString, Context context,
                                      Editor editor, Boolean nonArchived, int meta) {
        try {

            System.out.println("jsonres of get product in splash"
                    + responseString);
            JSONObject object = new JSONObject(responseString);

            DatabaseHandler dbh = new DatabaseHandler(context);


            User user1 = new User(object.getString("first_name"), object.getString("last_name"), object.getString("id"),
                    object.getString("username"), object.getString("email"), "", object.getString("resource_uri"),
                    object.getString("contact_number"), object.getString("address"), object.getString("is_active"), object.optString("created"),
                    object.optString("modified"), object.optString("city"), object.optString("storeinfo_id"));
            if (nonArchived)
                user1.setIsArchv(false);

            dbh.updateUser(user1);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("error", "error pasrsing json" + e.toString());
        }

    }


    /**
     * //     * extracts product info from response and resets the product db, also
     * //     * stores page info
     * //     *
     * //     * @param HttpResponse
     * //     * @param context
     * //
     */
    public static void update_user_db(String responseString, Context context,
                                      String id) {
        try {

            System.out.println("jsonres of get product in splash"
                    + responseString);
            JSONObject user = new JSONObject(responseString);
            DatabaseHandler dbh = new DatabaseHandler(context);

            User userUpdated = new User(user.getString("first_name"), user.getString("last_name"), user.getString("id"),
                    user.getString("username"), user.getString("email"), "", user.getString("resource_uri"),
                    user.getString("contact_number"), user.getString("address"), user.optString("is_active"), user.optString("created"),
                    user.optString("modified"), user.optString("city"), user.optString("storeinfo_id"));
            userUpdated.setId(id);
            dbh.updateUser(userUpdated);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("error", "error pasrsing json" + e.toString());
        }

    }


    public static HttpPost setUserData(boolean pass, HttpPost httpPost, User user) {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        // JSONObject jsonObject = new JSONObject();
        if (user != null) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", user.getUsername());
                jsonObject.put("first_name", user.getFname());
                jsonObject.put("last_name", user.getLname());
                jsonObject.put("email", user.getEmail());
                jsonObject.put("id", Integer.parseInt(user.getPid()));
                if (pass && !user.getPassword().equalsIgnoreCase("") && !user.getPassword().equalsIgnoreCase("null"))
                    jsonObject.put("password", user.getPassword());


                //jsonObject.put("address", user.getAddress());
                jsonObject.put("contact_number", user.getContact());
                jsonObject.put("city", user.getCity());
                jsonObject.put("is_active", !user.getIsActv().equalsIgnoreCase("false"));

                StringEntity en = new StringEntity(jsonObject.toString());
                en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(en);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        return httpPost;
    }

    public static HttpPost setUserDeleteData(boolean pass, HttpPost httpPost, User user) {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        // JSONObject jsonObject = new JSONObject();
        if (user != null) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", user.getUsername());
                jsonObject.put("email", user.getEmail());
                jsonObject.put("id", Integer.parseInt(user.getPid()));
                jsonObject.put("is_active", Boolean.parseBoolean(user.getIsActv()));

                StringEntity en = new StringEntity(jsonObject.toString());
                en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(en);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        return httpPost;
    }

    public static HttpPut setUserData(boolean pass, HttpPut httpPost, User user) {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        // JSONObject jsonObject = new JSONObject();
        if (user != null) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", user.getUsername());
                jsonObject.put("first_name", user.getFname());
                jsonObject.put("last_name", user.getLname());
                jsonObject.put("email", user.getEmail());
                jsonObject.put("id", Integer.parseInt(user.getPid()));
                if (pass && !user.getPassword().equalsIgnoreCase("") && !user.getPassword().equalsIgnoreCase("null"))
                    jsonObject.put("password", user.getPassword());


                //jsonObject.put("address", user.getAddress());
                jsonObject.put("contact_number", user.getContact());
                jsonObject.put("city", user.getCity());
                jsonObject.put("is_active", !user.getIsActv().equalsIgnoreCase("false"));

                StringEntity en = new StringEntity(jsonObject.toString());
                en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(en);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        return httpPost;
    }


    public static HttpPost setUserData(Context context, HttpPost httpPost, User user) {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        // JSONObject jsonObject = new JSONObject();
        if (user != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", user.getUsername());
                jsonObject.put("first_name", user.getFname());
                jsonObject.put("last_name", user.getLname());
                jsonObject.put("email", user.getEmail());
                if (!user.getPassword().equalsIgnoreCase("") && !user.getPassword().equalsIgnoreCase("null"))
                    jsonObject.put("password", user.getPassword());


                jsonObject.put("address", user.getAddress());
                jsonObject.put("contact_number", user.getContact());
                jsonObject.put("city", user.getCity());
                jsonObject.put("is_active", !user.getIsActv().equalsIgnoreCase("false"));

                Log.d("sent to server", jsonObject.toString());
                StringEntity en = new StringEntity(jsonObject.toString());
                en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(en);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

        }

        return httpPost;
    }


//    /**
//     * extracts product info from response and resets the product db, also
//     * stores page info
//     *
//     * @param HttpResponse
//     * @param context
//     */
//    public static void reset_product_db(String responseString, Context context,
//                                        Editor editor, Boolean removedb, String url) {
//        try {
//            String base_uri = Constants.base_uri1;
//            System.out.println("jsonres of get product in splash"
//                    + responseString);
//            JSONObject jsonResponse = new JSONObject(responseString);
//
//
//            if (!url.contains("title__icontains")) {
//                JSONObject jsonmeta = jsonResponse.getJSONObject("meta");
//                editor.putString("prod_next", jsonmeta.getString("next"));
//                editor.putString("total_prod", jsonmeta.getString("total_count"));
//                editor.commit();
//            }
//
//            JSONArray objects = jsonResponse.getJSONArray("objects");
//
//            DatabaseHandler dbh = new DatabaseHandler(context);
//            if (doesDatabaseExist(context, "productsmanager") && removedb) {
//                dbh.removeAll();
//            }
//            for (int i = 0; i < objects.length(); i++) {
//                JSONObject object = objects.getJSONObject(i);
//                JSONArray jarray = object.getJSONArray("categories");
//                String category = null;
//                if (jarray.length() != 0) {
//                    category = jarray.get(0).toString();
//                } else {
//                    category = null;
//                }
//                String image, image2, image3, image4, image5;
//                image = image2 = image3 = image4 = image5 = "null";
//                if (!object.getString("image").equalsIgnoreCase("null")) {
//                    //image = base_uri + object.getString("image");
//                    Calendar calendar = Calendar.getInstance();
//                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
//                    image = base_uri + object.getString("image") + "?time=" + currentTimestamp;
//                }
//                if (!object.getString("image2").equalsIgnoreCase("null")) {
//                    //image2 = base_uri + object.getString("image2");
//                    Calendar calendar = Calendar.getInstance();
//                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
//                    image2 = base_uri + object.getString("image2") + "?time=" + currentTimestamp;
//                }
//                if (!object.getString("image3").equalsIgnoreCase("null")) {
//                    //image3 = base_uri + object.getString("image3");
//                    Calendar calendar = Calendar.getInstance();
//                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
//                    image3 = base_uri + object.getString("image3") + "?time=" + currentTimestamp;
//                }
//                if (!object.getString("image4").equalsIgnoreCase("null")) {
//                    //image4 = base_uri + object.getString("image4");
//                    Calendar calendar = Calendar.getInstance();
//                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
//                    image4 = base_uri + object.getString("image4") + "?time=" + currentTimestamp;
//                }
//                if (!object.getString("image5").equalsIgnoreCase("null")) {
//                    // image5 = base_uri + object.getString("image5");
//                    Calendar calendar = Calendar.getInstance();
//                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
//                    image5 = base_uri + object.getString("image5") + "?time=" + currentTimestamp;
//                }
//
//                dbh.addallproduct(new Products(object.getString("id"), object
//                        .getString("title"), object.getString("stock"), object
//                        .getString("price"), object
//                        .getString("short_description"), image, image2, image3,
//                        image4, image5, category,
//                        object.getString("is_active"), object
//                        .getString("is_featured"), "true"));
//            }
//        } catch (JSONException e) {
//            Log.d("error", "error pasrsing json" + e.toString());
//        }
//
//    }
//
//    /**
//     * checks if database exists
//     */
//    static boolean doesDatabaseExist(Context context, String dbName) {
//        File dbFile = context.getDatabasePath(dbName);
//        return dbFile.exists();
//    }
//
//    /**
//     * extracts notif info from response and notif the cat db
//     *
//     * @param HttpResponse
//     * @param context
//     */
//    public static void reset_notification_db(HttpResponse response,
//                                             Context context, Editor editor, boolean remove_db) {
//
//        try {
//            InputStream inputStream = response.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            if (response.getStatusLine().getStatusCode() == 200) {
//                Log.d("notif data = ", "notif_data" + responseString);
//
//                JSONObject jsonResponse = new JSONObject(responseString);
//                JSONObject meta = jsonResponse.getJSONObject("meta");
//                String next_notif = meta.getString("next");
//                editor.remove("next_notif");
//                editor.putString("next_notif", next_notif);
//                editor.commit();
//
//                if (meta.getInt("total_count") > 0) {
//                    JSONArray objects = jsonResponse.getJSONArray("objects");
//                    DatabaseHandler dbh = new DatabaseHandler(context);
//                    if (doesDatabaseExist(context, "productsmanager")
//                            && remove_db) {
//                        dbh.removeAllnotif();
//                    }
//
//                    objects = jsonResponse.getJSONArray("objects");
//                    for (int i = 0; i < objects.length(); i++) {
//                        JSONObject object = objects.getJSONObject(i);
//                        String notif_id = object.getString("id");
//                        String notif_type = object.getString("type");
//                        String notif_content = object.getString("content");
//                        String notif_read = object.getString("read");
//                        String notif_timestamp = object.getString("timestamp");
//                        String notif_email = object.getString("email");
//                        Notifications notification = new Notifications(
//                                notif_id, notif_email, notif_read, notif_type,
//                                notif_content, notif_timestamp);
//                        dbh.addallNotifications(notification);
//
//                    }
//                }
//            }
//        } catch (JSONException e) {
//
//            e.printStackTrace();
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * extracts order info from response and resets the order db
//     *
//     * @param HttpResponse
//     * @param context
//     */
//    public static void reset_order_db(HttpResponse response, Context context,
//                                      Editor editor, Boolean remove_db) {
//
//        try {
//            InputStream inputStream = response.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            Log.d("order data = ", "order_data" + responseString);
//            JSONObject jsonResponse = new JSONObject(responseString);
//            JSONObject jsonmeta = jsonResponse.getJSONObject("meta");
//            String order_next = jsonmeta.getString("next");
//            editor.remove("next_order");
//            editor.putString("next_order", order_next);
//            editor.putString("total_order", jsonmeta.getString("total_count"));
//            editor.commit();
//
//            if (response.getStatusLine().getStatusCode() == 200) {
//                if (jsonmeta.getInt("total_count") > 0) {
//                    JSONArray objects = jsonResponse.getJSONArray("objects");
//                    DatabaseHandler dbh = new DatabaseHandler(context);
//                    if (doesDatabaseExist(context, "productsmanager")
//                            && remove_db) {
//                        dbh.removeAllorder();
//                    }
//                    for (int i = 0; i < objects.length(); i++) {
//                        JSONObject object = objects.getJSONObject(i);
//                        String id = object.optString("id");
//                        String order_id = object.optString("orderid");
//                        String created = object.optString("created");
//                        String firstname = object.optString("firstname");
//                        String lastname = object.optString("lastname");
//                        String email = object.optString("email");
//                        String address = object.optString("address");
//                        String city = object.optString("city");
//                        String state = object.optString("state");
//                        String code = object.optString("code");
//                        String country = object.optString("county");
//                        String contact = object.optString("contact");
//                        String total_order = object.optString("total_order");
//                        String pay_method = object.optString("payment_method");
//                        String pay_status = object.optString("payment_status");
//                        String order_status = object.optString("order_status");
//                        String order_json = object.optString("order_json");
//                        String archived = object.optString("is_archived");
//                        String temp = order_json.substring(order_json.indexOf("'items':") + 8, order_json.lastIndexOf(", 'discount':"));
//                        String orderProduct = temp.replace("u'", "'");
//
//                        Orders order = new Orders(order_id, created, firstname,
//                                lastname, email, address, city, state, code,
//                                country, contact, total_order, pay_method, orderProduct,
//                                pay_status, order_status, order_json, id, archived);
//                        dbh.addallorders(order);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * extracts sync message from JSON returns sync msg string
//     *
//     * @param HttpResponse
//     */
//    public static String get_query_message(HttpResponse response) {
//        Log.d("The sync msg: ", "sync ");
//        String query_status = "";
//        try {
//            InputStream inputStream = response.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            Log.d("The sync msg: ", "sync " + responseString.toString());
//            try {
//                JSONObject jobject = new JSONObject(responseString);
//                query_status = jobject.getString("message");
//            } catch (JSONException e) {
//                // Default case, just syncing it.
//                query_status = "required";
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return query_status;
//
//    }
//
//    /**
//     * binds product data to httppost returns httppost request with post data
//     *
//     * @param HttpPost
//     * @param username
//     * @param context
//     */
//    public static HttpPost set_product_post_data(Context context,
//                                                 String username, HttpPost httpPost) {
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        DatabaseHandler db = new DatabaseHandler(context);
//        ArrayList<Products> prod = (ArrayList<Products>) db
//                .getNotSyncedProducts();
//
//        try {
//            JSONObject jsonObject = new JSONObject();
//            if (prod != null && prod.size() > 0) {
//                JSONArray jsonarray = new JSONArray();
//                for (Products cn : prod) {
//                    Boolean image_exists = true;
//                    JSONObject jobject = new JSONObject();
//                    jobject.put("id", cn.getProductId());
//                    jobject.put("title", cn.getName());
//                    jobject.put("price", cn.getPrice());
//                    jobject.put("stock", cn.getStock());
//                    jobject.put("short_description", cn.getShortDescription());
//
//                    if (cn.getImage().equalsIgnoreCase("null")) {
//                        jobject.put("image", false);
//                    } else if (!cn.getImage().contains(Constants.base_uri)) {
//                        Log.d("tag", "images_val" + cn.getImage());
//                        File imFile = new File(cn.getImage());
//                        if (!imFile.exists()) {
//                            image_exists = false;
//                        } else {
//                            jobject.put("image", makeSlug(imFile.getName()));
//                            FileBody imBody = new FileBody(imFile);
//                            builder.addPart(cn.getName() + "_image", imBody);
//                        }
//
//                    }
//
//                    if (cn.getImage2().equalsIgnoreCase("null")) {
//                        jobject.put("image2", false);
//                    } else if (!cn.getImage2().contains(Constants.base_uri)) {
//                        Log.d("tag", "images_val" + cn.getImage2());
//                        File imFile = new File(cn.getImage2());
//                        if (!imFile.exists()) {
//                            image_exists = false;
//                        } else {
//                            jobject.put("image2", makeSlug(imFile.getName()));
//                            FileBody imBody = new FileBody(imFile);
//                            builder.addPart(cn.getName() + "_image2", imBody);
//                        }
//                    }
//
//                    if (cn.getImage3().equalsIgnoreCase("null")) {
//                        jobject.put("image3", false);
//                    } else if (!cn.getImage3().contains(Constants.base_uri)) {
//                        Log.d("tag", "images_val" + cn.getImage3());
//                        File imFile = new File(cn.getImage3());
//                        if (!imFile.exists()) {
//                            image_exists = false;
//                        } else {
//                            jobject.put("image3", makeSlug(imFile.getName()));
//                            FileBody imBody = new FileBody(imFile);
//                            builder.addPart(cn.getName() + "_image3", imBody);
//                        }
//                    }
//
//                    if (cn.getImage4().equalsIgnoreCase("null")) {
//                        jobject.put("image4", false);
//                    } else if (!cn.getImage4().contains(Constants.base_uri)) {
//                        Log.d("tag", "images_val" + cn.getImage4());
//                        File imFile = new File(cn.getImage4());
//                        if (!imFile.exists()) {
//                            image_exists = false;
//                        } else {
//                            jobject.put("image4", makeSlug(imFile.getName()));
//                            FileBody imBody = new FileBody(imFile);
//                            builder.addPart(cn.getName() + "_image4", imBody);
//                        }
//                    }
//
//                    if (cn.getImage5().equalsIgnoreCase("null")) {
//                        jobject.put("image5", false);
//                    } else if (!cn.getImage5().contains(Constants.base_uri)) {
//                        Log.d("tag", "images_val" + cn.getImage5());
//                        File imFile = new File(cn.getImage5());
//                        if (!imFile.exists()) {
//                            image_exists = false;
//                        } else {
//                            jobject.put("image5", makeSlug(imFile.getName()));
//                            FileBody imBody = new FileBody(imFile);
//                            builder.addPart(cn.getName() + "_image5", imBody);
//                        }
//                    }
//                    jobject.put("is_active", cn.getisactive());
//                    jobject.put("is_featured", cn.getisfeatured());
//                    jobject.put("categories", cn.getCatName());
//                    if (image_exists)
//                        jsonarray.put(jobject);
//                }
//                jsonObject.put("products", jsonarray);
//
//            }
//            Log.d("tag", "data sent=" + jsonObject.toString());
//            builder.addPart("prod_json", new StringBody(jsonObject.toString(),
//                    ContentType.TEXT_PLAIN));
//
//            builder.addPart("username", new StringBody(username,
//                    ContentType.TEXT_PLAIN));
//            httpPost.setEntity(builder.build());
//            return httpPost;
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }
//
//    /**
//     * binds cat data to httppost returns httppost request with post data
//     *
//     * @param HttpPost
//     * @param username
//     * @param context
//     */
//    public static HttpPost set_cat_post_data(Context context, String username,
//                                             HttpPost httpPost) {
//        try {
//            DatabaseHandler db = new DatabaseHandler(context);
//            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//            ArrayList<Categories> prod = (ArrayList<Categories>) db
//                    .getNotSyncedCategory();
//
//            JSONObject jsonObject = new JSONObject();
//            if (prod != null && prod.size() > 0) {
//                JSONArray jsonarray = new JSONArray();
//                for (Categories cn : prod) {
//                    Boolean image_exists = true;
//                    JSONObject jobject = new JSONObject();
//                    jobject.put("id", cn.getCid());
//                    jobject.put("name", cn.getname());
//                    if (cn.getImage().equalsIgnoreCase("null")) {
//                        jobject.put("image", false);
//                    } else if (!cn.getImage().contains(Constants.base_uri)) {
//                        File imFile = new File(cn.getImage());
//                        if (!imFile.exists()) {
//                            image_exists = false;
//                        }
//                        jobject.put("image", makeSlug(imFile.getName()));
//                        FileBody imBody = new FileBody(imFile);
//                        builder.addPart(cn.getname(), imBody);
//                    }
//                    jobject.put("parent_category", cn.getparentname());
//                    jobject.put("is_featured", cn.getFeatured());
//                    if (image_exists)
//                        jsonarray.put(jobject);
//                }
//                jsonObject.put("categories", jsonarray);
//            }
//
//            Log.d("from app", username + "json check" + jsonObject);
//
//            builder.addPart("prod_json", new StringBody(jsonObject.toString(),
//                    ContentType.TEXT_PLAIN));
//            builder.addPart("username", new StringBody(username,
//                    ContentType.TEXT_PLAIN));
//            httpPost.setEntity(builder.build());
//            return httpPost;
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * called when updating/adding product extracts product info from response
//     * and updates the product db
//     *
//     * @param HttpResponse
//     * @param context
//     */
//    public static void update_product_data(HttpResponse httpResponse,
//                                           final Context context, final Activity activity) {
//        SharedPreferences sp = context.getSharedPreferences("Users", Context.MODE_PRIVATE);
//        Editor editor = sp.edit();
//        try {
//            String base_uri = Constants.base_uri1;
//            InputStream inputStream = httpResponse.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            String total_products = sp.getString("total_prod", "0");
//            Log.d("tag", "data sent from=" + responseString);
//            JSONObject jobject = new JSONObject(responseString);
//            if (jobject.getString("status").equals("success")) {
//                JSONArray objects = jobject.getJSONArray("products");
//
//                DatabaseHandler dbh = new DatabaseHandler(context);
//                for (int i = 0; i < objects.length(); i++) {
//                    JSONObject object = objects.getJSONObject(i);
//                    String prod_synced = "true";
//                    if (object.getString("status").equals("error")) {
//                        JSONObject notif = new JSONObject();
//                        notif.put("object", "product");
//                        notif.put(
//                                "title",
//                                "Error syncing product: "
//                                        + object.getString("title"));
//                        notif.put("message", object.getString("message"));
//                        notif.put("notif_id", 1);
//                        notif.put("type", "error");
//                        prod_synced = "false";
//                        showNotification(notif, context);
//                    } else if (object.getString("images.status")
//                            .equals("error")) {
//                        JSONObject notif = new JSONObject();
//                        notif.put("object", "product");
//                        notif.put(
//                                "title",
//                                "Error syncing product: "
//                                        + object.getString("title"));
//                        notif.put("message", "Error uploading image");
//                        notif.put("notif_id", 1);
//                        notif.put("type", "error");
//                        prod_synced = "false";
//                        showNotification(notif, context);
//                    } else {
//
//                        String image, image2, image3, image4, image5;
//                        image = image2 = image3 = image4 = image5 = "null";
//
//                        try {
//                            Products prod1 = dbh.getProduct(object
//                                    .getString("id"));
//                            if (prod1.getName() == null)
//                                total_products = "" + (Integer.parseInt(total_products) + 1);
//
//                            image = prod1.getImage();
//                            image2 = prod1.getImage2();
//                            image3 = prod1.getImage3();
//                            image4 = prod1.getImage4();
//                            image5 = prod1.getImage5();
//                        } catch (Exception e) {
//                        }
//
//                        image = check_if_image(object, "image", base_uri, image);
//                        image2 = check_if_image(object, "image2", base_uri, image2);
//                        image3 = check_if_image(object, "image3", base_uri, image3);
//                        image4 = check_if_image(object, "image4", base_uri, image4);
//                        image5 = check_if_image(object, "image5", base_uri, image5);
//
//                        image = check_null(image);
//                        image2 = check_null(image2);
//                        image3 = check_null(image3);
//                        image4 = check_null(image4);
//                        image5 = check_null(image5);
//
//                        Log.d("", "images" + image + "  && " + image2 + "  && "
//                                + image3 + "  &&  " + image4 + "  && " + image5);
//                        dbh.updateSingleProduct(new Products(object
//                                .getString("id"), object.getString("title"),
//                                object.getString("stock"), object
//                                .getString("price"), object
//                                .getString("short_description"), image,
//                                image2, image3, image4, image5, object
//                                .optString("categories", ""), object
//                                .getString("is_active"), object
//                                .getString("is_featured"), prod_synced));
//                    }
//                }
//                editor.remove("total_prod");
//                editor.putString("total_prod", total_products);
//                editor.commit();
//            } else {
//                final String message = jobject.getString("message");
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(context,
//                                "Unable to sync products: " + message,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * called when updating/adding category extracts cat info from response and
//     * updates the cat db
//     *
//     * @param HttpResponse
//     * @param context
//     */
//    public static void update_cat_data(HttpResponse httpResponse,
//                                       final Context context, final Activity activity) {
//        SharedPreferences sp = context.getSharedPreferences("Users", Context.MODE_PRIVATE);
//        Editor editor = sp.edit();
//
//        try {
//            String base_uri = Constants.base_uri1;
//            InputStream inputStream = httpResponse.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            Log.d("tag", "json check" + responseString);
//            JSONObject jobject = new JSONObject(responseString);
//            String total_cat = sp.getString("total_cat", "0");
//            if (jobject.getString("status").equals("success")) {
//                JSONArray objects = jobject.getJSONArray("products");
//
//                DatabaseHandler dbh = new DatabaseHandler(context);
//                for (int i = 0; i < objects.length(); i++) {
//                    JSONObject object = objects.getJSONObject(i);
//                    String temp_img = "null";
//                    String parent_category = "None";
//                    String cat_synced = "true";
//                    if (object.getString("status").equals("error")) {
//                        JSONObject notif = new JSONObject();
//                        notif.put("object", "category");
//                        notif.put(
//                                "title",
//                                "Error syncing category: "
//                                        + object.getString("title"));
//                        notif.put("message", object.getString("message"));
//                        notif.put("notif_id", 2);
//                        notif.put("type", "error");
//                        cat_synced = "false";
//                        showNotification(notif, context);
//                    } else {
//                        String name = dbh.getCatNameFromId(object.getString("id"));
//                        if (name == null) {
//                            total_cat = "" + (Integer.parseInt(total_cat) + 1);
//                        }
//                        temp_img = check_if_image(object, "image", base_uri, temp_img);
//
//                        if (object.getString("parent_category") != null) {
//                            parent_category = object
//                                    .getString("parent_category");
//                        }
//
//                        dbh.updateCategory(
//                                new Categories(object.getString("id"), object
//                                        .getString("name"), temp_img,
//                                        parent_category, object
//                                        .getString("is_featured"),
//                                        cat_synced), object.getString("name"));
//                    }
//                }
//                editor.remove("total_cat");
//                editor.putString("total_cat", total_cat);
//                editor.commit();
//            } else {
//                final String message = jobject.getString("message");
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(context,
//                                "Unable to sync category: " + message,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * builds notification, called in case of failure update product/cat
//     *
//     * @param JSON    Object containing message
//     * @param context
//     */
//    private static void showNotification(JSONObject content, Context context) {
//
//        NotificationManager mNotificationManager = (NotificationManager) context
//                .getSystemService(context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
//                context);
//        Integer notification_id = 0;
//        String notif_type = null;
//        Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
//                R.drawable.iic_launcher);
//        try {
//            notif_type = content.getString("type");
//            Integer icon = 0;
//            if (notif_type.equals("error")) {
//                icon = R.drawable.iic_launcher;
//            } else {
//                icon = R.drawable.iic_launcher;
//            }
//            mNotifyBuilder.setContentTitle(content.getString("title"))
//                    .setContentText(content.getString("message"))
//                    .setAutoCancel(true).setSmallIcon(icon).setLargeIcon(bm)
//                    .setTicker(content.getString("message"));
//            notification_id = content.getInt("notif_id");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        mNotificationManager.notify(notification_id, mNotifyBuilder.build());
//    }
//
//    private static String check_if_image(JSONObject object, String image,
//                                         String base_uri, String image_previous) {
//        try {
//            if (!object.getString(image).equalsIgnoreCase("null")) {
//                Calendar calendar = Calendar.getInstance();
//                java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
//                String temp_img = base_uri + object.getString(image) + "?time=" + currentTimestamp;
//                return temp_img;
//            }
//        } catch (JSONException e) {
//        }
//        if (image_previous == null)
//            return "null";
//        return image_previous;
//    }
//
//    private static String check_null(String image) {
//        if (image == null)
//            return null;
//        return image;
//    }
//
//    public static void reset_customer_db(HttpResponse response,
//                                         Context context, Editor editor, Boolean remove_db) {
//
//        try {
//            InputStream inputStream = response.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//
//            JSONObject jsonResponse = new JSONObject(responseString);
//
//            JSONObject meta = jsonResponse.getJSONObject("meta");
//            String next_notif = meta.getString("next");
//            editor.remove("next_cust");
//            editor.putString("next_cust", next_notif);
//            editor.commit();
//
//            JSONArray objects = jsonResponse.getJSONArray("objects");
//            System.out.println(objects.length() + "jsonres of get customers"
//                    + responseString);
//            DatabaseHandler dbh = new DatabaseHandler(context);
//
//            if (doesDatabaseExist(context, "productsmanager") && remove_db) {
//                dbh.removeAllCustomers();
//            }
//
//            for (int i = 0; i < objects.length(); i++) {
//
//                JSONObject object = objects.getJSONObject(i);
//
//                String name, email, sex, bday, anniv, contact, add, city, state, pin, country, id, sid, created, archived, l_name, r_uri = "";
//                name = email = sex = bday = anniv = contact = add = city = state = pin = country = id = created = l_name = archived = sid = "";
//
//                if (!object.getString("firstname").equalsIgnoreCase("null"))
//                    name = object.getString("firstname");
//                if (!object.getString("email").equalsIgnoreCase("null"))
//                    email = object.getString("email");
//                if (!object.getString("sex").equalsIgnoreCase("null"))
//                    sex = object.getString("sex");
//                if (!object.getString("birthday").equalsIgnoreCase("null"))
//                    bday = object.getString("birthday");
//                if (!object.getString("anniversary").equalsIgnoreCase("null"))
//                    anniv = object.getString("anniversary");
//                if (!object.getString("contact").equalsIgnoreCase("null"))
//                    contact = object.getString("contact");
//                if (!object.getString("address").equalsIgnoreCase("null"))
//                    add = object.getString("address");
//                if (!object.getString("city").equalsIgnoreCase("null"))
//                    city = object.getString("city");
//                if (!object.getString("state").equalsIgnoreCase("null"))
//                    state = object.getString("state");
//                if (!object.getString("pin").equalsIgnoreCase("null"))
//                    pin = object.getString("pin");
//                if (!object.getString("country").equalsIgnoreCase("null"))
//                    country = object.getString("country");
//                if (!object.getString("user_id").equalsIgnoreCase("null"))
//                    sid = object.getString("user_id");
//                if (!object.getString("id").equalsIgnoreCase("null"))
//                    id = object.getString("id");
//                if (!object.getString("resource_uri").equalsIgnoreCase("null"))
//                    r_uri = object.getString("resource_uri");
//                if (!object.getString("lastname").equalsIgnoreCase("null"))
//                    l_name = object.getString("lastname");
//                if (!object.getString("created").equalsIgnoreCase("null"))
//                    created = object.getString("created");
//                //if (!object.getString("is_archived").equalsIgnoreCase("null"))
//                //  archived = object.getString("is_archived");
//
//                dbh.addCustomer(new Customers(name, email, sex, bday, anniv,
//                        contact, add, city, state, pin, country, r_uri, id,
//                        sid, created, l_name, archived, "true"));
//
//            }
//        } catch (JSONException e) {
//            Log.d("error", "error pasrsing cust json" + e.toString());
//        } catch (IOException e) {
//            Log.d("error", "error pasrsing sust json" + e.toString());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * binds customer data to httppost returns httppost request with post data
//     *
//     * @param HttpPost
//     * @param username
//     * @param context
//     */
//    public static HttpPost set_cust_add_post_data(Context context,
//                                                  String username, HttpPost httpPost, Customers customer) {
//
//        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//        Customers cn = customer;
//
//        // JSONObject jsonObject = new JSONObject();
//        if (cn != null) {
//
//            if (!cn.getEmail().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("email", cn
//                        .getEmail()));
//            if (!cn.getName().equalsIgnoreCase(""))
//                nameValuePairs
//                        .add(new BasicNameValuePair("firstname", cn.getName()));
//            if (!cn.getSex().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("sex", cn.getSex()));
//            if (!cn.getBday().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("birthday", cn
//                        .getBday()));
//            if (!cn.getAnniversary().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("anniversary", cn
//                        .getAnniversary()));
//            if (!cn.getContact().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("contact", cn
//                        .getContact()));
//            if (!cn.getAdd().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("address", cn.getAdd()));
//            if (!cn.getCity().equalsIgnoreCase(""))
//                nameValuePairs
//                        .add(new BasicNameValuePair("city", cn.getCity()));
//            if (!cn.getState().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("state", cn
//                        .getState()));
//            if (!cn.getPin().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("pin", cn.getPin()));
//            if (!cn.getCountry().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("country", cn
//                        .getCountry()));
//            if (!cn.getPid().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("profile_id", cn
//                        .getPid()));
//            if (!cn.getLastName().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("lastname", cn
//                        .getLastName()));
//            if (!cn.getCreated().equalsIgnoreCase(""))
//                nameValuePairs.add(new BasicNameValuePair("created", cn
//                        .getCreated()));
//
//        }
//
//        try {
//            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            return null;
//        }
//
//        return httpPost;
//
//    }
//
//    /**
//     * called when updating/adding customer extracts customer info from response
//     * and updates the product db
//     *
//     * @param HttpResponse
//     * @param context
//     */
//    public static void update_customer_data(HttpResponse httpResponse,
//                                            final Context context, final Activity activity, Customers customer) {
//
//        try {
//            InputStream inputStream = httpResponse.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            Log.d("tag", "data sent from cust=" + responseString);
//            JSONObject jobject = new JSONObject(responseString);
//            if (jobject.getString("status").equals("success")) {
//                String sid = "", id = "";
//                if (!jobject.getString("user_id").equalsIgnoreCase("null"))
//                    sid = jobject.getString("user_id");
//                if (!jobject.getString("id").equalsIgnoreCase("null"))
//                    id = jobject.getString("id");
//
//                JSONObject object = jobject.getJSONObject("form");
//                DatabaseHandler dbh = new DatabaseHandler(context);
//
//                String name, email, sex, bday, anniv, contact, add, city, state, pin, country, created, archived, l_name, r_uri = "";
//                name = email = sex = bday = anniv = contact = add = city = state = pin = country = created = l_name = archived = "";
//
//                if (!object.getString("firstname").equalsIgnoreCase("null"))
//                    name = object.getString("firstname");
//                if (!object.getString("email").equalsIgnoreCase("null"))
//                    email = object.getString("email");
//                if (!object.getString("sex").equalsIgnoreCase("null"))
//                    sex = object.getString("sex");
//                if (!object.getString("birthday").equalsIgnoreCase("null"))
//                    bday = object.getString("birthday");
//                if (!object.getString("anniversary").equalsIgnoreCase("null"))
//                    anniv = object.getString("anniversary");
//                if (!object.getString("contact").equalsIgnoreCase("null"))
//                    contact = object.getString("contact");
//                if (!object.getString("address").equalsIgnoreCase("null"))
//                    add = object.getString("address");
//                if (!object.getString("city").equalsIgnoreCase("null"))
//                    city = object.getString("city");
//                if (!object.getString("state").equalsIgnoreCase("null"))
//                    state = object.getString("state");
//                if (!object.getString("pin").equalsIgnoreCase("null"))
//                    pin = object.getString("pin");
//                if (!object.getString("country").equalsIgnoreCase("null"))
//                    country = object.getString("country");
//                // if (!object.getString("resource_uri").equalsIgnoreCase("null"))
//                //   r_uri = object.getString("resource_uri");
//                if (!object.getString("lastname").equalsIgnoreCase("null"))
//                    l_name = object.getString("lastname");
//                //if (!object.getString("created").equalsIgnoreCase("null"))
//                //    created = object.getString("created");
//                //if (!object.getString("is_archived").equalsIgnoreCase("null"))
//                //  archived = object.getString("is_archived");
//
//                dbh.updateSingleCustomer(new Customers(name, email, sex, bday, anniv,
//                        contact, add, city, state, pin, country, r_uri, customer.getPid(),
//                        sid, created, l_name, archived, "true"), customer.getPid());
//
//            } else {
//                // final String message = jobject.getString("message");
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(
//                                context,
//                                "Unable to sync changes,Please try again later ",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * called when updating/adding product extracts product info from response
//     * and updates the product db
//     *
//     * @param HttpResponse
//     * @param context
//     */
//    public static void add_customer_data(HttpResponse httpResponse,
//                                         final Context context, final Activity activity) {
//
//        try {
//            InputStream inputStream = httpResponse.getEntity().getContent();
//            String responseString = Utils
//                    .convertInputStreamToString(inputStream);
//            Log.d("tag", "data sent from cust=" + responseString);
//            JSONObject jobject = new JSONObject(responseString);
//            if (jobject.getString("status").equals("success")) {
//                String sid = "", id = "";
//                if (!jobject.getString("user_id").equalsIgnoreCase("null"))
//                    sid = jobject.getString("user_id");
//                if (!jobject.getString("id").equalsIgnoreCase("null"))
//                    id = jobject.getString("id");
//                JSONObject object = jobject.getJSONObject("form");
//
//                DatabaseHandler dbh = new DatabaseHandler(context);
//
//                String name, email, sex, bday, anniv, contact, add, city, state, pin, country, created, archived, l_name, r_uri = "";
//                name = email = sex = bday = anniv = contact = add = city = state = pin = country = created = l_name = archived = "";
//
//                if (!object.getString("firstname").equalsIgnoreCase("null"))
//                    name = object.getString("firstname");
//                if (!object.getString("email").equalsIgnoreCase("null"))
//                    email = object.getString("email");
//                if (!object.getString("sex").equalsIgnoreCase("null"))
//                    sex = object.getString("sex");
//                if (!object.getString("birthday").equalsIgnoreCase("null"))
//                    bday = object.getString("birthday");
//                if (!object.getString("anniversary").equalsIgnoreCase("null"))
//                    anniv = object.getString("anniversary");
//                if (!object.getString("contact").equalsIgnoreCase("null"))
//                    contact = object.getString("contact");
//                if (!object.getString("address").equalsIgnoreCase("null"))
//                    add = object.getString("address");
//                if (!object.getString("city").equalsIgnoreCase("null"))
//                    city = object.getString("city");
//                if (!object.getString("state").equalsIgnoreCase("null"))
//                    state = object.getString("state");
//                if (!object.getString("pin").equalsIgnoreCase("null"))
//                    pin = object.getString("pin");
//                if (!object.getString("country").equalsIgnoreCase("null"))
//                    country = object.getString("country");
//                //if (!object.getString("resource_uri").equalsIgnoreCase("null"))
//                //    r_uri = object.getString("resource_uri");
//                if (!object.getString("lastname").equalsIgnoreCase("null"))
//                    l_name = object.getString("lastname");
//                // if (!object.getString("created").equalsIgnoreCase("null"))
//                //    created = object.getString("created");
//                //if (!object.getString("is_archived").equalsIgnoreCase("null"))
//                //  archived = object.getString("is_archived");
//
//                dbh.addCustomer(new Customers(name, email, sex, bday, anniv,
//                        contact, add, city, state, pin, country, r_uri, id,
//                        sid, created, l_name, archived, "true"));
//
//            } else {
//                // final String message = jobject.getString("message");
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(
//                                context,
//                                "Unable to sync changes please try again later: ",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public static String makeSlug(String input) {
//        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
//        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
//        String slug = NONLATIN.matcher(normalized).replaceAll("");
//        return slug.toLowerCase(Locale.ENGLISH);
//    }

}
