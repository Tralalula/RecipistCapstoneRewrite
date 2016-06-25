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
import com.example.tobias.recipist.data.RecipistDbHandler;
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

    private String mTitle;
    private int mProgress;
    private String mTime;
    private String mServings;

    public UploadRecipeTask(Context context, TaskCallback callback,
                            Bitmap bitmap, String inBitmapPath,
                            Bitmap thumbnail, String inThumbnailPath, String inFileName,
                            String title, int progress, String time, String servings) {
        mContext = context;
        mCallback = callback;

        mBitmapReference = new WeakReference<>(bitmap);
        mThumbnailReference = new WeakReference<>(thumbnail);

        mFileName = inFileName;
        mBitmapPath = inBitmapPath;
        mThumbnailPath = inThumbnailPath;

        mTitle = title;
        mProgress = progress;
        mTime = time;
        mServings = servings;
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
                        final DatabaseReference reference = FirebaseUtil.getBaseRef();
                        DatabaseReference recipesRef = FirebaseUtil.getRecipesRef();
                        final String newRecipeKey = recipesRef.push().getKey();

                        final Uri thumbnailUrl = taskSnapshot.getDownloadUrl();
                        Log.d(TAG, "doInBackground:fullSizeRef:onSuccess:thumbnailRef:onSuccess:thumbnailUrl: " + thumbnailUrl.toString());

                        Author author = FirebaseUtil.getAuthor();
                        if (author == null) {
                            FirebaseCrash.logcat(Log.ERROR, TAG, "Couldn't upload recipe: Couldn't get signed in author.");
                            mCallback.onRecipeUploaded("Author not signed in");
                            return;
                        }

                        ArrayList<Ingredients.Ingredient> ingredients = new ArrayList<>();
                        ingredients.add(new Ingredients.Ingredient("5 pounds (2.25 kg) apples, peeled, cored, and sliced 1/2 inch thick"));
                        ingredients.add(new Ingredients.Ingredient("100-150g sugar, plus more for sprinkling"));
                        ingredients.add(new Ingredients.Ingredient("15g cornstarch"));
                        ingredients.add(new Ingredients.Ingredient("1/2 tsp ground cinnamon"));
                        ingredients.add(new Ingredients.Ingredient("2 tsp fresh juice and 1 tsp grated zest from 1 lemon"));
                        ingredients.add(new Ingredients.Ingredient("1 recipe easy pie dough"));
                        ingredients.add(new Ingredients.Ingredient("1 egg white"));

                        ArrayList<Steps.Step> steps = new ArrayList<>();
                        steps.add(new Steps.Step("Adjust oven rack to lower middle position and place a heavy rimmed baking sheet on it. Preheat the oven to 425°F (220°C). Toss apple slices with sugar, cornstarch, cinnamon, and lemon juice and zest until well-coated. Let rest for 10 minutes."));
                        steps.add(new Steps.Step("To Cook Filling on the Stovetop: Transfer apples and their juices to a large Dutch oven. Heat over low heat, stirring constantly, until lightly steaming. Cover and continue cooking over lowest heat setting, stirring frequently, using a thermometer to maintain temperature at below 160°F (71°C). Do not allow liquid to come to a boil for first 20 minutes. After 20 minutes, increase heat to medium-high and cook, stirring frequently, until juices thicken enough that a spatula dragged through the bottom of the pot leaves a trail that very slowly closes back up, about 10 minutes. Transfer apples to a rimmed baking sheet, spread out into a single layer, and allow to cool completely, about 1 hour."));
                        steps.add(new Steps.Step("To Cook Filling in a Sous-Vide Precision Cooker (see note above): Set precision cooker to 160°F (70°C). Transfer apples and their juices to a vacuum bag and seal. Cook in water bath for 1 hour. Transfer contents to a large Dutch oven and heat over medium-high heat, stirring frequently, until juices thicken enough that a spatula dragged through the bottom of the pot leaves a trail that very slowly closes back up, about 10 minutes. Transfer apples to a rimmed baking sheet, spread out into a single layer, and allow to cool completely, about 1 hour."));
                        steps.add(new Steps.Step("Roll one disk of pie dough into a circle roughly 12-inches in diameter. Transfer to a 9-inch pie plate. Add filling, piling it into the pie shell until it all fits. Roll remaining disk of pie dough into a circle roughly 12-inches in diameter. Transfer to top of pie."));
                        steps.add(new Steps.Step("Using a pair of kitchen shears, trim the edges of both pie crusts until they overhang the edge of the pie plate by 1/2 inch all the way around. Fold edges of both pie crusts down together, tucking them in between the bottom crust and the pie plate and working your way all the way around the pie plate until everything is well tucked. Use the forefinger on your left hand and the thumb and forefinger on your right hand to crimp the edges. Cut 5 slits in the top with a sharp knife for ventilation."));
                        steps.add(new Steps.Step("Use a pastry brush to brush an even coat of lightly beaten egg white all over the top surface of the pie. Sprinkle evenly with a tablespoon of sugar. Transfer pie to sheet tray in the oven and bake until light golden brown, about 20 minutes. Reduce heat to 375°F (190°C) and continue baking until deep golden brown, about 25 minutes longer. Remove from oven and allow to cool at room temperature for at least 4 hours before serving."));

                        Recipe recipe = new Recipe(
                                author,
                                fullSizeUrl.toString(),
                                fullSizeRef.toString(),
                                thumbnailUrl.toString(),
                                thumbnailRef.toString(),
                                mTitle,
                                mProgress,
                                mTime,
                                mServings,
                                ingredients,
                                steps,
                                newRecipeKey
                        );

                        // Add recipe to local SQLite DB
                        RecipistDbHandler recipistDbHandler = new RecipistDbHandler(mContext);
                        recipistDbHandler.addRecipeToDb(recipe, ingredients, steps);

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

        return null;
    }

    @Override
    protected void onPreExecute() {

    }
}