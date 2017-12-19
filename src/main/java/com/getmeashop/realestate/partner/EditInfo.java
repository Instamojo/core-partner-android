package com.getmeashop.realestate.partner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.getmeashop.realestate.partner.util.Constants;
import com.getmeashop.realestate.partner.util.GetRequest;
import com.getmeashop.realestate.partner.util.ImageConverter;
import com.getmeashop.realestate.partner.util.Interfaces;
import com.getmeashop.realestate.partner.util.PatchRequest;
import com.getmeashop.realestate.partner.util.PostRequest;
import com.getmeashop.realestate.partner.util.PutRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class EditInfo extends AppCompatActivity implements Callbacks, Interfaces.PutCallbacks, Interfaces.removeImg, BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener, Interfaces.enterLocation,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected static final String TAG = "location-updates-sample";
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private final static int CAMERA_REQUEST = 1888;
    private final static int GALLERY_REQUEST = 1889;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    Button update, update_shipping;
    File fileUri;
    int clicked;
    ProgressDialog dialog;
    Context context;
    String uri_update = "", uri_shipping = "";
    String uri_themes, uri_themes_put, uri_lookup_domain, uri_reserve_domain = Constants.uri_domain_register;
    String store_info_contact, store_info_address, store_name, store_minimum, store_shipping, store_image;
    String theme_name, store_info_domain, store_info_domain_status, check_domain, domain_error;
    Toolbar mtoolbar;
    Button select_theme, select_domain, cancel_domain;
    HashMap<String, String> url_maps = new HashMap<String, String>();
    private EditText contact, address, name, minimum, shipping, edit_domain;
    private TextView theme, domain_name, err_domain;
    private ImageView store_logo;
    private SharedPreferences sp;
    private String image, id;
    private SharedPreferences.Editor editor;
    private SliderLayout mDemoSlider;
    private boolean select_theme_pressed = false, domain_available = false;
    private Button rotate;
    private ArrayList<String> domain_suggestions;
    private LinearLayout domain_list;
    private Button getLocation;
    private TextView location;
    private String userid;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        store_info_contact = "";
        store_info_address = "";
        image = "";
        store_image = "";
        store_shipping = "";
        store_minimum = "";
        store_info_domain_status = "";
        store_info_domain = "";
        check_domain = "";
        domain_available = false;
        theme_name = "";


        sp = this.getSharedPreferences("Users", Context.MODE_PRIVATE);
        editor = sp.edit();

        context = this;
        contact = (EditText) findViewById(R.id.contact);
        address = (EditText) findViewById(R.id.address);
        name = (EditText) findViewById(R.id.name);
        store_logo = (ImageView) findViewById(R.id.store_logo);
        update = (Button) findViewById(R.id.update_info);
        theme = (TextView) findViewById(R.id.theme);
        select_theme = (Button) findViewById(R.id.select_theme);
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);
        rotate = (Button) findViewById(R.id.rotate);

        select_domain = (Button) findViewById(R.id.select_domain);
        cancel_domain = (Button) findViewById(R.id.cancel_domain);

        getLocation = (Button) findViewById(R.id.getLocation);
        location = (TextView) findViewById(R.id.location);

        domain_name = (TextView) findViewById(R.id.domain_name);
        err_domain = (TextView) findViewById(R.id.err_domain);
        edit_domain = (EditText) findViewById(R.id.edit_domain);
        domain_list = (LinearLayout) findViewById(R.id.domain_suggestions);

        minimum = (EditText) findViewById(R.id.minimum);
        shipping = (EditText) findViewById(R.id.shipping);
        update_shipping = (Button) findViewById(R.id.update_shipping);
        // image = sp.getString("store_info_store_logo", "null");
        // base_uri = Constants.base_uri;

        id = getIntent().getStringExtra("storeid");
        userid = getIntent().getStringExtra("id");
        uri_update = Constants.uri_store_info + id + "/";
        uri_shipping = Constants.uri_shipping_info + id + "/";

        //uri_themes = Constants.base_uri + "mobile/theme/?userid=" + userid + "&format=json";
        uri_themes = Constants.base_uri + "mobile/themes/?format=json";
        uri_themes_put = Constants.uri_store_info + id + "/";
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(" Checking details Please wait...");
        dialog.setCancelable(false);

        //TODO add refresh button instead of loading every time

        new GetRequest(this, uri_update + "?format=json", context);


        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();


        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showSettingsAlert();
                } else {
                    if (mGoogleApiClient.isConnected()) {
                        if (!mRequestingLocationUpdates) {
                            startLocationUpdates();
                            getLocation.setVisibility(View.GONE
                            );
                        }
                    } else {
                        Utils.showToast("waiting for location", context);
                    }
                }
            }
        });

        store_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileUri = Utils.getOutputMediaFileUri(context, "store_info_store_logo.jpg");
                Utils.choose_image_from(context, fileUri, (Activity) context, null, image);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().trim().equalsIgnoreCase("")) {
                    Utils.showToast("Enter valid Store name", context);
                } else if (address.getText().toString().trim().equalsIgnoreCase("")) {
                    Utils.showToast("Enter valid address", context);
                } else if (contact.getText().toString().trim().equalsIgnoreCase("") || contact.getText().toString().trim().length() < 10) {
                    Utils.showToast("Enter valid contact number", context);
                } else {
                    if (changed())
                        new PatchRequest((Callbacks) context, uri_update, context);
                }
            }
        });

        update_shipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (minimum.getText().toString().trim().length() < 0) {
                    Utils.showToast("Enter valid minimum order value", context);
                } else if (shipping.getText().toString().trim().length() < 0) {
                    Utils.showToast("Enter valid shipping charge", context);
                } else {
                    if (changedShipping())
                        new PutRequest((Interfaces.PutCallbacks) context, uri_shipping, context);
                }
            }
        });

        select_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (select_theme_pressed) {
                    theme_name = (new ArrayList<String>(url_maps.keySet())).get(mDemoSlider.getCurrentPosition());
                    new PatchRequest(EditInfo.this, uri_themes_put, EditInfo.this);
                } else {
                    new GetRequest(EditInfo.this, uri_themes, EditInfo.this);
                }
            }
        });

        edit_domain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                select_domain.setText("Lookup Domain");
                domain_available = false;
                err_domain.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        select_domain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_domain.getVisibility() == View.GONE) {
                    //domain_name.setVisibility(View.GONE);
                    edit_domain.setVisibility(View.VISIBLE);
                    select_domain.setText("Lookup domain");
                    cancel_domain.setVisibility(View.VISIBLE);
                } else {
                    check_domain = edit_domain.getText().toString().trim();
                    if (check_domain.length() == 0) {
                        Utils.showToast("Please enter a valid domain", EditInfo.this);
                    } else {
                        if (check_domain.contains("www.")) {
                            check_domain = check_domain.substring(check_domain.indexOf("."));
                        } else
                            try {
                                uri_lookup_domain = Constants.uri_lookup_domain + "?query=" + URLEncoder.encode(check_domain, "UTF-8");
                            } catch (Exception e) {
                                Utils.showToast("Please enter a valid url", EditInfo.this);
                                e.printStackTrace();
                            }
                        new GetRequest(EditInfo.this, uri_lookup_domain, EditInfo.this);

                    }
                }
            }
        });

        cancel_domain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDomainLayout(store_info_domain);
            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImgAfterRotate();
            }
        });
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_info, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
           finish();
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * called when image selecting activity call back
     * <p/>
     * This method handles the intent data/file_uri and store image accordingly
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            if (fileUri != null && process_image(fileUri.getAbsolutePath(), 1) == -1) {
                Utils.showToast("Unable to save image, Please try again",
                        getApplicationContext());
            }

        } else if (requestCode == GALLERY_REQUEST
                && resultCode == Activity.RESULT_OK && data != null) {


            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = {MediaStore.Images.Media.DATA};


            String imagePath = Utils.getPath(getApplicationContext(), pickedImage);
            // Now we need to set the GUI ImageView data with data read from
            // the
            // picked file.
            if (imagePath == null || imagePath.equalsIgnoreCase("error_image_url"))
                Utils.showToast("Please select image from local storage",
                        getApplicationContext());

            else if (process_image(imagePath, 0) == -1) {
                Utils.showToast("Unable to save image",
                        getApplicationContext());
            }
            // At the end remember to close the cursor or you will end with
            // the
            // RuntimeException!

        }

    }

    /**
     * called by activityresult after obtaining path of image
     * <p/>
     * This method scales original image, stores a copy of scaled and compressed
     * image , set image variables
     *
     * @param cam to tell if camera activity was used
     */
    private int process_image(String path, int cam) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);

        float height = bmOptions.outHeight;
        float width = bmOptions.outWidth;

        int reqw = 1280;
        int reqh = ImageConverter.calheight(
                reqw, height, width);

        Bitmap scaledImage = ImageConverter
                .decodeSampledBitmapFromResource(path, reqw, reqh, (int) width,
                        (int) height);
        clicked = 0;
        if (scaledImage == null) {
            return -1;
        } else {
            if (cam == 1) {
                Utils.saveImg(path, 80, scaledImage, true);
                clicked = 1;
                image = path;
            } else {
                String path2 = Utils.getOutputMediaFileUri(
                        getApplicationContext(), path.substring(path.lastIndexOf("/"))).toString();
                Utils.saveImg(path2, 80, scaledImage, true);
                image = path2;
            }
            store_logo.setImageBitmap(scaledImage);
            scaledImage = null;
            if (image.equalsIgnoreCase("") || image.equalsIgnoreCase("null") || image.contains(Constants.base_uri)) {
                rotate.setVisibility(View.INVISIBLE);
            } else {
                rotate.setVisibility(View.VISIBLE);
            }
            //delivery_text.setText(image);
        }
        return 0;
    }

    @Override
    public void postexecute(String url, int status) {
        if (status == 200 || status == 201 || status == 202) {
            if (url.equalsIgnoreCase(uri_update + "?format=json")) {
                setvalues();
                setDomainLayout(store_info_domain);
            } else if (url.equalsIgnoreCase(uri_update)) {
                Utils.showToast("Successfully updated", context);
                setvalues();
                finish();
            } else if (url.equalsIgnoreCase(uri_themes)) {
                setThemesLayout();
            } else if (url.equalsIgnoreCase(uri_themes_put)) {
                select_theme_pressed = false;
                mDemoSlider.setVisibility(View.GONE);
                theme.setText(theme_name);
                select_theme.setText("Change Theme");
                Utils.showToast("Theme updated successfully", context);
            } else if (url.equalsIgnoreCase(uri_lookup_domain)) {
                err_domain.setVisibility(View.VISIBLE);
                if (domain_available) {
                    domain_list.setVisibility(View.VISIBLE);
                    addToList();
                    if (domain_suggestions.get(0).contains(check_domain)) {
                        err_domain.setText("Domain available :");
                    } else {
                        err_domain.setText("Domain unavailable, consider following :");
                    }
                    //select_domain.setText("Reserve");
                    cancel_domain.setVisibility(View.VISIBLE);
                } else {
                    domain_list.setVisibility(View.GONE);
                    err_domain.setText("No suggestions available.");
                    cancel_domain.setVisibility(View.VISIBLE);
                }
            } else if (url.equalsIgnoreCase(uri_reserve_domain)) {
                store_info_domain_status = "reserved";
                store_info_domain = check_domain;
                check_domain = "";
                domain_available = false;
                setDomainLayout(store_info_domain);
            } else if (url.equalsIgnoreCase(uri_shipping + "?format=json")) {
                setvaluesShipping();
            } else if (url.equalsIgnoreCase(uri_shipping)) {
                Utils.showToast("Successfully updated", context);
                setvaluesShipping();
                finish();
            }
        } else if (status == 400) {
            if (url.equalsIgnoreCase(uri_reserve_domain)) {
                err_domain.setVisibility(View.VISIBLE);
                cancel_domain.setVisibility(View.VISIBLE);
                err_domain.setText("Domain is available but reserved by another broker");
            } else if (url.equalsIgnoreCase(uri_lookup_domain)) {
                err_domain.setVisibility(View.VISIBLE);
                cancel_domain.setVisibility(View.VISIBLE);
                err_domain.setText(domain_error);
                select_domain.setText("Lookup Domain");
            }
        } else if (status != 404) {
            if (url.equalsIgnoreCase(uri_update)) {
                Utils.showToast("failed to update info, please try again later", context);
            } else
                Utils.showToast("failed to retrieve data, please try again later", context);
        }


        dialog.dismiss();

    }

    @Override
    public void preexecute(String url) {
        if (url.equalsIgnoreCase(uri_update + "?format=json") || url.equalsIgnoreCase(update_shipping + "?format=json")) {
            dialog.setMessage("Checking details, please wait...");
        } else if (url.equalsIgnoreCase(uri_update) || url.equalsIgnoreCase(uri_shipping)) {
            dialog.setMessage("Updating information, Please wait");
        } else if (url.equalsIgnoreCase(uri_themes)) {
            dialog.setMessage("Getting themes, Please wait");
        } else if (url.equalsIgnoreCase(uri_themes_put)) {
            dialog.setMessage("Updating theme, Please wait");
        } else if (url.equalsIgnoreCase(uri_lookup_domain)) {
            dialog.setMessage("Checking availability, please wait");
        } else if (url.equalsIgnoreCase(uri_reserve_domain)) {
            dialog.setMessage("Requesting domain reservation, please wait");
        }

        dialog.show();
    }

    @Override
    public void processResponse(HttpResponse response, String url) {

        try {

            InputStream inputStream = response.getEntity().getContent();
            String responseString = Utils
                    .convertInputStreamToString(inputStream);
            Log.e("response", responseString);

            if (response.getStatusLine().getStatusCode() == 200 ||
                    response.getStatusLine().getStatusCode() == 201 || response.getStatusLine().getStatusCode() == 202) {
                JSONObject jsonResponse = new JSONObject(responseString);
                if (url.equalsIgnoreCase(uri_update) || url.equalsIgnoreCase(uri_update + "?format=json")) {
                    store_name = jsonResponse.getString("shop_name");
                    store_info_contact = jsonResponse.getString("shop_contact_info");
                    store_info_address = jsonResponse.getString("shop_address");
                    //store_info_domain_status = jsonResponse.getString("domain_status");
                    //store_info_domain = jsonResponse.getString("domain_reserved");
                    theme_name = jsonResponse.getString("theme");
                    if (!jsonResponse.getString("aboutus_image").equalsIgnoreCase("") && !jsonResponse.getString("aboutus_image").equalsIgnoreCase("null")) {
                        Calendar calendar = Calendar.getInstance();
                        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
                        store_image = Constants.base_uri1 + jsonResponse.getString("aboutus_image") + "?time=" + URLEncoder.encode(currentTimestamp.toString(), "UTF-8");
                    } else {
                        store_image = "null";
                    }
                    image = store_image;
                } else if (url.equalsIgnoreCase(uri_themes)) {
                    select_theme_pressed = true;
                    JSONArray jarray = jsonResponse.getJSONArray("templates");
                    url_maps = new HashMap<String, String>();
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);
                        if (object.optString("image").indexOf("//") == 0) {
                            url_maps.put(object.optString("template"), "http:" + object.optString("image"));
                        } else {
                            url_maps.put(object.optString("template"), Constants.base_uri1 + object.optString("image"));
                        }
                    }
                } else if (url.equalsIgnoreCase(uri_lookup_domain)) {
                    JSONArray jarrayobj = jsonResponse.getJSONArray("objects");
                    JSONObject jObjobj = jarrayobj.getJSONObject(0);
                    JSONObject jObjResults = jObjobj.getJSONObject("results");
                    JSONArray jarray = jObjResults.getJSONArray("suggestion");
                    domain_suggestions = new ArrayList<String>();
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);
                        if (object.getString("status").equalsIgnoreCase("available")) {
                            domain_available = true;
                            if (object.getString("domain").contains(check_domain))
                                domain_suggestions.add(0, object.getString("domain"));
                            else
                                domain_suggestions.add(object.getString("domain"));
                        }
                    }
                }
            } else if (url.equalsIgnoreCase(uri_lookup_domain) && response.getStatusLine().getStatusCode() == 400) {
                JSONObject jobject = new JSONObject(responseString);
                String msg = "";
                for (int i = 0; i < jobject.names().length(); i++) {

                    if (!jobject.names().getString(i).equalsIgnoreCase("status") && !jobject.names().getString(i).equalsIgnoreCase("errors")) {
                        String temp = jobject.getString(jobject.names().getString(i))
                                .toString();
                        msg += jobject.names().getString(i) + " : " + temp.substring(2, temp.length() - 2) + "\r\n";
                    }

                }
                domain_available = false;
                domain_error = msg;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public HttpPut preparePutData(String url, HttpPut httpPost) {
        if (url.equalsIgnoreCase(uri_update)) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            Boolean image_exists = true;
            builder.addPart("shop_name", new StringBody(name.getText().toString(), ContentType.TEXT_PLAIN));
            builder.addPart("shop_address", new StringBody(address.getText().toString(), ContentType.TEXT_PLAIN));
            builder.addPart("shop_contact_info", new StringBody(contact.getText().toString(), ContentType.TEXT_PLAIN));

            if (mCurrentLocation != null) {
                builder.addPart("latitude", new StringBody(mCurrentLocation.getLatitude() + "", ContentType.TEXT_PLAIN));
                builder.addPart("longitude", new StringBody(mCurrentLocation.getLongitude() + "", ContentType.TEXT_PLAIN));
                builder.addPart("location_accuracy", new StringBody(mCurrentLocation.getAccuracy() + "", ContentType.TEXT_PLAIN));
            }
            if (image.equalsIgnoreCase("")
                    || image.equalsIgnoreCase("null")) {
                builder.addPart("aboutus_image", new StringBody("", ContentType.TEXT_PLAIN));
            } else if (!image.contains(Constants.base_uri)) {
                File imFile = new File(image);
                if (!imFile.exists()) {
                    image_exists = false;
                }
                FileBody imBody = new FileBody(imFile);
                builder.addPart("aboutus_image", imBody);
            }
            // httpPost.addHeader("Content-Length", "0");

            HttpEntity http = builder.build();
            http.isChunked();
            httpPost.setEntity(http);
        }

        //  builder.addPart("prod_json", new StringBody(jobject.toString(),
        //        ContentType.TEXT_PLAIN));


        return httpPost;

    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {

        if (url.equalsIgnoreCase(uri_update)) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            Boolean image_exists = true;
            builder.addPart("shop_name", new StringBody(name.getText().toString(), ContentType.TEXT_PLAIN));
            builder.addPart("shop_address", new StringBody(address.getText().toString(), ContentType.TEXT_PLAIN));
            builder.addPart("shop_contact_info", new StringBody(contact.getText().toString(), ContentType.TEXT_PLAIN));

            if (theme_name != null && !theme_name.equalsIgnoreCase(""))
                builder.addPart("theme", new StringBody(theme_name, ContentType.TEXT_PLAIN));
            if (mCurrentLocation != null) {
                builder.addPart("latitude", new StringBody(mCurrentLocation.getLatitude() + "", ContentType.TEXT_PLAIN));
                builder.addPart("longitude", new StringBody(mCurrentLocation.getLongitude() + "", ContentType.TEXT_PLAIN));
                builder.addPart("location_accuracy", new StringBody(mCurrentLocation.getAccuracy() + "", ContentType.TEXT_PLAIN));
            }
            if (image.equalsIgnoreCase("")
                    || image.equalsIgnoreCase("null")) {
                builder.addPart("aboutus_image", new StringBody("", ContentType.TEXT_PLAIN));
            } else if (!image.contains(Constants.base_uri)) {
                File imFile = new File(image);
                if (!imFile.exists()) {
                    image_exists = false;
                }
                FileBody imBody = new FileBody(imFile);
                builder.addPart("aboutus_image", imBody);
            }
            // httpPost.addHeader("Content-Length", "0");

            HttpEntity http = builder.build();
            http.isChunked();
            httpPost.setEntity(http);
        } else if (url.equalsIgnoreCase(uri_lookup_domain)) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();


            try {
                nameValuePairs.add(new BasicNameValuePair("query", URLEncoder.encode(check_domain, "UTF-8")));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (url.equalsIgnoreCase(uri_reserve_domain)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("domain", check_domain);
                jsonObject.put("userid", userid);


                StringEntity en = new StringEntity(jsonObject.toString());
                en.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(en);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return httpPost;
    }


    /**
     * get values from sharedprefrences and put those into edit text and draws image
     */

    private void setvalues() {
        if (store_name.equalsIgnoreCase("null")) {
            name.setText("");
        } else {
            name.setText(store_name);
        }

        if (theme_name.equalsIgnoreCase("null")) {
            theme.setText("");
        } else {
            theme.setText(theme_name);
        }

        if (store_info_contact.equalsIgnoreCase("null")) {
            contact.setText("");
        } else {
            contact.setText(store_info_contact);
        }
        if (store_info_address.equalsIgnoreCase("null")) {
            address.setText("");
        } else {
            address.setText(store_info_address);
        }
        Utils.nullcase(store_logo, store_image, this);
        if (image.equalsIgnoreCase("") || image.equalsIgnoreCase("null") || image.contains(Constants.base_uri)) {
            rotate.setVisibility(View.INVISIBLE);
        }
        //new GetRequest(this, uri_shipping + "?format=json", context);
    }

    public void setvaluesShipping() {
        if (store_minimum.equalsIgnoreCase("null")) {
            minimum.setText("");
        } else {
            minimum.setText(store_minimum);
        }
        if (store_shipping.equalsIgnoreCase("null")) {
            shipping.setText("");
        } else {
            shipping.setText(store_shipping);
        }
    }

    @Override
    public void clickedRemoveImg() {
        image = "null";
        store_logo.setImageResource(R.drawable.icon_no_image);
        rotate.setVisibility(View.INVISIBLE);
    }

    private boolean changed() {
//        if (address.getText().toString().equalsIgnoreCase(store_info_address)
//                && contact.getText().toString().equalsIgnoreCase(store_info_contact)
//                && image.equalsIgnoreCase(store_image)
//                && name.getText().toString().equalsIgnoreCase(store_name)) {
//            Utils.showToast("Please update any information", context);
//            return false;
//        }
        return true;
    }

    private boolean changedShipping() {

        if (minimum.getText().toString().equalsIgnoreCase(store_minimum)
                && shipping.getText().toString().equalsIgnoreCase(store_shipping)) {
            Utils.showToast("Please update any information", context);
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    public void setThemesLayout() {
        mDemoSlider.setVisibility(View.VISIBLE);
        mDemoSlider.removeAllSliders();
        select_theme.setText("Select");
        int counter = 0;
        for (String name : url_maps.keySet()) {
            if (counter++ > 2)
                break;

            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name))
                    .imagePlaceHolder(R.drawable.icon_no_image)
                    .setScaleType(BaseSliderView.ScaleType.CenterInside)
                    .setOnSliderClickListener(this);
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);


            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOutSlide);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(900000000);
        mDemoSlider.addOnPageChangeListener(this);

    }

    private void setDomainLayout(String domain) {
        cancel_domain.setVisibility(View.GONE);
        err_domain.setVisibility(View.GONE);
        // domain_name.setVisibility(View.VISIBLE);
        edit_domain.setVisibility(View.GONE);
//        if(domain.equalsIgnoreCase("null")){
//            domain_name.setText("No domain reserved");
//        } else {
//            domain_name.setText(domain);
//        }

        if (store_info_domain_status.equalsIgnoreCase("reserved")
                || store_info_domain_status.equalsIgnoreCase("null")
                || store_info_domain_status.equalsIgnoreCase("")) {
            select_domain.setText("Change");
            select_domain.setVisibility(View.VISIBLE);
        } else {
            select_domain.setVisibility(View.GONE);
        }

        if (store_info_domain_status.equalsIgnoreCase("null")) {
            domain_name.setText("Currently not Reserved");
        } else {
            domain_name.setText("Currently " + store_info_domain_status + " : " + domain);
        }

        domain_list.setVisibility(View.GONE);
    }


    private void saveImgAfterRotate() {
        if (!image.equalsIgnoreCase("") && !image.equalsIgnoreCase("null") && !image.contains(Constants.base_uri)) {
            rotate.setClickable(false);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image, bmOptions);
            String path2 = Utils.getOutputMediaFileUri(
                    getApplicationContext(), image.substring(image.lastIndexOf("/"))).toString();
            bitmap = ImageConverter.rotate(bitmap);
            Utils.saveImg(path2, 100, bitmap, false);
            image = path2;
            store_logo.setImageBitmap(bitmap);
            rotate.setClickable(true);
        }
    }

    private View getView(final int position) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.domain_suggestions_item, null);
        TextView textView = (TextView) rowView.findViewById(R.id.domain_suggestion);
        TextView book = (TextView) rowView.findViewById(R.id.domain_reserve);
        textView.setText(domain_suggestions.get(position));

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_domain = domain_suggestions.get(position);
                new PostRequest(EditInfo.this, uri_reserve_domain, EditInfo.this);
            }
        });

        return rowView;
    }

    private void addToList() {
        domain_list.removeAllViews();
        for (int i = 0; i < domain_suggestions.size(); i++) {
            domain_list.addView(getView(i));
        }
    }


    @Override
    public void enterLocation(Location loc) {

        location.setText("accuracy : " + loc.getAccuracy() + "\nlat : " + loc.getLatitude() + "\nlong : " + loc.getLongitude());
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    public void onConnected(Bundle bundle) {

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation == null) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        } else if (mCurrentLocation.getAccuracy() > location.getAccuracy()) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    protected void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        mRequestingLocationUpdates = true;
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                //setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            location.setText("accuracy : " + mCurrentLocation.getAccuracy() + "\nlatitude : " + mCurrentLocation.getLatitude() + "\nlongitude : " + mCurrentLocation.getLongitude());
        }
    }


    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        }
        super.onResume();
    }
}
