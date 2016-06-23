// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/NewPostUploadTaskFragment.java

package com.example.tobias.recipist.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.callback.TaskCallback;
import com.example.tobias.recipist.util.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Tobias on 23-06-2016.
 */
public class UploadRecipeTask extends AsyncTask<Void, Void, Void> {
    public static String TAG = UploadRecipeTask.class.getSimpleName();

    private Context mContext;
    private TaskCallback mCallback;

    private WeakReference<Bitmap> mBitmapReference;
    private WeakReference<Bitmap> mThumbnailReference;

    private String mFileName;
    private String mBitmapPath;
    private String mThumbnailPath;

    public UploadRecipeTask(Context context, TaskCallback callback, Bitmap bitmap, String inBitmapPath, Bitmap thumbnail, String inThumbnailPath, String inFileName) {
        mContext = context;
        mCallback = callback;

        mBitmapReference = new WeakReference<Bitmap>(bitmap);
        mThumbnailReference = new WeakReference<Bitmap>(thumbnail);

        mFileName = inFileName;
        mBitmapPath = inBitmapPath;
        mThumbnailPath = inThumbnailPath;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Bitmap fullSize = mBitmapReference.get();
        final Bitmap thumbnail = mThumbnailReference.get();
        if (fullSize == null || thumbnail == null) return null;

        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        StorageReference photoRef = storageRef.getReferenceFromUrl("gs://" + mContext.getString(R.string.google_storage_bucket));

        Long timestamp = System.currentTimeMillis();
        final StorageReference fullSizeRef = photoRef.child(FirebaseUtil.getCurrentUserId()).child("full").child(timestamp.toString());
        final StorageReference thumbnailRef = photoRef.child(FirebaseUtil.getCurrentUserId()).child("thumb").child(timestamp.toString());

        Log.d(TAG, "doInBackground:fullSizeRef: " + fullSizeRef);
        Log.d(TAG, "doInBackground:thumbnailRef: " + thumbnailRef);

        ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
        fullSize.compress(Bitmap.CompressFormat.JPEG, 90, fullSizeStream);
        byte[] bytes = fullSizeStream.toByteArray();
        fullSizeRef.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Uri fullSizeUrl = taskSnapshot.getDownloadUrl();

                Log.d(TAG, "doInBackground:fullSizeRef:onSuccess:fullSizeUrl: " + fullSizeUrl.toString());

                ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, thumbnailStream);
                thumbnailRef.putBytes(thumbnailStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri thumbnailUrl = taskSnapshot.getDownloadUrl();

                        Log.d(TAG, "doInBackground:fullSizeRef:onSuccess:thumbnailRef:onSuccess:thumbnailUrl: " + thumbnailUrl.toString());
                        mCallback.onRecipeUploaded(null);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrash.logcat(Log.ERROR, TAG, "thumbnail: Failed to upload recipe to database.");
                        FirebaseCrash.report(e);
                        mCallback.onRecipeUploaded("Error uploading recipe");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "fullSize: Failed to upload recipe to database.");
                FirebaseCrash.report(e);
                mCallback.onRecipeUploaded("Error uploading recipe");
            }
        });

        return null;
    }

    @Override
    protected void onPreExecute() {

    }
}