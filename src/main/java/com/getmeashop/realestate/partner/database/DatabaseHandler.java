package com.getmeashop.realestate.partner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    // table name
    public static final String TABLE_USERS = "users";

    // Database Name
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "reseller";
    private static final String TABLE_ARCHIVED = "archived";
    // Products Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_STOCK = "stock";
    private static final String KEY_PRICE = "price";
    private static final String KEY_SHORT_DESCRIPTION = "short_description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_IMAGE2 = "image2";
    private static final String KEY_IMAGE3 = "image3";
    private static final String KEY_IMAGE4 = "image4";
    private static final String KEY_IMAGE5 = "image5";
    private static final String KEY_PRODUCTID = "productid";
    private static final String KEY_CATNAME = "catname";
    private static final String KEY_ISSYNCED = "sync";
    private static final String KEY_ISACTV = "active";
    private static final String KEY_ISFTRD = "featured";
    // Categories Table Columns Name
    private static final String KEYID = "_id";
    private static final String KEY_CAT_ID = "cat_id";
    private static final String KEY_CAT_IMAGE = "cat_image";
    private static final String KEY_CAT_NAME = "cat_name";
    private static final String KEY_PARENT_ID = "parent_id";
    private static final String KEY_PARENT_NAME = "parent_name";
    private static final String KEY_CAT_SYNCED = "cat_sync";
    private static final String KEY_CAT_FEATURED = "is_featured";
    // Notifiacations Table Coulumns Name
    private static final String KEY_NID = "nid";
    private static final String KEY_N_ID = "n_id";
    private static final String KEY_N_TYPE = "type";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_READ = "read";
    // order table names
    private static final String KEY_OID = "oid";
    private static final String KEY_CREATED = "created";
    private static final String KEY_FNAME = "fname";
    private static final String KEY_ADD = "address";
    private static final String KEY_CITY = "city";
    private static final String KEY_STOREINFO_ID = "storeinfoid";
    private static final String KEY_STATE = "state";
    private static final String KEY_CODE = "code";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_CONTACT = "contact";
    private static final String KEY_TOTAL_ORDER = "total_order";
    private static final String KEY_SERVER_ID = "server_unique_id";
    private static final String KEY_PAY_METHOD = "p_method";
    private static final String KEY_ORDER_PRODUCT = "o_product";
    private static final String KEY_PAY_STATUS = "p_status";
    private static final String KEY_ORDER_STATUS = "o_status";
    private static final String KEY_ORDER_INFO = "o_info";
    // customer table keys
    // private static final String KEY_ID="id";
    // private static final String KEY_ADD = "address";
    private static final String KEY_ANNIVERSARY = "anniversary";
    private static final String KEY_BDAY = "birthday";
    // private static final String KEY_CITY = "city";
    // private static final String KEY_CONTACT = "contact";
    // private static final String KEY_COUNTRY = "country";
    // private static final String KEY_EMAIL="email";
    private static final String KEY_P_ID = "profile_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PIN = "pin";
    private static final String KEY_R_URI = "resource_uri";
    private static final String KEY_SEX = "sex";
    // private static final String KEY_STATE = "state";
    private static final String KEY_S_ID = "user_id";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_ARCHIVED = "archived";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_MODIFIED = "modified";
    public static int productCounter = 0;
    // private static final String KEY_ISSYNCED = "sync";
    public static int categoryCounter = 0;
    static Context context;
    String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "(" + KEY_ID
            + " INTEGER PRIMARY KEY, " + KEY_P_ID + " TEXT, " + KEY_FNAME
            + " TEXT, " + KEY_LAST_NAME + " TEXT, " + KEY_EMAIL + " TEXT, " + KEY_USERNAME + " TEXT, "
            + KEY_CONTACT + " TEXT, " + KEY_PASSWORD + " TEXT, " + KEY_ISACTV + " TEXT, "
            + KEY_ADD + " TEXT, " + KEY_R_URI + " TEXT, " +
            KEY_ISSYNCED + " TEXT ," + KEY_ARCHIVED + " TEXT DEFAULT false, " + KEY_CREATED + " TEXT, "
            + KEY_MODIFIED + " TEXT," + KEY_CITY + " TEXT," + KEY_STOREINFO_ID + " TEXT," +
            "  UNIQUE( " + KEY_USERNAME + ") ON CONFLICT REPLACE )";

    String CREATE_ARCHIVED_TABLE = "CREATE TABLE " + TABLE_ARCHIVED + "(" + KEY_P_ID
            + " INTEGER PRIMARY KEY" + ")";

    public DatabaseHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_ARCHIVED_TABLE);
        Log.d("dbh", "creating dbh");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_CITY + " TEXT DEFAULT '' ");
        db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_STOREINFO_ID + " TEXT DEFAULT '' ");
    }

    public void deletedatabase() {
        Log.d("Database name", DATABASE_NAME);
        context.deleteDatabase(DATABASE_NAME);
    }


    public boolean errorsync() {
        boolean shouldsync = false;
        String selectQuery = "SELECT  * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(KEY_ISSYNCED)).equals("false")) {
                    shouldsync = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return shouldsync;

    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> userList = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " where " + KEY_ISSYNCED + " ='true'  order by "
                + KEY_P_ID + " DESC ";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                user.setPid(cursor.getString(cursor.getColumnIndex(KEY_P_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(KEY_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
                user.setIsArchv(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(KEY_ARCHIVED))));
                user.setCreated(cursor.getString(cursor.getColumnIndex(KEY_CREATED)));
                user.setCreated(cursor.getString(cursor.getColumnIndex(KEY_MODIFIED)));
                user.setR_uri(cursor.getString(cursor.getColumnIndex(KEY_R_URI)));
                user.setIsActv(cursor.getString(cursor.getColumnIndex(KEY_ISACTV)));
                user.setContact(cursor.getString(cursor.getColumnIndex(KEY_CONTACT)));
                user.setCity(cursor.getString(cursor.getColumnIndex(KEY_CITY)));
                user.setStoreinfo(cursor.getString(cursor.getColumnIndex(KEY_STOREINFO_ID)));

                // Adding contact to list
                if (!isArchivedUser(user.getPid())) {
                    user.setIsArchv(false);
                    userList.add(user);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return contact list
        return userList;
    }


    public ArrayList<User> getAllArchivedUsers() {
        ArrayList<User> userList = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " where " + KEY_ISSYNCED + " ='true'  order by "
                + KEY_ID + " ASC ";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getString(cursor.getColumnIndex(KEY_ID)));
                user.setPid(cursor.getString(cursor.getColumnIndex(KEY_P_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(KEY_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
                user.setIsArchv(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(KEY_ARCHIVED))));
                user.setCreated(cursor.getString(cursor.getColumnIndex(KEY_CREATED)));
                user.setCreated(cursor.getString(cursor.getColumnIndex(KEY_MODIFIED)));
                user.setR_uri(cursor.getString(cursor.getColumnIndex(KEY_R_URI)));
                user.setIsActv(cursor.getString(cursor.getColumnIndex(KEY_ISACTV)));
                user.setContact(cursor.getString(cursor.getColumnIndex(KEY_CONTACT)));
                user.setCity(cursor.getString(cursor.getColumnIndex(KEY_CITY)));
                user.setStoreinfo(cursor.getString(cursor.getColumnIndex(KEY_STOREINFO_ID)));

                // Adding contact to list
                if (isArchivedUser(user.getPid())) {
                    user.setIsArchv(true);
                    userList.add(user);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return contact list
        return userList;
    }


    public int addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FNAME, user.getFname());
        values.put(KEY_LAST_NAME, user.getLname());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_USERNAME, user.getUsername());
        values.put(KEY_CONTACT, user.getContact());
        values.put(KEY_P_ID, user.getPid());
        values.put(KEY_PASSWORD, user.getPassword());
        values.put(KEY_ISACTV, user.getIsActv());
        values.put(KEY_ADD, user.getAddress());
        values.put(KEY_R_URI, user.getR_uri());
        values.put(KEY_ISSYNCED, "true");
        values.put(KEY_CREATED, user.getCreated());
        values.put(KEY_MODIFIED, user.getModified());
        values.put(KEY_CITY, user.getCity());
        values.put(KEY_STOREINFO_ID, user.getStoreinfo());

        // check if exists
        boolean exists = false;
        String selectQuery = "select * from " + TABLE_USERS + " where " + KEY_USERNAME + "='" + user.getUsername() + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int row;
        if (cursor.moveToFirst()) {
            row = db.update(TABLE_USERS, values, KEY_ID + " ='" + user.getId() + "'", null);
        } else {
            Long temp = db.insert(TABLE_USERS, null, values);
            row = temp.intValue();
        }
        db.close(); // Closing database connection

        return row;
    }


    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FNAME, user.getFname());
        values.put(KEY_LAST_NAME, user.getLname());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_USERNAME, user.getUsername());
        values.put(KEY_CONTACT, user.getContact());
        values.put(KEY_P_ID, user.getPid());
        values.put(KEY_PASSWORD, user.getPassword());
        values.put(KEY_ISACTV, user.getIsActv());
        values.put(KEY_ADD, user.getAddress());
        values.put(KEY_R_URI, user.getR_uri());
        values.put(KEY_ISSYNCED, "true");
        values.put(KEY_CREATED, user.getCreated());
        values.put(KEY_MODIFIED, user.getModified());
        values.put(KEY_CITY, user.getCity());
        values.put(KEY_STOREINFO_ID, user.getStoreinfo());

        // check if exists
        boolean exists = false;
        String selectQuery = "select * from " + TABLE_USERS + " where " + KEY_ID + "='" + user.getId() + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int row;
        if (cursor.moveToFirst()) {
            row = db.update(TABLE_USERS, values, KEY_ID + " ='" + user.getId() + "'", null);
        } else {
            Long temp = db.insert(TABLE_USERS, null, values);
            row = temp.intValue();
        }
        db.close();
        db.close(); // Closing database connection


    }


    public void ArchiveUser(String user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // values.put(KEY_ARCHIVED, "true");
        values.put(KEY_P_ID, user);
        db.insert(TABLE_ARCHIVED, null, values);
        db.close(); // Closing database connection
    }

    public void unArchiveUser(String user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // values.put(KEY_ARCHIVED, "true");
        values.put(KEY_P_ID, user);
        db.delete(TABLE_ARCHIVED, KEY_P_ID + "='" + user + "'", null);
        db.close(); // Closing database connection
    }

    public void ArchiveUser(ArrayList<String> user, SQLiteDatabase db) {
        // SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < user.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(KEY_P_ID, user.get(i));
            db.insert(TABLE_ARCHIVED, null, values);
            Log.d("added to this version ", user.get(i) + " p_id_prev");
        }
    }

    public boolean isArchivedUser(String userid) {
        SQLiteDatabase db = this.getWritableDatabase();
        // values.put(KEY_ARCHIVED, "true");

        boolean exists = false;
        String selectQuery = "select * from " + TABLE_ARCHIVED + " where " + KEY_P_ID + "='" + userid + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int row;
        if (cursor.moveToFirst()) {
            return true;
        }
        return false;
    }

    public void deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, KEY_ID + " ='" + user.getId() + "'", null);
        db.close(); // Closing database connection


    }

    public void removeAllusers() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_USERS);
        db.close();
    }

    public User getUserById(String id) {
        User user = new User();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " where " + KEY_ID + " = '" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            user.setId(cursor.getString(cursor.getColumnIndex(KEY_ID)));
            user.setPid(cursor.getString(cursor.getColumnIndex(KEY_P_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndex(KEY_USERNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
            user.setFname(cursor.getString(cursor.getColumnIndex(KEY_FNAME)));
            user.setLname(cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME)));
            user.setR_uri(cursor.getString(cursor.getColumnIndex(KEY_R_URI)));
            user.setPassword(cursor.getString(cursor.getColumnIndex(KEY_PASSWORD)));
            user.setAddress(cursor.getString(cursor.getColumnIndex(KEY_ADD)));
            user.setContact(cursor.getString(cursor.getColumnIndex(KEY_CONTACT)));
            user.setIsActv(cursor.getString(cursor.getColumnIndex(KEY_ISACTV)));
            user.setCity(cursor.getString(cursor.getColumnIndex(KEY_CITY)));
            user.setStoreinfo(cursor.getString(cursor.getColumnIndex(KEY_STOREINFO_ID)));
            // Adding contact to list
        }
        cursor.close();
        db.close();
        // return contact list
        return user;
    }

    public String getMaxModified() {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " order by " + KEY_MODIFIED + " DESC ";
        String return_value = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            return_value = cursor.getString(cursor.getColumnIndex(KEY_MODIFIED));
            // Adding contact to list
        }
        cursor.close();
        db.close();
        // return contact list
        return return_value;
    }

    public String getMinModified() {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " order by " + KEY_MODIFIED + " ASC ";
        String return_value = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            return_value = cursor.getString(cursor.getColumnIndex(KEY_MODIFIED));
            // Adding contact to list
        }
        cursor.close();
        db.close();
        // return contact list
        return return_value;
    }

    public ArrayList<String> getAllArchived(SQLiteDatabase db) {
        // Select All Query
        ArrayList<String> users = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_USERS + " where " + KEY_ISSYNCED + " ='true' and " + KEY_ARCHIVED + " = 'true' order by "
                + KEY_ID + " ASC ";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {    // Adding contact to list
                users.add(cursor.getString(cursor.getColumnIndex(KEY_P_ID)));
                Log.d("previous version ", cursor.getString(cursor.getColumnIndex(KEY_P_ID)) + " p_id_prev");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

/*
    public void enterStatisticsJson(String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, json);
        // Inserting Row1
        db.execSQL("DELETE FROM " + TABLE_STATISTICS);
        db.insert(TABLE_STATISTICS, null, values);
        db.close();
    }

    public String getStatisticsJson() {

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STATISTICS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            selectQuery = cursor.getString(0);

        }
        cursor.close();
        db.close();
        // return contact list
        return selectQuery;
    }

    // =======================================ORDER DB

    public void addallorders(Orders order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_OID, order.getOid());
        values.put(KEY_SERVER_ID, order.getServerid());
        values.put(KEY_CREATED, order.getCreated());
        values.put(KEY_FNAME, order.getFname());
        values.put(KEY_LAST_NAME, order.getLname());
        values.put(KEY_EMAIL, order.getEmail());
        values.put(KEY_ADD, order.getAdd());
        values.put(KEY_CITY, order.getCity());
        values.put(KEY_STATE, order.getState());
        values.put(KEY_CODE, order.getCode());
        values.put(KEY_COUNTRY, order.getCountry());
        values.put(KEY_CONTACT, order.getContact());
        values.put(KEY_TOTAL_ORDER, order.getTotalorder());
        values.put(KEY_PAY_METHOD, order.getPaymethod());
        values.put(KEY_ORDER_PRODUCT, order.getOrderproduct());
        values.put(KEY_PAY_STATUS, order.getPaystatus());
        values.put(KEY_ORDER_STATUS, order.getOrderstatus());
        values.put(KEY_ORDER_INFO, order.getOrderinfo());
        values.put(KEY_ARCHIVED, order.getArchived());


        // Inserting Row1
        db.insert(TABLE_ORDER, null, values);
        db.close(); // Closing database connection
    }

    public ArrayList<Orders> getAllorders() {
        ArrayList<Orders> orderList = new ArrayList<Orders>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ORDER + " order by "
                + KEY_CREATED + " DESC ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Orders n = new Orders();
                n.setOid(cursor.getString(cursor.getColumnIndex(KEY_OID)));
                n.setServerid(cursor.getString(cursor.getColumnIndex(KEY_SERVER_ID)));
                n.setTotalorder(cursor.getString(cursor.getColumnIndex(KEY_TOTAL_ORDER)));
                n.setPaymethod(cursor.getString(cursor.getColumnIndex(KEY_PAY_METHOD)));
                n.setPaystatus(cursor.getString(cursor.getColumnIndex(KEY_PAY_STATUS)));
                n.setOrderstatus(cursor.getString(cursor.getColumnIndex(KEY_ORDER_STATUS)));
                // Adding contact to list
                orderList.add(n);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return contact list
        return orderList;
    }


    public Orders getOrder(String id) {
        Orders order = new Orders();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ORDER + " where " + KEY_SERVER_ID + "='" + id + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            order.setid(cursor.getString(cursor.getColumnIndex(KEY_ID)));
            order.setOid(cursor.getString(cursor.getColumnIndex(KEY_OID)));
            order.setServerid(cursor.getString(cursor.getColumnIndex(KEY_SERVER_ID)));
            order.setCreated(cursor.getString(cursor.getColumnIndex(KEY_CREATED)));
            order.setFname(cursor.getString(cursor.getColumnIndex(KEY_FNAME)));
            order.setLname(cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME)));
            order.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
            order.setAdd(cursor.getString(cursor.getColumnIndex(KEY_ADD)));
            order.setCity(cursor.getString(cursor.getColumnIndex(KEY_CITY)));
            order.setState(cursor.getString(cursor.getColumnIndex(KEY_STATE)));
            order.setCode(cursor.getString(cursor.getColumnIndex(KEY_CODE)));
            order.setCountry(cursor.getString(cursor.getColumnIndex(KEY_COUNTRY)));
            order.setContact(cursor.getString(cursor.getColumnIndex(KEY_CONTACT)));
            order.setOrderproduct(cursor.getString(cursor.getColumnIndex(KEY_ORDER_PRODUCT)));
            order.setPaystatus(cursor.getString(cursor.getColumnIndex(KEY_PAY_STATUS)));
            order.setOrderinfo(cursor.getString(cursor.getColumnIndex(KEY_ORDER_INFO)));
            order.setTotalorder(cursor.getString(cursor.getColumnIndex(KEY_TOTAL_ORDER)));
            order.setPaymethod(cursor.getString(cursor.getColumnIndex(KEY_PAY_METHOD)));
            order.setPaystatus(cursor.getString(cursor.getColumnIndex(KEY_PAY_STATUS)));
            order.setOrderstatus(cursor.getString(cursor.getColumnIndex(KEY_ORDER_STATUS)));
            order.setArchived(cursor.getString(cursor.getColumnIndex(KEY_ARCHIVED)));
            // order.setOrderinfo(cu);
            // Adding contact to list
        }
        cursor.close();
        db.close();
        // return contact list
        return order;
    }

    public void updateOrder(Orders order) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SERVER_ID, order.getServerid()); // Contact Name
        values.put(KEY_ARCHIVED, order.getArchived());
        values.put(KEY_ORDER_STATUS, order.getOrderstatus());

        try {
            int ret = db.update(TABLE_ORDER, values, KEY_ID + " ='" + order.getid() + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
    }

    public void removeOrder(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_ORDER + " where " + KEY_ID + "='" + id + "'");
        db.close();
    }

    public void removeAllorder() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_ORDER);
        db.close();
    }

    // ================================Notifications
    // Database============================================================
    public void addallNotifications(Notifications notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_N_ID, notification.getID());
        values.put(KEY_N_TYPE, notification.getType());
        values.put(KEY_EMAIL, notification.getEmail());
        values.put(KEY_CONTENT, notification.getContent());
        values.put(KEY_READ, notification.getRead());
        Log.d("email", notification.getEmail());
        Log.d("all added", "notifications added successfully");
        // Inserting Row1
        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close(); // Closing database connection
    }

    public ArrayList<Notifications> getAllNotifications() {
        ArrayList<Notifications> productList = new ArrayList<Notifications>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS
                + " order by " + KEY_N_ID + " DESC ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Notifications n = new Notifications();
                n.setID(cursor.getString(1));
                n.settype(cursor.getString(2));
                n.setEmail(cursor.getString(3));
                n.setContent(cursor.getString(4));
                n.setRead(cursor.getString(5));
                // Adding contact to list
                productList.add(n);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return contact list
        return productList;
    }

    public void removeAllnotif() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NOTIFICATIONS);
        db.close();
    }

    public void addproduct(Products product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, product.getName()); // Contact Name
        values.put(KEY_STOCK, product.getStock()); // Contact Phone

        // Inserting Row
        db.insert(TABLE_PRODUCTS, null, values);
        db.close(); // Closing database connection
    }

    public int updateSingleProduct(Products product) {
        SQLiteDatabase db = this.getWritableDatabase();
        int ret = 0;
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCTID, product.getProductId());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_IMAGE, product.getImage());
        values.put(KEY_IMAGE2, product.getImage2());
        values.put(KEY_IMAGE3, product.getImage3());
        values.put(KEY_IMAGE4, product.getImage4());
        values.put(KEY_IMAGE5, product.getImage5());
        values.put(KEY_ISSYNCED, product.getissynced());
        try {
            ret = db.update(TABLE_PRODUCTS, values, KEY_TITLE + " = ?",
                    new String[]{String.valueOf(product.getName())});
        } catch (Exception e) {

            e.printStackTrace();
        }

        // updating row
        db.close();
        return ret;

    }

    // Updating single contact
    public int updateProduct(Products product, String id, String price) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, product.getName()); // Contact Name
        values.put(KEY_STOCK, product.getStock());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_SHORT_DESCRIPTION, product.getShortDescription());
        values.put(KEY_IMAGE, product.getImage());
        values.put(KEY_IMAGE2, product.getImage2());
        values.put(KEY_IMAGE3, product.getImage3());
        values.put(KEY_IMAGE4, product.getImage4());
        values.put(KEY_IMAGE5, product.getImage5());
        values.put(KEY_CATNAME, product.getCatName());
        values.put(KEY_ISACTV, product.getisactive());
        values.put(KEY_ISFTRD, product.getisfeatured());
        values.put(KEY_ISSYNCED, product.getissynced());

        try {
            int ret = db.update(TABLE_PRODUCTS, values, KEY_TITLE + " ='" + id
                    + "' and " + KEY_PRICE + "='" + price + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
        return 0;
    }

    public void addallproduct(Products product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCTID, product.getProductId());
        values.put(KEY_TITLE, product.getName()); // Contact Name
        values.put(KEY_STOCK, product.getStock());
        values.put(KEY_PRICE, product.getPrice());
        values.put(KEY_SHORT_DESCRIPTION, product.getShortDescription());
        values.put(KEY_IMAGE, product.getImage());
        values.put(KEY_IMAGE2, product.getImage2());
        values.put(KEY_IMAGE3, product.getImage3());
        values.put(KEY_IMAGE4, product.getImage4());
        values.put(KEY_IMAGE5, product.getImage5());
        values.put(KEY_CATNAME, product.getCatName());
        values.put(KEY_ISACTV, product.getisactive());
        values.put(KEY_ISFTRD, product.getisfeatured());
        values.put(KEY_ISSYNCED, product.getissynced());
        // Inserting Row1
        db.insert(TABLE_PRODUCTS, null, values);
        db.close(); // Closing database connection
        // Toast.makeText(context,"hi", Toast.LENGTH_LONG).show();
        // SplashScreen.dbPrefs.edit().putInt("productCounter",
        // productCounter).apply();
    }

    public boolean checkDuplicateProduct(String check) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean answer = false;
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                // String duplicate = cursor.getString(2) + cursor.getString(4);
                String duplicate = cursor.getString(2);
                if (check.equals(duplicate)) {
                    answer = true;
                }
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return answer;
    }

    public boolean errorsync() {
        boolean shouldsync = false;
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(14).equals("false")) {
                    shouldsync = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return shouldsync;

    }

    public ArrayList<Products> getAllProducts() {
        ArrayList<Products> productList = new ArrayList<Products>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS + " order by "
                + KEY_PRODUCTID + " DESC ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Products products = new Products();
                products.setId(cursor.getString(0));
                products.setProductId(cursor.getString(1));
                products.setName(cursor.getString(2));
                products.setStock(cursor.getString(3));
                products.setPrice(cursor.getString(4));
                products.setShortDescription(cursor.getString(5));
                products.setImage(cursor.getString(6));
                products.setImage2(cursor.getString(7));
                products.setImage3(cursor.getString(8));
                products.setImage4(cursor.getString(9));
                products.setImage5(cursor.getString(10));
                products.setCatName(cursor.getString(11));
                products.setisactive(cursor.getString(12));
                products.setisfeatured(cursor.getString(13));
                products.setissynced(cursor.getString(14));
                // Adding contact to list
                productList.add(products);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return contact list
        return productList;
    }

    public Products getProductById(String id) {
        Products products = new Products();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS + " where " + KEY_ID + "='" + id + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            products.setId(cursor.getString(0));
            products.setProductId(cursor.getString(1));
            products.setName(cursor.getString(2));
            products.setStock(cursor.getString(3));
            products.setPrice(cursor.getString(4));
            products.setShortDescription(cursor.getString(5));
            products.setImage(cursor.getString(6));
            products.setImage2(cursor.getString(7));
            products.setImage3(cursor.getString(8));
            products.setImage4(cursor.getString(9));
            products.setImage5(cursor.getString(10));
            products.setCatName(cursor.getString(11));
            products.setisactive(cursor.getString(12));
            products.setisfeatured(cursor.getString(13));
            products.setissynced(cursor.getString(14));

        }
        cursor.close();
        db.close();
        // return contact list
        return products;
    }

    public Products getProducts(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS + " WHERE "
                + KEY_ID + " = " + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Products product = new Products();
        if (!cursor.moveToNext()) {
            product.setProductId(cursor.getString(2));
        }
        cursor.close();
        db.close();
        return product;

    }

    public Products getProduct(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS + " WHERE "
                + KEY_PRODUCTID + " = '" + id + "'";
        Log.d("tag", "query  " + selectQuery);
        Products products = new Products();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                products.setId(cursor.getString(0));
                products.setProductId(cursor.getString(1));
                products.setName(cursor.getString(2));
                products.setStock(cursor.getString(3));
                products.setPrice(cursor.getString(4));
                products.setShortDescription(cursor.getString(5));
                products.setImage(cursor.getString(6));
                products.setImage2(cursor.getString(7));
                products.setImage3(cursor.getString(8));
                products.setImage4(cursor.getString(9));
                products.setImage5(cursor.getString(10));
                products.setCatName(cursor.getString(11));
                products.setisactive(cursor.getString(12));
                products.setisfeatured(cursor.getString(13));
                products.setissynced(cursor.getString(14));
                Log.d("tag", "id=" + id + "& product=" + products.getName());
            }
        }

        cursor.close();
        db.close();
        return products;

    }

    public void removeAll() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_PRODUCTS);
        db.close();
    }

    public List<String> getAllStyleIDs() {
        List<String> results = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query
        String selectQuery = "select * from " + TABLE_PRODUCTS;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                // results.add(cursor.getInt(0));
                results.add(cursor.getString(2));

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return results;
    }

    public List<Products> getNotSyncedProducts() {
        List<Products> productList = new ArrayList<Products>();
        // Select All Query
        String selectQuery = "SELECT *  FROM " + TABLE_PRODUCTS + " WHERE "
                + KEY_ISSYNCED + " = 'false'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Products products = new Products();
                products.setId(cursor.getString(0));
                products.setProductId(cursor.getString(1));
                products.setName(cursor.getString(2));
                products.setStock(cursor.getString(3));
                products.setPrice(cursor.getString(4));
                products.setShortDescription(cursor.getString(5));
                products.setImage(cursor.getString(6));
                products.setImage2(cursor.getString(7));
                products.setImage3(cursor.getString(8));
                products.setImage4(cursor.getString(9));
                products.setImage5(cursor.getString(10));
                products.setCatName(cursor.getString(11));
                products.setisactive(cursor.getString(12));
                products.setisfeatured(cursor.getString(13));
                products.setissynced(cursor.getString(14));
                productList.add(products);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return productList;
    }

    public List<Products> getAllProducts(int offset, int limit) {
        List<Products> productList = new ArrayList<Products>();

        // Select page Query
        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS + " order by "
                + KEY_ID + " DESC " + "   LIMIT " + limit + " OFFSET " + offset;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + TABLE_PRODUCTS,
                null);
        mCount.moveToFirst();
        //ManageProducts.total = mCount.getInt(0);
        mCount.close();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Products products = new Products();
                products.setId(cursor.getString(0));
                products.setProductId(cursor.getString(1));
                products.setName(cursor.getString(2));
                products.setStock(cursor.getString(3));
                products.setPrice(cursor.getString(4));
                products.setShortDescription(cursor.getString(5));
                products.setImage(cursor.getString(6));
                products.setImage2(cursor.getString(7));
                products.setImage3(cursor.getString(8));
                products.setImage4(cursor.getString(9));
                products.setImage5(cursor.getString(10));
                products.setCatName(cursor.getString(11));
                products.setisactive(cursor.getString(12));
                products.setisfeatured(cursor.getString(13));
                products.setissynced(cursor.getString(14));
                // Adding contact to list
                productList.add(products);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return productList;
    }

    */
//
//    public int updateCategory(Categories category, String name) {
//        /*
//         * Updates single category in the database.
//         */
//        name = name.replace("'", "");
//        name = name.replace('"', ' ');
//        Log.d("", "sql check" + category.getparentname());
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_CAT_ID, category.getCid());
//        values.put(KEY_CAT_NAME, category.getname()); // Contact Name
//        values.put(KEY_PARENT_NAME, category.getparentname());
//        values.put(KEY_CAT_IMAGE, category.getImage());
//        values.put(KEY_CAT_FEATURED, category.getFeatured());
//        values.put(KEY_CAT_SYNCED, category.getissynced());
//        int ret = db.update(TABLE_CATEGORIES, values, KEY_CAT_NAME + " = ?",
//                new String[]{String.valueOf(name)});
//        // updating row
//        ContentValues values1 = new ContentValues();
//        values1.put(KEY_PARENT_NAME, category.getname());
//        values1.put(KEY_CAT_SYNCED, category.getissynced());
//        try {
//            int ret1 = db.update(TABLE_CATEGORIES, values1, KEY_PARENT_NAME
//                    + " = ?", new String[]{String.valueOf(name)});
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        db.close();
//        return ret;
//    }
//
//    // =============update by name
//    public int updateCategorybyname(Categories category) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_CAT_NAME, category.getname()); // Contact Name
//        values.put(KEY_CAT_IMAGE, category.getImage());
//        values.put(KEY_CAT_SYNCED, category.getissynced());
//        values.put(KEY_CAT_FEATURED, category.getFeatured());
//        int ret = db.update(TABLE_CATEGORIES, values, KEY_CAT_NAME + " = ?",
//                new String[]{String.valueOf(category.getname())});
//        // updating row
//        db.close();
//        return ret;
//    }
//
//    public List<Categories> getNotSyncedCategory() {
//        List<Categories> productList = new ArrayList<Categories>();
//        // Select All Query
//        String selectQuery = "SELECT *  FROM " + TABLE_CATEGORIES + " WHERE "
//                + KEY_CAT_SYNCED + " = 'false'";
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Categories category = new Categories();
//                category.setId(cursor.getString(0));
//                category.setCid(cursor.getString(1));
//                category.setname(cursor.getString(2));
//                category.setparentname(cursor.getString(3));
//                category.setImage(cursor.getString(4));
//                category.setFeatured(cursor.getString(5));
//                productList.add(category);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return productList;
//    }
//
//    public boolean checkSync() {
//        List<Categories> cat_list = getNotSyncedCategory();
//        List<Products> prod_list = getNotSyncedProducts();
//        List<Customers> cust_list = getNotAddedCustomer();
//        List<Customers> cust_list1 = getNotUpdatedCustomers();
//        if (cat_list.isEmpty() && prod_list.isEmpty() && cust_list.isEmpty()
//                && cust_list1.isEmpty()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public void addcategory(Categories category) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        // values.put(KEY_CAT_ID, categoryCounter++); // Contact Name
//        values.put(KEY_CAT_ID, category.getCid());
//        values.put(KEY_CAT_NAME, category.getname()); // Contact Phone
//        values.put(KEY_PARENT_NAME, category.getparentname());
//        values.put(KEY_CAT_IMAGE, category.getImage());
//        values.put(KEY_CAT_FEATURED, category.getissynced());
//        values.put(KEY_CAT_SYNCED, category.getissynced());
//        // Inserting Row
//        db.insert(TABLE_CATEGORIES, null, values);
//        db.close(); // Closing database connection
//
//        // SplashScreen.dbPrefs.edit().putInt("categoryCounter",
//        // categoryCounter).apply();
//
//    }
//
//    public void removeAllcat() {
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("delete from " + TABLE_CATEGORIES);
//        db.close();
//    }
//
//    public boolean errorsynccategory() {
//        boolean shouldsync = false;
//        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                if (cursor.getString(6).equals("false")) {
//                    shouldsync = true;
//                    break;
//                }
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return shouldsync;
//    }
//
//    public List<Categories> getAllCategory() {
//        List<Categories> categorylist = new ArrayList<Categories>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Categories category = new Categories();
//                category.setId(cursor.getString(0));
//                category.setCid(cursor.getString(1));
//                category.setname(cursor.getString(2));
//                category.setparentname(cursor.getString(3));
//                category.setImage(cursor.getString(4));
//                category.setFeatured(cursor.getString(5));
//                category.setissynced(cursor.getString(6));
//                // Adding category to list
//                categorylist.add(category);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        // return contact list
//        return categorylist;
//    }
//
//
//    public Categories getCatById(String catid) {
//        Categories category = new Categories();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES + " where " + KEYID + " ='" + catid + "'";
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//
//            category.setId(cursor.getString(0));
//            category.setCid(cursor.getString(1));
//            category.setname(cursor.getString(2));
//            category.setparentname(cursor.getString(3));
//            category.setImage(cursor.getString(4));
//            category.setFeatured(cursor.getString(5));
//            category.setissynced(cursor.getString(6));
//            // Adding category to list
//        }
//        cursor.close();
//        db.close();
//        // return contact list
//        return category;
//    }
//
//    public ArrayList<Categories> getAllCategory_order() {
//        ArrayList<Categories> categorylist = new ArrayList<Categories>();
//        // Select All Query
//
//        String selectQuery = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE "
//                + KEY_PARENT_NAME + " = \"None\"" + " order by " + KEYID
//                + " DESC ";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Categories category = new Categories();
//                category.setId(cursor.getString(0));
//                category.setCid(cursor.getString(1));
//                category.setname(cursor.getString(2));
//                category.setparentname(cursor.getString(3));
//                category.setImage(cursor.getString(4));
//                category.setFeatured(cursor.getString(5));
//                category.setissynced(cursor.getString(6));
//                // Adding category to list
//                categorylist.add(category);
//
//                // childern of this cat
//                selectQuery = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE "
//                        + KEY_PARENT_NAME + " = \"" + cursor.getString(2)
//                        + "\"" + " order by " + KEYID + " DESC ";
//                Cursor cursor1 = db.rawQuery(selectQuery, null);
//                if (cursor1.moveToFirst()) {
//                    do {
//                        category = new Categories();
//                        category.setId(cursor1.getString(0));
//                        category.setCid(cursor1.getString(1));
//                        category.setname(cursor1.getString(2));
//                        category.setparentname(cursor1.getString(3));
//                        category.setImage(cursor1.getString(4));
//                        category.setFeatured(cursor1.getString(5));
//                        category.setissynced(cursor1.getString(6));
//                        // Adding category to list
//                        categorylist.add(category);
//
//                    } while (cursor1.moveToNext());
//                }
//                cursor1.close();
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        // return contact list
//        return categorylist;
//    }
//
//    public void removeCategory() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("delete from " + TABLE_CATEGORIES);
//    }
//
//    public String getIdFromCatName(String name) {
//        String selectQuery = "SELECT *  FROM " + TABLE_CATEGORIES;
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        cursor.moveToFirst();
//        String id = null;
//
//        if (cursor.moveToFirst())
//            do {
//                {
//                    if (cursor.getString(2).equals(name)) {
//                        id = cursor.getString(1);
//                    }
//                }
//            } while (cursor.moveToNext());
//        cursor.close();
//        db.close();
//        return id;
//
//    }
//
//    public String getCatNameFromId(String id) {
//        String selectQuery = "SELECT *  FROM " + TABLE_CATEGORIES + " WHERE "
//                + KEY_CAT_ID + " ='" + id + "'";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        String name = null;
//        cursor.moveToFirst();
//        Log.d("count", String.valueOf(cursor.getCount()));
//        if (cursor.getCount() > 0) {
//            Log.d("before if", cursor.getString(2));
//            if (cursor.getString(1).equals(id)) {
//                name = cursor.getString(2);
//            }
//        }
//        cursor.close();
//        db.close();
//        return name;
//    }
//
//    public String getCategoryIdFromName(String name) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String queryString = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE "
//                + KEY_CAT_NAME + " = '" + name + "'";
//        Cursor mCursor = db.rawQuery(queryString, null);
//        String cat_id = null;
//        if (mCursor != null) {
//            if (mCursor.moveToFirst()) {
//                cat_id = mCursor.getString(0);
//            }
//            mCursor.close();
//        }
//        db.close();
//        return cat_id;
//    }
//
//    public List<String> getAllCategoryIDs() {
//        List<String> results = new ArrayList<String>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        String selectQuery = "select * from " + TABLE_CATEGORIES + " where "
//                + KEY_CAT_SYNCED + " ='true'";
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                results.add(cursor.getString(2));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return results;
//    }
//
//    public List<String> getEligibleParentCategories() {
//        /*
//         * Returns list of categories which are qualified to be parents.
//         */
//        List<String> results = new ArrayList<String>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        // Select Parent Categories Query
//        String selectQuery = "SELECT * FROM " + TABLE_CATEGORIES + " WHERE "
//                + KEY_PARENT_NAME + " = 'None' and " + KEY_CAT_SYNCED
//                + " = 'true'";
//
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                results.add(cursor.getString(2));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//
//        return results;
//    }
//
//    public List<String> getEligibleParentCategories_update(String cat,
//                                                           String parent) {
//        /*
//         * Returns list of categories which are qualified to be parents.
//         */
//        List<String> results = new ArrayList<String>();
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        if (parent.equalsIgnoreCase("None")) {
//            String selectQuery = "SELECT * FROM " + TABLE_CATEGORIES
//                    + " WHERE " + KEY_PARENT_NAME + " = '" + cat + "'";
//            Cursor cursor = db.rawQuery(selectQuery, null);
//            if (cursor.moveToFirst()) {
//                do {
//                    cursor.close();
//                    db.close();
//                    return results;
//                } while (cursor.moveToNext());
//            }
//
//            if (cursor != null) {
//                cursor.close();
//                db.close();
//            }
//        } else {
//
//            return getEligibleParentCategories();
//        }
//
//        return null;
//    }
//
//    public List<String> getAllCategoryNames() {
//        List<String> results = new ArrayList<String>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        // Select All Query
//        String selectQuery = "select * from " + TABLE_CATEGORIES;
//
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                // results.add(cursor.getInt(0));
//                results.add(cursor.getString(2));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//
//        return results;
//    }
//
//    public boolean isTableExists(String tableName, boolean openDb) {
//        SQLiteDatabase db = null;
//        // if(openDb) {
//        if (db == null || !db.isOpen()) {
//            db = this.getReadableDatabase();
//        }
//        if (!db.isReadOnly()) {
//            db = getReadableDatabase();
//        }
//        Cursor cursor = db.rawQuery(
//                "select DISTINCT tbl_name from sqlite_master where tbl_name = '"
//                        + tableName + "'", null);
//        if (cursor != null) {
//            if (cursor.getCount() > 0) {
//                cursor.close();
//                return true;
//            }
//            cursor.close();
//        }
//        db.close();
//        return false;
//    }
//
//    public boolean isDatabaseEmpty() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor mCursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
//        Boolean rowExists;
//
//        if (mCursor.moveToFirst()) {
//            rowExists = true;
//        } else {
//            rowExists = false;
//        }
//
//        db.close();
//        return rowExists;
//    }
//
//    public String getProductIdFromTitle(String title) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String queryString = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE "
//                + KEY_TITLE + " = '" + title + "'";
//        Log.d("Product name", title);
//        Cursor mCursor = db.rawQuery(queryString, null);
//        String product_id = null;
//
//        if (mCursor != null) {
//            if (mCursor.moveToFirst()) {
//                product_id = mCursor.getString(1);
//                Log.d("ID", product_id);
//            }
//            mCursor.close();
//        }
//        db.close();
//        return product_id;
//    }
//
//    // -------------------------------
//    // ---------------------------------
//    // customer table database functions
//
//    public void addCustomer(Customers customer) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_EMAIL, customer.getEmail());
//        values.put(KEY_NAME, customer.getName());
//        values.put(KEY_SEX, customer.getSex());
//        values.put(KEY_BDAY, customer.getBday());
//        values.put(KEY_ANNIVERSARY, customer.getAnniversary());
//        values.put(KEY_CONTACT, customer.getContact());
//        values.put(KEY_ADD, customer.getAdd());
//        values.put(KEY_CITY, customer.getCity());
//        values.put(KEY_STATE, customer.getState());
//        values.put(KEY_PIN, customer.getPin());
//        values.put(KEY_COUNTRY, customer.getCountry());
//        values.put(KEY_P_ID, customer.getPid());
//        values.put(KEY_S_ID, customer.getSid());
//        values.put(KEY_R_URI, customer.getRURI());
//        values.put(KEY_ISSYNCED, customer.getSync());
//        values.put(KEY_LAST_NAME, customer.getLastName());
//        // Inserting Row
//        db.insert(TABLE_CUSTOMER, null, values);
//        db.close(); // Closing database connection
//    }
//
//    public int updateSingleCustomer(Customers customer) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        int ret = 0;
//        ContentValues values = new ContentValues();
//        values.put(KEY_EMAIL, customer.getEmail());
//        values.put(KEY_NAME, customer.getName());
//        values.put(KEY_SEX, customer.getSex());
//        values.put(KEY_BDAY, customer.getBday());
//        values.put(KEY_ANNIVERSARY, customer.getAnniversary());
//        values.put(KEY_CONTACT, customer.getContact());
//        values.put(KEY_ADD, customer.getAdd());
//        values.put(KEY_CITY, customer.getCity());
//        values.put(KEY_STATE, customer.getState());
//        values.put(KEY_PIN, customer.getPin());
//        values.put(KEY_COUNTRY, customer.getCountry());
//        values.put(KEY_P_ID, customer.getPid());
//        values.put(KEY_S_ID, customer.getSid());
//        values.put(KEY_R_URI, customer.getRURI());
//        values.put(KEY_ISSYNCED, customer.getSync());
//        values.put(KEY_LAST_NAME, customer.getLastName());
//        try {
//            ret = db.update(TABLE_CUSTOMER, values, KEY_EMAIL + " = ?",
//                    new String[]{String.valueOf(customer.getEmail())});
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        }
//
//        // updating row
//        db.close();
//        return ret;
//
//    }
//
//    public int updateSingleCustomer(Customers customer, String email) {
//
//        email = email.replace("'", "");
//        email = email.replace('"', ' ');
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        int ret = 0;
//        ContentValues values = new ContentValues();
//        values.put(KEY_EMAIL, customer.getEmail());
//        values.put(KEY_NAME, customer.getName());
//        values.put(KEY_SEX, customer.getSex());
//        values.put(KEY_BDAY, customer.getBday());
//        values.put(KEY_ANNIVERSARY, customer.getAnniversary());
//        values.put(KEY_CONTACT, customer.getContact());
//        values.put(KEY_ADD, customer.getAdd());
//        values.put(KEY_CITY, customer.getCity());
//        values.put(KEY_STATE, customer.getState());
//        values.put(KEY_PIN, customer.getPin());
//        values.put(KEY_COUNTRY, customer.getCountry());
//        values.put(KEY_P_ID, customer.getPid());
//        values.put(KEY_S_ID, customer.getSid());
//        values.put(KEY_R_URI, customer.getRURI());
//        values.put(KEY_ISSYNCED, customer.getSync());
//        values.put(KEY_LAST_NAME, customer.getLastName());
//        try {
//            ret = db.update(TABLE_CUSTOMER, values, KEY_P_ID + " = ?",
//                    new String[]{String.valueOf(email)});
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        }
//
//        // updating row
//        db.close();
//        return ret;
//
//    }
//
//
//    public void addallCustomer(Customers customer) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_EMAIL, customer.getEmail());
//        values.put(KEY_NAME, customer.getName());
//        values.put(KEY_SEX, customer.getSex());
//        values.put(KEY_BDAY, customer.getBday());
//        values.put(KEY_ANNIVERSARY, customer.getAnniversary());
//        values.put(KEY_CONTACT, customer.getContact());
//        values.put(KEY_ADD, customer.getAdd());
//        values.put(KEY_CITY, customer.getCity());
//        values.put(KEY_STATE, customer.getState());
//        values.put(KEY_PIN, customer.getPin());
//        values.put(KEY_COUNTRY, customer.getCountry());
//        values.put(KEY_P_ID, customer.getPid());
//        values.put(KEY_S_ID, customer.getSid());
//        values.put(KEY_R_URI, customer.getRURI());
//        values.put(KEY_ISSYNCED, customer.getSync());
//        values.put(KEY_LAST_NAME, customer.getLastName());
//        // Inserting Row1
//        db.insert(TABLE_CUSTOMER, null, values);
//        db.close(); // Closing database connection
//        // Toast.makeText(context,"hi", Toast.LENGTH_LONG).show();
//        // SplashScreen.dbPrefs.edit().putInt("productCounter",
//        // productCounter).apply();
//    }
//
//    public boolean checkDuplicateCustomer(String email, String fname, String lname, String contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        boolean answer = false;
//        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMER + " WHERE "
//                + KEY_EMAIL + " = '" + email + "'" + " AND "
//                + KEY_NAME + " = '" + fname + "'" + " AND "
//                + KEY_LAST_NAME + " = '" + lname + "'" + " AND "
//                + KEY_CONTACT + " = '" + contact + "'";
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//
//                answer = true;
//
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return answer;
//    }
//
//    public boolean checkDuplicateCustomer(String check) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        boolean answer = false;
//        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMER + " where "
//                + KEY_CONTACT + " = '" + check + "'";
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//
//                answer = true;
//
//            } while (cursor.moveToNext());
//
//        }
//        cursor.close();
//        db.close();
//        return answer;
//    }
//
//    public boolean errorsyncAddCustomer() {
//        boolean shouldsync = false;
//        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMER + " where "
//                + KEY_ISSYNCED + " = 'false' and " + KEY_P_ID + " ='-1'";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                shouldsync = true;
//                break;
//
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return shouldsync;
//
//    }
//
//    public boolean errorsyncUpdateCustomer() {
//        boolean shouldsync = false;
//        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMER + " where "
//                + KEY_ISSYNCED + " = 'false' and " + KEY_P_ID + " !='-1'";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                shouldsync = true;
//                break;
//
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return shouldsync;
//
//    }
//
//    public ArrayList<Customers> getAllCustomers() {
//        ArrayList<Customers> customerList = new ArrayList<Customers>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMER + " order by "
//                + KEY_P_ID + " DESC ";
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                Customers customers = new Customers();
//                customers.setId(cursor.getString(0));
//                customers.setPid(cursor.getString(1));
//                customers.setName(cursor.getString(2));
//                customers.setEmail(cursor.getString(3));
//                customers.setContact(cursor.getString(4));
//                customers.setAdd(cursor.getString(5));
//                customers.setCity(cursor.getString(6));
//                customers.setState(cursor.getString(7));
//                customers.setPin(cursor.getString(8));
//                customers.setCountry(cursor.getString(9));
//                customers.setBday(cursor.getString(10));
//                customers.setAnniversary(cursor.getString(11));
//                customers.setSex(cursor.getString(12));
//                customers.setSid(cursor.getString(13));
//                customers.setRURI(cursor.getString(14));
//                customers.setLastName(cursor.getString(16));
//                customers.setSync(cursor.getString(18));
//                // Adding contact to list
//                customerList.add(customers);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        // return contact list
//        return customerList;
//    }
//
//
//    public Customers getCustById(String id) {
//        Customers customers = new Customers();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_CUSTOMER + " where " + KEY_ID + " = '" + id + "'";
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            customers.setId(cursor.getString(0));
//            customers.setPid(cursor.getString(1));
//            customers.setName(cursor.getString(2));
//            customers.setEmail(cursor.getString(3));
//            customers.setContact(cursor.getString(4));
//            customers.setAdd(cursor.getString(5));
//            customers.setCity(cursor.getString(6));
//            customers.setState(cursor.getString(7));
//            customers.setPin(cursor.getString(8));
//            customers.setCountry(cursor.getString(9));
//            customers.setBday(cursor.getString(10));
//            customers.setAnniversary(cursor.getString(11));
//            customers.setSex(cursor.getString(12));
//            customers.setSid(cursor.getString(13));
//            customers.setRURI(cursor.getString(14));
//            customers.setLastName(cursor.getString(16));
//            customers.setSync(cursor.getString(18));
//            // Adding contact to list
//        }
//        cursor.close();
//        db.close();
//        // return contact list
//        return customers;
//    }
//
//    public void removeAllCustomers() {
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("delete from " + TABLE_CUSTOMER);
//        db.close();
//    }
//
//    public List<Customers> getNotUpdatedCustomers() {
//        List<Customers> customerList = new ArrayList<Customers>();
//        // Select All Query
//        String selectQuery = "SELECT *  FROM " + TABLE_CUSTOMER + " WHERE "
//                + KEY_ISSYNCED + " = 'false' and " + KEY_P_ID + "!= '-1'";
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                Customers customers = new Customers();
//                customers.setId(cursor.getString(0));
//                customers.setPid(cursor.getString(1));
//                customers.setName(cursor.getString(2));
//                customers.setEmail(cursor.getString(3));
//                customers.setContact(cursor.getString(4));
//                customers.setAdd(cursor.getString(5));
//                customers.setCity(cursor.getString(6));
//                customers.setState(cursor.getString(7));
//                customers.setPin(cursor.getString(8));
//                customers.setCountry(cursor.getString(9));
//                customers.setBday(cursor.getString(10));
//                customers.setAnniversary(cursor.getString(11));
//                customers.setSex(cursor.getString(12));
//                customers.setSid(cursor.getString(13));
//                customers.setRURI(cursor.getString(14));
//                customers.setLastName(cursor.getString(16));
//                customers.setSync(cursor.getString(18));
//                customerList.add(customers);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return customerList;
//    }
//
//    public List<Customers> getNotAddedCustomer() {
//        List<Customers> customerList = new ArrayList<Customers>();
//        // Select All Query
//        String selectQuery = "SELECT *  FROM " + TABLE_CUSTOMER + " WHERE "
//                + KEY_ISSYNCED + " = 'false' and " + KEY_P_ID + "= '-1'";
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                Customers customers = new Customers();
//                customers.setId(cursor.getString(0));
//                customers.setPid(cursor.getString(1));
//                customers.setName(cursor.getString(2));
//                customers.setEmail(cursor.getString(3));
//                customers.setContact(cursor.getString(4));
//                customers.setAdd(cursor.getString(5));
//                customers.setCity(cursor.getString(6));
//                customers.setState(cursor.getString(7));
//                customers.setPin(cursor.getString(8));
//                customers.setCountry(cursor.getString(9));
//                customers.setBday(cursor.getString(10));
//                customers.setAnniversary(cursor.getString(11));
//                customers.setSex(cursor.getString(12));
//                customers.setSid(cursor.getString(13));
//                customers.setRURI(cursor.getString(14));
//                customers.setLastName(cursor.getString(16));
//                customers.setSync(cursor.getString(18));
//                customerList.add(customers);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return customerList;
//    }
//
//
//    public String getAllImagenotFoundProducts() {
//        List<Products> productList = new ArrayList<Products>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_PRODUCTS + " where " + KEY_ISSYNCED + " ='false'";
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        String _string = "";
//        if (cursor.moveToFirst()) {
//            do {
//                Boolean exists = true;
//                if (!ImageExists(cursor.getString(cursor.getColumnIndex(KEY_IMAGE)))) {
//                    exists = false;
//                } else if (!ImageExists(cursor.getString(cursor.getColumnIndex(KEY_IMAGE2)))) {
//                    exists = false;
//                } else if (!ImageExists(cursor.getString(cursor.getColumnIndex(KEY_IMAGE3)))) {
//                    exists = false;
//                } else if (!ImageExists(cursor.getString(cursor.getColumnIndex(KEY_IMAGE4)))) {
//                    exists = false;
//                } else if (!ImageExists(cursor.getString(cursor.getColumnIndex(KEY_IMAGE5)))) {
//                    exists = false;
//                }
//                if (!exists) {
//                    _string = _string + cursor.getString(cursor.getColumnIndex(KEY_TITLE)) + ",";
//                }
//
//
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        // return contact list
//        return _string;
//    }
//
//
//    public String getAllIMageNOtFoundCategory() {
//        List<Categories> categorylist = new ArrayList<Categories>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES + " where " + KEY_ISSYNCED + " ='false'";
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        String _string = "";
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                if (!ImageExists(cursor.getString(cursor.getColumnIndex(KEY_IMAGE)))) {
//                    _string = _string + cursor.getString(cursor.getColumnIndex(KEY_CAT_NAME)) + ",";
//                }
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        // return contact list
//        return _string;
//    }
//
//
//    private Boolean ImageExists(String path) {
//        if (!path.equalsIgnoreCase("null")
//                && !path.contains(Constants.base_uri)) {
//            File check = new File(path);
//            if (!check.exists()) {
//                return false;
//            }
//        }
//
//        return true;
//    }

}
