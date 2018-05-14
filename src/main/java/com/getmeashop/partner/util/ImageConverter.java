package com.getmeashop.partner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class ImageConverter {

    public ImageConverter() {
        // TODO Auto-generated constructor stub

    }



    public static Bitmap rotate(Bitmap original){
        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(original,original.getWidth(),original.getHeight(),true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

        return rotatedBitmap;
    }

    /**
     * calculates width on basis of pixel density
     */
    public static int calwidth(Context context) {
        int pixel = 100;
        float a = context.getResources().getDisplayMetrics().density;

        if (a < 1.0)
            pixel = 75;

        else if (a < 1.5)
            pixel = 100;

        else if (a < 2.0)
            pixel = 150;
        else
            pixel = 200;

        return pixel;
    }





    /**
     * calculates width to maintain aspect ratio of image
     *
     * @param width of image
     * @param orgw  original width of image
     * @param orgh  original height of image
     */
    public static int calheight(int width, float orgw, float orgh) {
        // Log.d("check", "check"+orgh+" "+orgw+" "+width+" "+(((int) orgh
        // /orgw) * width));
        return ((int) ((orgw / orgh) * width));
    }

    /**
     * calculates factor of reducing resolution checks current resolution return
     * multiple of 2
     *
     * @param currw     current width
     * @param currh     current height
     * @param reqWidth  required width
     * @param reqHeight required height
     */
    public static int calculateInSampleSize(int currw, int currh, int reqWidth,
                                            int reqHeight) {
        // Raw height and width of image
        final int height = currh;
        final int width = currw;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        if (currw > 1400 && inSampleSize < 2)
            inSampleSize = 2;
        if (currw > 2800 && inSampleSize < 4)
            inSampleSize = 4;

        return inSampleSize;
    }

    /**
     * returns decoded bitmap
     *
     * @param path      path of image
     * @param currw     current width
     * @param currh     current height
     * @param reqWidth  required width
     * @param reqHeight required height
     */
    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight, int currw, int currh) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(currw, currh, reqWidth,
                reqHeight);
        // options.inPurgeable = true;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * stores given file to gmas directory
     *
     * @param path    path of image
     * @param name    to be set
     * @param context
     */
    public static String setFileName(String name, String path, Context c) {

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
        String currentFileName = path.substring(path.lastIndexOf("/") + 1,
                path.length());
        // currentFileName = currentFileName.substring(1);
        Log.i("file previous name", currentFileName);
        Log.i("file current name", name + currentFileName);
        Log.i("path file name", path.substring(0, path.lastIndexOf("/") - 1));

        File from = new File(filesDir, currentFileName);
        File to = new File(filesDir, name + currentFileName.substring(currentFileName.lastIndexOf("_"), currentFileName.length()));
        Boolean ab = from.renameTo(to);
        Log.i("current", "true" + ab);
        if (ab)
            return to.getAbsolutePath();
        else
            return "false";

    }

    /**
     * creates directory if doesn't exists
     *
     * @param path path of directory
     */
    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("directory :: ", "Problem creating Image folder");
                ret = false;
            }
        }
        return ret;
    }

}
