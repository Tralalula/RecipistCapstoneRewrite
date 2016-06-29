// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/NewPostUploadTaskFragment.java

package com.example.tobias.recipist.callback;

import android.graphics.Bitmap;

/**
 * Created by Tobias on 23-06-2016.
 */
public interface TaskCallback {
    void onBitmapResized(Bitmap resizedBitmap, int maxDimension);
    void onRecipeUploaded(String error, String firebaseKey);
}
