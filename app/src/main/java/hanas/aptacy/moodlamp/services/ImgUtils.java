package hanas.aptacy.moodlamp.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hanas.aptacy.moodlamp.R;

public class ImgUtils {
//    //Save the file to the specified path
//    public static boolean saveImageToGallery(Context context, Bitmap bmp) {
//        // First save the picture
//        File path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
//        String fileName = System.currentTimeMillis() + ".png";
//        File file = new File(path, fileName);
//        try {
//            path.mkdirs();
//
//            FileOutputStream fos = new FileOutputStream(file);
//            //Compress and save pictures by io stream
//            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            fos.flush();
//            fos.close();
//
//            //Insert files into the system Gallery
//            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
//
//            //Update the database by sending broadcast notifications after saving pictures
//            Uri uri = Uri.fromFile(file);
//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
//            if (isSuccess) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    public static void saveBitmap(@NonNull final Context context, @NonNull final Bitmap bitmap) {
        final String relativeLocation = Environment.DIRECTORY_PICTURES;

        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "tmp_wallpaper");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

        final ContentResolver resolver = context.getContentResolver();

        OutputStream stream;
        Uri uri = null;

        try
        {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;//MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, contentValues);

            if (uri == null) {
                throw new IOException("Failed to create new MediaStore record.");
            }

            stream = resolver.openOutputStream(uri);

            if (stream == null) {
                throw new IOException("Failed to get output stream.");
            }

            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw new IOException("Failed to save bitmap.");
            }
            if (stream != null) {
                stream.close();
            }
        }
        catch (IOException e)
        {
            if (uri != null)
            {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }
        }
    }


    public static void createExternalStoragePublicPicture(Context context, Bitmap bmp) {
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");

        try {
            // Make sure the Pictures directory exists.
            path.mkdirs();

            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            FileOutputStream fos = new FileOutputStream(file);
            //Compress and save pictures by io stream
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(context,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }
}