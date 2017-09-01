package com.getmeashop.realestate.partner;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.getmeashop.realestate.partner.util.Constants;
import com.getmeashop.realestate.partner.util.ImageConverter;
import com.getmeashop.realestate.partner.util.Interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {


    private final static int CAMERA_REQUEST = 1888;
    private final static int GALLERY_REQUEST = 1889;
    private final static int REMOVE_IMG = 1889;
    private static Toast toast;

    /**
     * coverts inputstream to String
     * <p/>
     * returns String converted
     *
     * @param inputStream to be converted
     */
    public static String convertInputStreamToString(InputStream inputStream)
            throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
            // System.out.println("---->"+line);
        }

        inputStream.close();
        return result;

    }

    public static boolean validate(EditText... testObj) {
        for (int i = 0; i < testObj.length; i++) {
            if (testObj[i].getText().toString().trim().equals(""))
                return false;
        }
        return true;
    }

    /**
     * check internet connectivity
     * <p/>
     * shows toast msg if no connection and returns false
     */
    public static boolean isConnectingToInternet(Context c) {
        ConnectivityManager connectivity = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (isConnectionFast(info.getType(), info.getSubtype())) {
                    return true;
                } else {
                    Toast.makeText(c, "Connection Too Slow.Try Again with faster connection", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        Toast.makeText(c, "No internet connection available",
                Toast.LENGTH_SHORT).show();
        return false;
    }


    /**
     * check internet connectivity
     * <p/>
     * if no connection and returns false
     */
    public static boolean isConnectingToInternetWithoutToast(Context c) {
        ConnectivityManager connectivity = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (isConnectionFast(info.getType(), info.getSubtype())) {
                    return true;
                } else {
                   // Toast.makeText(c, "Connection Too Slow.Try Again with faster connection", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        //Toast.makeText(c, "No internet connection available",
          //      Toast.LENGTH_SHORT).show();
        return false;
    }


    /**
     * creates file uri from given name under gmas directory
     * <p/>
     * returns file pointed to that file uri
     *
     * @param filename
     * @param
     * @param c
     */
    public static File getOutputMediaFileUri(Context c, String filename) {
        String state = Environment.getExternalStorageState();
        File filesDir = null;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            try {
                filesDir = Environment.getExternalStorageDirectory();
                File gmasDir = new File(filesDir + "/.MBPartner");
                if (!gmasDir.exists()) {
                    gmasDir.mkdir();
                }
                filesDir = gmasDir;
            } catch (Exception e) {
                filesDir = c.getExternalFilesDir(null);
            }
        } else {
            filesDir = c.getFilesDir();
        }
        File retu = new File(filesDir.getPath() + File.separator + filename);
        if (retu.exists()) {
            retu.delete();
            // retu= new File(filesDir.getPath() + File.separator + filename +
            // ".jpg");
        }
        return retu;
    }


    /**
     * compress image and save image
     *
     * @param path    to image
     * @param quality of image to be saved
     * @scaled image bitmap to image to be stored
     */
    public static void saveImg(String path, int quality, Bitmap scaledImage, boolean compress) {
        FileOutputStream fos = null;
        try {
            File check = new File(path);
            if (check.exists()) {
                check.delete();
            }
            fos = new FileOutputStream(path);
            if(compress)
                scaledImage.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            else
                scaledImage.compress(Bitmap.CompressFormat.PNG, quality, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    /**
     * clear cache
     */
    public static void clrCache(String image, String prevImg, Context context) {
        if (!image.equalsIgnoreCase(prevImg)) {
            Log.d("tag", "cache cleared for" + prevImg);
            UrlImageViewHelper.cleanup(context);

        }
    }

    public static int quality(int height) {
        if (height > 3000)
            return 60;
        if (3000 >= height && height > 2000)
            return 70;
        if (2000 >= height && height > 1000)
            return 75;
        if (1000 >= height && height > 500)
            return 85;
        if (500 >= height)
            return 95;
        return 75;
    }

    /**
     * invoke intent to chhose image
     * <p/>
     * shows dailogue to chhose from gallery or use camer and creates intent
     * accordingly
     *
     * @param fragment
     * @param activity
     * @param fileUri  to be passed to camera as results back image to this uri
     */
    public static void choose_image_from(final Context context,
                                         final File fileUri, final Activity activity,
                                         final Fragment fragment, String image) {
        List<String> options = new ArrayList<String>();
        options.add("Take Photo");
        options.add("Choose from Library");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (image != null && !image.equalsIgnoreCase("null") && !image.equalsIgnoreCase("")) {
            options.add("Remove Photo");
            builder.setTitle("Add/Remove Photo!");
        } else {
            builder.setTitle("Add Photo!");
        }
        options.add("Cancel");

        final CharSequence[] items = options.toArray(new CharSequence[options.size()]);


        builder.setItems(items, new DialogInterface.OnClickListener() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {

                    try {
                        Intent cameraIntent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);

                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(fileUri));
                        if (fragment != null) {
                            fragment.startActivityForResult(cameraIntent,
                                    CAMERA_REQUEST);
                            //MainFragment.gall = true;
                        } else
                            activity.startActivityForResult(cameraIntent,
                                    CAMERA_REQUEST);
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(context,
                                "Unable to save image, insufficient memory.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (items[item].equals("Choose from Library")) {

                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

               /*     Intent pickIntent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent,
                            "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                            new Intent[] { pickIntent });
        */
                    // fragment.startActivityForResult(chooserIntent,
                    // GALLERY_REQUEST);

                    /*
                     * Intent i = new Intent( Intent.ACTION_PICK,
                     * android.provider
                     * .MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                     * i.setType("image/*");
                     */
                    if (fragment != null) {
                        // MainFragment.gall = true;
                        fragment.startActivityForResult(getIntent,
                                GALLERY_REQUEST);
                    } else
                        activity.startActivityForResult(getIntent,
                                GALLERY_REQUEST);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                } else if (items[item].equals("Remove Photo")) {
                    if (fragment != null) {
                        // MainFragment.gall = true;
                        Interfaces.removeImg remove = (Interfaces.removeImg) fragment;
                        remove.clickedRemoveImg();
                    } else {
                        Interfaces.removeImg remove = (Interfaces.removeImg) activity;
                        remove.clickedRemoveImg();
                    }
                }
            }

        });
        builder.show();

    }

    /**
     * draws image basis on url
     *
     * @param imgv image to be set to this view
     * @param img  url of image
     */
    public static void nullcase(ImageView imgv, String img, Context context) {
        Log.d("nullcase", "nullcase " + img);
        if (img == null || img.equals("null")) {
            img = "null";
            imgv.setImageResource(R.drawable.icon_no_image);
        } else if (!img.contains(Constants.base_uri)) {
            File imgFile = new File(img);
            if (imgFile.exists()) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);
                float height = bmOptions.outHeight;
                float width = bmOptions.outWidth;
                int reqw = ImageConverter
                        .calwidth(context);
                int reqh = ImageConverter
                        .calheight(reqw, height, width);
                Bitmap scaledImage = ImageConverter
                        .decodeSampledBitmapFromResource(
                                imgFile.getAbsolutePath(), reqw, reqh,
                                (int) width, (int) height);
                imgv.setImageBitmap(scaledImage);
            }
        } else {
            UrlImageViewHelper.setUrlDrawable(imgv, img);

        }
    }

    public static void showToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

    }

    /**
     * <strong>public void showAToast (String st)</strong></br>
     * this little method displays a toast on the screen.</br>
     * it checks if a toast is currently visible</br>
     * if so </br>
     * ... it "sets" the new text</br>
     * else</br>
     * ... it "makes" the new text</br>
     * and "shows" either or
     *
     * @param st the string to be toasted
     */

    public static void showToast(Context context, String st) { //"Toast toast" is declared in the class
        try {
            toast = Toast.makeText(context, st, Toast.LENGTH_SHORT);
            // String message = ((EditText) ((LinearLayout)toast.getView()).getChildAt(0)).getText().toString();
            if (!toast.getView().isShown()) {    // true if visible
                toast.show();
            }

        } catch (Exception e) {         // invisible if exception
            toast = Toast.makeText(context, st, Toast.LENGTH_LONG);
            toast.show();
        }
        //finally display it
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(19)
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                // not accepting online images
                //   return uri.getLastPathSegment();
                return "error_image_url";
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "error_image_url";
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * Check device's network connectivity and speed
     * @author emil http://stackoverflow.com/users/220710/emil
     *
     */

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
        /*
         * Above API level 7, make sure to set android:targetSdkVersion
         * to appropriate level to use these
         */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static void showStatusCodeToast(int statusCode, Context context) {
        switch (statusCode) {
            case 70:
            case 80:
            case 90:
                Utils.showToast("Connection TimeOut. Try Again.", context);
                break;
            case 404:
                Utils.showToast("Connection TimeOut. Try Again.", context);
                break;
        }
    }

}
