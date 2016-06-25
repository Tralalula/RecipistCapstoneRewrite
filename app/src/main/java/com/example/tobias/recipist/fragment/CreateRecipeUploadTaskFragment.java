// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/NewPostUploadTaskFragment.java

package com.example.tobias.recipist.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.example.tobias.recipist.callback.TaskCallback;
import com.example.tobias.recipist.model.Ingredients;
import com.example.tobias.recipist.model.Steps;
import com.example.tobias.recipist.task.LoadResizedBitmapTask;
import com.example.tobias.recipist.task.UploadRecipeTask;

import java.util.ArrayList;

/**
 * Created by Tobias on 23-06-2016.
 */
public class CreateRecipeUploadTaskFragment extends Fragment {
    public static String TAG = CreateRecipeUploadTaskFragment.class.getSimpleName();

    private Context mContext;
    private TaskCallback mCallback;

    private Bitmap mSelectedBitmap;
    private Bitmap mThumbnail;

    public CreateRecipeUploadTaskFragment() {

    }

    public static CreateRecipeUploadTaskFragment newInstance() {
        return new CreateRecipeUploadTaskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskCallback) mCallback = (TaskCallback) context;
        else throw new RuntimeException(context.toString() + " must implement TaskCallback");
        mContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public Bitmap getSelectedBitmap() {
        return mSelectedBitmap;
    }

    public void setSelectedBitmap(Bitmap mSelectedBitmap) {
        mSelectedBitmap = mSelectedBitmap;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(Bitmap mThumbnail) {
        mThumbnail = mThumbnail;
    }

    public void resizeBitmap(Uri uri, int maxDimension) {
        LoadResizedBitmapTask task = new LoadResizedBitmapTask(mContext, mCallback, maxDimension);
        task.execute(uri);
    }

    public void uploadRecipe(Bitmap bitmap, String inBitmapPath,
                             Bitmap thumbnail, String inThumbnailPath,
                             String inFileName,
                             String title, int progress, String time, String servings,
                             ArrayList<Ingredients.Ingredient> ingredients,
                             ArrayList<Steps.Step> steps) {
        UploadRecipeTask task = new UploadRecipeTask(
                mContext,
                mCallback,
                bitmap,
                inBitmapPath,
                thumbnail,
                inThumbnailPath,
                inFileName,
                title,
                progress,
                time,
                servings,
                ingredients,
                steps
        );

        task.execute();
    }
}
