package com.getmeashop.partner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.getmeashop.partner.database.Cheque;
import com.getmeashop.partner.util.Constants;
import com.getmeashop.partner.util.ImageConverter;
import com.getmeashop.partner.util.Interfaces;
import com.getmeashop.partner.util.PostRequest;
import com.getmeashop.partner.util.PutRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Calendar;


public class EditPayment extends AppCompatActivity implements Callbacks, Interfaces.PutCallbacks, Interfaces.removeImg {

    private final static int CAMERA_REQUEST = 1888;
    private final static int GALLERY_REQUEST = 1889;
    TextView start_date;
    Button update;
    File fileUri;
    int clicked;
    int pos;
    ProgressDialog dialog;
    Context context;
    String uri_update = "";
    String store_info_bank_name, store_info_cheque_number, prev_img, store_info_plan, store_info_amount, store_info_months, store_info_start_date;
    boolean store_info_is_deposit, add = false;
    Toolbar mtoolbar;
    private EditText bank_name, cheque_number, amount_paid, months;
    private Spinner plan;
    private CheckBox is_deposited;
    private ImageView store_logo;
    private SharedPreferences sp;
    private Calendar calendar;
    private int year, month, day;
    private String image, id;
    private SharedPreferences.Editor editor;
    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            showDate(arg1, arg2 + 1, arg3, start_date);
        }
    };


  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_payment);
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        update = (Button) findViewById(R.id.update_info);

        if (getIntent().hasExtra("user_id")) {
            add = true;
            store_info_is_deposit = false;
            store_info_bank_name = "";
            store_info_cheque_number = "";
            prev_img = image = "";
            store_info_amount = "";
            store_info_months = "";
            store_info_plan = "";
            store_info_start_date = "";
            uri_update = Constants.uri_pay_info;
            pos = -1;
            update.setText("Add");
        } else {

            store_info_is_deposit = false;
            if ((getIntent().getStringExtra("deposited")).equalsIgnoreCase("true")) {
                store_info_is_deposit = true;
            }

            store_info_bank_name = getIntent().getStringExtra("bank_name");
            store_info_cheque_number = getIntent().getStringExtra("cheque_num");
            prev_img = image = getIntent().getStringExtra("image");
            store_info_amount = getIntent().getStringExtra("amount_paid");
            store_info_months = getIntent().getStringExtra("months");
            store_info_plan = getIntent().getStringExtra("plan");
            id = getIntent().getStringExtra("id");
            uri_update = Constants.base_uri1 + getIntent().getStringExtra("r_uri");
            pos = getIntent().getIntExtra("position", 0);
            store_info_start_date = getIntent().getStringExtra("start_date");
        }

        sp = this.getSharedPreferences("Users", Context.MODE_PRIVATE);
        editor = sp.edit();

        context = this;
        is_deposited = (CheckBox) findViewById(R.id.is_deposited);
        cheque_number = (EditText) findViewById(R.id.cheque_number);
        bank_name = (EditText) findViewById(R.id.bank_name);
        store_logo = (ImageView) findViewById(R.id.store_logo);
        amount_paid = (EditText) findViewById(R.id.amount_number);
        start_date = (TextView) findViewById(R.id.start_date);
        months = (EditText) findViewById(R.id.months);
        plan = (Spinner) findViewById(R.id.plan);
        // image = sp.getString("store_info_store_logo", "null");
        // base_uri = Constants.base_uri;


        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(" Checking details Please wait...");
        dialog.setCancelable(false);

        setvalues();

        store_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileUri = Utils.getOutputMediaFileUri(context, "store_info_store_logo.jpg");
                Utils.choose_image_from(context, fileUri, (Activity) context, null, "null");
            }
        });

        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(1);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (plan.getSelectedItemPosition() == 0)
                    Utils.showToast("Choose any plan", getApplicationContext());
                else if (start_date.getText().toString().length() == 0) {
                    Utils.showToast("Please select a start date", getApplicationContext());
                } else if (months.getText().toString().length() == 0 || Integer.parseInt(months.getText().toString().trim()) > 20) {
                    Utils.showToast("Enter months value between than 20", getApplicationContext());
                } else if (amount_paid.getText().toString().trim().equalsIgnoreCase("")) {
                    Utils.showToast("Enter valid amount", context);
                } else if (cheque_number.getText().toString().trim().equalsIgnoreCase("")) {
                    Utils.showToast("Enter valid cheque number", context);
                } else if (bank_name.getText().toString().trim().equalsIgnoreCase("")) {
                    Utils.showToast("Enter valid bank name", context);
                } else if (!image.contains(Constants.base_uri) && (image.equalsIgnoreCase("null") || image.equalsIgnoreCase(""))) {
                    Utils.showToast("Please choose an image", context);
                } else {
                    if (changed())
                        if (add)
                            new PostRequest((Callbacks) EditPayment.this, uri_update, getApplicationContext());
                        else
                            new PutRequest((Interfaces.PutCallbacks) context, uri_update, context);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }

        return true
                ;
        // return super.onOptionsItemSelected(menuItem);
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
     * @param Path to of the image;
     * @param cam  to tell if camera activity was used
     */
    private int process_image(String path, int cam) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);

        float height = bmOptions.outHeight;
        float width = bmOptions.outWidth;

        int reqw = ImageConverter
                .calwidth(getApplicationContext());
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
                Utils.saveImg(path, 100, scaledImage, true);
                clicked = 1;
                image = path;
            } else {
                String path2 = Utils.getOutputMediaFileUri(
                        getApplicationContext(), path).toString();
                Utils.saveImg(path2, 100, scaledImage, true);
                image = path2;
            }

            store_logo.setImageBitmap(scaledImage);

            scaledImage = null;
            //cheque_number.setText(image);
        }
        return 0;
    }

    @Override
    public void postexecute(String url, int status) {
        if (status == 200 || status == 201) {
            if (url.equalsIgnoreCase(uri_update)) {
                if (add)
                    Utils.showToast("Successfully added", context);
                else
                    Utils.showToast("Successfully updated", context);
                finish();
            }
        } else {
            if (url.equalsIgnoreCase(uri_update)) {
                Utils.showToast("failed to update info, please try again later", context);
            }
        }
        if (dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void preexecute(String url) {
        if (url.equalsIgnoreCase(uri_update)) {
            if (add)
                dialog.setMessage("Updating information, Please wait");
            else
                dialog.setMessage("Updating information, Please wait");
        }
        dialog.show();
    }

    @Override
    public void processResponse(HttpResponse response, String url) {


        try {
            InputStream inputStream = response.getEntity().getContent();
            String responseString = Utils
                    .convertInputStreamToString(inputStream);
            JSONObject jsonResponse = new JSONObject(responseString);
            if (response.getStatusLine().getStatusCode() == 200 ||
                    response.getStatusLine().getStatusCode() == 201) {
                store_info_bank_name = jsonResponse.getString("bank_name");
                store_info_cheque_number = jsonResponse.getString("cheque_number");
                if (!jsonResponse.getString("cheque_image").equalsIgnoreCase("") || !jsonResponse.getString("cheque_image").equalsIgnoreCase("null")) {
                    Calendar calendar = Calendar.getInstance();
                    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
                    image = Constants.base_uri1 + jsonResponse.getString("cheque_image") + "?time=" + URLEncoder.encode(currentTimestamp.toString(), "UTF-8");
                } else {
                    image = "null";
                }
                String datewa = jsonResponse.getString("start_date");
                datewa = datewa.substring(0, datewa.indexOf("T"));
                Cheque cheque = new Cheque();
                cheque.setR_uri(jsonResponse.getString("resource_uri"));
                cheque.setImage(image);
                cheque.setNumber(store_info_cheque_number);
                cheque.setBank_name(store_info_bank_name);
                cheque.setAmount(jsonResponse.getString("amount_paid"));
                cheque.setMonths(jsonResponse.getString("months"));
                cheque.setPlan(jsonResponse.getString("plan"));
                cheque.setDeposited(jsonResponse.getString("is_cheque_deposited"));
                cheque.setId(jsonResponse.getString("id"));
                cheque.setStartDate(datewa);

                UserChequeList.update(cheque, pos);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public HttpPut preparePutData(String url, HttpPut httpPost) {


        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        String temp;

        Boolean image_exists = true;
        //builder.addPart("id", new StringBody(id,ContentType.TEXT_PLAIN));
        //builder.addPart("user_id", new StringBody(getIntent().getStringExtra("user_id"), ContentType.TEXT_PLAIN));
        builder.addPart("cheque_number", new StringBody(cheque_number.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("bank_name", new StringBody(bank_name.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("amount_paid", new StringBody(amount_paid.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("months", new StringBody(months.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("plan", new StringBody("" + plan.getSelectedItemPosition(), ContentType.TEXT_PLAIN));
        builder.addPart("start_date", new StringBody(start_date.getText().toString(), ContentType.TEXT_PLAIN));
        if (is_deposited.isChecked())
            builder.addPart("is_cheque_deposited", new StringBody("on", ContentType.TEXT_PLAIN));
        else
            builder.addPart("is_cheque_deposited", new StringBody("", ContentType.TEXT_PLAIN));
        if (image.equalsIgnoreCase("")
                || image.equalsIgnoreCase("null")) {
            builder.addPart("cheque_image", new StringBody("off", ContentType.TEXT_PLAIN));
        } else if (!image.contains(Constants.base_uri)) {
            File imFile = new File(image);
            if (!imFile.exists()) {
                image_exists = false;
            }
            FileBody imBody = new FileBody(imFile);
            builder.addPart("cheque_image", imBody);
        }

        //  builder.addPart("prod_json", new StringBody(jobject.toString(),
        //        ContentType.TEXT_PLAIN));
        //httpPost.setHeader("Content-Type", "multipart/form-data");
        httpPost.setEntity(builder.build());
        return httpPost;

    }

    @Override
    public HttpPost preparePostData(String url, HttpPost httpPost) {

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        Boolean image_exists = true;
        builder.addPart("user_id", new StringBody(getIntent().getStringExtra("user_id"), ContentType.TEXT_PLAIN));
        builder.addPart("cheque_number", new StringBody(cheque_number.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("bank_name", new StringBody(bank_name.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("amount_paid", new StringBody(amount_paid.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("months", new StringBody(months.getText().toString(), ContentType.TEXT_PLAIN));
        builder.addPart("plan", new StringBody("" + plan.getSelectedItemPosition(), ContentType.TEXT_PLAIN));
        builder.addPart("start_date", new StringBody(start_date.getText().toString(), ContentType.TEXT_PLAIN));

        if (is_deposited.isChecked())
            builder.addPart("is_cheque_deposited", new StringBody("on", ContentType.TEXT_PLAIN));
        else
            builder.addPart("is_cheque_deposited", new StringBody("", ContentType.TEXT_PLAIN));

        if (image.equalsIgnoreCase("")
                || image.equalsIgnoreCase("null")) {
            builder.addPart("cheque_image", new StringBody("", ContentType.TEXT_PLAIN));
        } else if (!image.contains(Constants.base_uri)) {
            File imFile = new File(image);
            if (!imFile.exists()) {
                image_exists = false;
            }
            FileBody imBody = new FileBody(imFile);
            builder.addPart("cheque_image", imBody);
        }
        //  builder.addPart("prod_json", new StringBody(jobject.toString(),
        //        ContentType.TEXT_PLAIN));

        //httpPost.setHeader("Content-Type", "multipart/form-data");
        httpPost.setEntity(builder.build());
        return httpPost;
    }

    /**
     * get values from sharedprefrences and put those into edit text and draws image
     */

    private void setvalues() {
        if (store_info_is_deposit) {
            is_deposited.setChecked(true);
        } else {
            is_deposited.setChecked(false);
        }
        if (store_info_start_date.equalsIgnoreCase("null")) {
            start_date.setText("");
        } else {
            start_date.setText(store_info_start_date);
        }
        if (store_info_bank_name.equalsIgnoreCase("null")) {
            bank_name.setText("");
        } else {
            bank_name.setText(store_info_bank_name);
        }
        if (store_info_cheque_number.equalsIgnoreCase("null")) {
            cheque_number.setText("");
        } else {
            cheque_number.setText(store_info_cheque_number);
        }
        if (store_info_months.equalsIgnoreCase("null")) {
            months.setText("");
        } else {
            months.setText(store_info_months);
        }
        if (store_info_amount.equalsIgnoreCase("null")) {
            amount_paid.setText("");
        } else {
            amount_paid.setText(store_info_amount);
        }

        if (store_info_plan.equalsIgnoreCase("null") ||
                store_info_plan.equalsIgnoreCase("")) {
            plan.setSelection(1);
        } else {
            plan.setSelection(1);
        }
        Utils.nullcase(store_logo, image, this);
    }

    @Override
    public void clickedRemoveImg() {
        image = "null";
        store_logo.setImageResource(R.drawable.icon_no_image);
    }

    private boolean changed() {

        if (start_date.getText().toString().equalsIgnoreCase(store_info_start_date)
                && cheque_number.getText().toString().equalsIgnoreCase(store_info_cheque_number)
                && bank_name.getText().toString().equalsIgnoreCase(store_info_bank_name)
                && is_deposited.isChecked() == store_info_is_deposit
                && image.equalsIgnoreCase(prev_img)
                && store_info_plan.equalsIgnoreCase(plan.getSelectedItemPosition() + "")
                && store_info_months.equalsIgnoreCase(months.getText().toString())
                && store_info_amount.equalsIgnoreCase(amount_paid.getText().toString())) {
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

    private void showDate(int year, int month, int day, TextView tv) {
        tv.setText(new StringBuilder().append(year).append("-")
                .append(String.format("%02d", month)).append("-").append(String.format("%02d", day)));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 1)
            return new DatePickerDialog(this, myDateListener, year, month, day);
        return null;
    }
}
