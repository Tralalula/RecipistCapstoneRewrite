// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/NewPostUploadTaskFragment.java

package com.example.tobias.recipist.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.adapter.LocalRecipesAdapter;
import com.example.tobias.recipist.callback.TaskCallback;
import com.example.tobias.recipist.data.RecipistDbHandler;
import com.example.tobias.recipist.fragment.LocalRecipesFragment;
import com.example.tobias.recipist.model.Ingredients;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.model.Author;
import com.example.tobias.recipist.model.Steps;
import com.example.tobias.recipist.util.FirebaseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    private String mFullSizeUrl;
    private String mFullSizeRef;
    private String mThumbnailUrl;
    private String mThumbnailRef;

    private String mTitle;
    private int mPublish;
    private String mTime;
    private String mServings;

    private ArrayList<Ingredients.Ingredient> mIngredients;
    private ArrayList<Steps.Step> mSteps;

    private boolean mEditing;
    private String mRecipeFirebaseKeyForEditing;

    private boolean mNewImage;

    public UploadRecipeTask(Context context, TaskCallback callback,
                            String fullSizeUrl, String fullSizeRef,
                            String thumbnailUrl, String thumbnailRef,
                            String title, int publish, String time, String servings,
                            ArrayList<Ingredients.Ingredient> ingredients,
                            ArrayList<Steps.Step> steps,
                            boolean editing, String firebaseKeyForEditing) {
        mContext = context;
        mCallback = callback;

        mFullSizeUrl = fullSizeUrl;
        mFullSizeRef = fullSizeRef;
        mThumbnailUrl = thumbnailUrl;
        mThumbnailRef = thumbnailRef;

        mTitle = title;
        mPublish = publish;
        mTime = time;
        mServings = servings;

        mIngredients = ingredients;
        mSteps = steps;

        mEditing = editing;
        mRecipeFirebaseKeyForEditing = firebaseKeyForEditing;

        mNewImage = false;
    }

    public UploadRecipeTask(Context context, TaskCallback callback,
                            Bitmap bitmap, String inBitmapPath,
                            Bitmap thumbnail, String inThumbnailPath, String inFileName,
                            String title, int publish, String time, String servings,
                            ArrayList<Ingredients.Ingredient> ingredients,
                            ArrayList<Steps.Step> steps,
                            boolean editing, String firebaseKeyForEditing) {
        mContext = context;
        mCallback = callback;

        mBitmapReference = new WeakReference<>(bitmap);
        mThumbnailReference = new WeakReference<>(thumbnail);

        mFileName = inFileName;
        mBitmapPath = inBitmapPath;
        mThumbnailPath = inThumbnailPath;

        mTitle = title;
        mPublish = publish;
        mTime = time;
        mServings = servings;

        mIngredients = ingredients;
        mSteps = steps;

        mEditing = editing;
        mRecipeFirebaseKeyForEditing = firebaseKeyForEditing;

        mNewImage = true;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (mNewImage) {
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
                            final DatabaseReference reference = FirebaseUtil.getBaseRef();
                            DatabaseReference recipesRef = FirebaseUtil.getRecipesRef();
                            String newRecipeKey;
                            if (!mEditing) newRecipeKey = recipesRef.push().getKey();
                            else newRecipeKey = mRecipeFirebaseKeyForEditing;

                            final Uri thumbnailUrl = taskSnapshot.getDownloadUrl();
                            Log.d(TAG, "doInBackground:fullSizeRef:onSuccess:thumbnailRef:onSuccess:thumbnailUrl: " + thumbnailUrl.toString());

                            Author author = FirebaseUtil.getAuthor();
                            if (author == null) {
                                FirebaseCrash.logcat(Log.ERROR, TAG, "Couldn't upload recipe: Couldn't get signed in author.");
                                mCallback.onRecipeUploaded("Author not signed in");
                                return;
                            }

                            Recipe recipe = new Recipe(
                                    author,
                                    fullSizeUrl.toString(),
                                    fullSizeRef.toString(),
                                    thumbnailUrl.toString(),
                                    thumbnailRef.toString(),
                                    mTitle,
                                    mPublish,
                                    mTime,
                                    mServings,
                                    mIngredients,
                                    mSteps,
                                    newRecipeKey
                            );

                            // Add recipe to local SQLite DB
                            RecipistDbHandler recipistDbHandler = new RecipistDbHandler(mContext);
                            if (mEditing) {
                                recipistDbHandler.updateRecipeInDb(recipe, mIngredients, mSteps);
                            } else {
                                recipistDbHandler.addRecipeToDb(recipe, mIngredients, mSteps);
                            }

                            Map<String, Object> updatedUserData = new HashMap<>();
                            updatedUserData.put(FirebaseUtil.getUsersPath() + author.getUid() + "/" + FirebaseUtil.getRecipesPath() + newRecipeKey, true);
                            updatedUserData.put(FirebaseUtil.getRecipesPath() + newRecipeKey, new ObjectMapper().convertValue(recipe, Map.class));
                            reference.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        mCallback.onRecipeUploaded(null);
                                    } else {
                                        Log.e(TAG, "Unable to create new recipe: " + databaseError.getMessage());
                                        FirebaseCrash.report(databaseError.toException());
                                        mCallback.onRecipeUploaded("Error uploading recipe task...");
                                    }
                                }
                            });
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
        } else {
            final DatabaseReference reference = FirebaseUtil.getBaseRef();
            DatabaseReference recipesRef = FirebaseUtil.getRecipesRef();
            String newRecipeKey;
            if (!mEditing) newRecipeKey = recipesRef.push().getKey();
            else newRecipeKey = mRecipeFirebaseKeyForEditing;

            Author author = FirebaseUtil.getAuthor();
            if (author == null) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Couldn't upload recipe: Couldn't get signed in author.");
                mCallback.onRecipeUploaded("Author not signed in");
                return null;
            }

            Recipe recipe = new Recipe(
                    author,
                    mFullSizeUrl,
                    mFullSizeRef,
                    mThumbnailUrl,
                    mThumbnailRef,
                    mTitle,
                    mPublish,
                    mTime,
                    mServings,
                    mIngredients,
                    mSteps,
                    newRecipeKey
            );

            // Add recipe to local SQLite DB
            RecipistDbHandler recipistDbHandler = new RecipistDbHandler(mContext);
            if (mEditing) {
                recipistDbHandler.updateRecipeInDb(recipe, mIngredients, mSteps);
            } else {
                recipistDbHandler.addRecipeToDb(recipe, mIngredients, mSteps);
            }

            Map<String, Object> updatedUserData = new HashMap<>();
            updatedUserData.put(FirebaseUtil.getUsersPath() + author.getUid() + "/" + FirebaseUtil.getRecipesPath() + newRecipeKey, true);
            updatedUserData.put(FirebaseUtil.getRecipesPath() + newRecipeKey, new ObjectMapper().convertValue(recipe, Map.class));
            reference.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mCallback.onRecipeUploaded(null);
                    } else {
                        Log.e(TAG, "Unable to create new recipe: " + databaseError.getMessage());
                        FirebaseCrash.report(databaseError.toException());
                        mCallback.onRecipeUploaded("Error uploading recipe task...");
                    }
                }
            });
        }


        return null;
    }

    @Override
    protected void onPreExecute() {

    }
}