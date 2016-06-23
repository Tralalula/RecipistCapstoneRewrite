// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/NewPostUploadTaskFragment.java

package com.example.tobias.recipist.task;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.tobias.recipist.callback.TaskCallback;
import com.example.tobias.recipist.fragment.CreateRecipeUploadTaskFragment;
import com.google.firebase.crash.FirebaseCrash;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tobias on 23-06-2016.
 */
public class LoadResizedBitmapTask extends AsyncTask<Uri, Void, Bitmap> {
    public static String TAG = LoadResizedBitmapTask.class.getSimpleName();

    private Context mContext;
    private TaskCallback mCallback;
    private int mMaxDimension;

    public LoadResizedBitmapTask(Context context, TaskCallback callback, int maxDimension) {
        mContext = context;
        mCallback = callback;
        mMaxDimension = maxDimension;
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        Uri uri = params[0];
        if (uri != null) {
            Bitmap bitmap = null;
            try {
                bitmap = decodeSampleBitmapFromUri(uri, mMaxDimension, mMaxDimension);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Can't find file to resize: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Error occured during resize: " + e.getMessage());
                FirebaseCrash.report(e);
            }

            return bitmap;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mCallback.onBitmapResized(bitmap, mMaxDimension);
    }

    public Bitmap decodeSampleBitmapFromUri(Uri fileUri, int reqWidth, int reqHeight) throws IOException {
        InputStream stream = new BufferedInputStream(mContext.getContentResolver().openInputStream(fileUri));
        stream.mark(stream.available());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        stream.reset();

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        BitmapFactory.decodeStream(stream, null, options);
        stream.reset();

        return BitmapFactory.decodeStream(stream, null, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
