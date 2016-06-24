package com.example.tobias.recipist.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.example.tobias.recipist.data.RecipistContract;
import com.example.tobias.recipist.model.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Tobias on 24-06-2016.
 */
public class FetchMyRecipesTask extends AsyncTask<Void, Void, List<Recipe>> {
    public static String TAG = FetchMyRecipesTask.class.getSimpleName();

    private Context mContext;

    public FetchMyRecipesTask(Context context) {
        Log.d(TAG, "FetchMyRecipesTask: context: " + context);
        mContext = context;
    }

    @Override
    protected List<Recipe> doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: params " + Arrays.toString(params));
        Cursor cursor = mContext.getContentResolver().query(
                RecipistContract.RecipeEntry.CONTENT_URI,
                Recipe.RECIPE_COLUMNS,
                null,
                null,
                null
        );

        return getRecipesData(cursor);
    }

    private List<Recipe> getRecipesData(Cursor cursor) {
        Log.d(TAG, "getRecipesData: cursor: " + cursor);
        List<Recipe> results = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, "getRecipesData: cursor != null && cursor.moveToFirst");
            do {
                Recipe recipe = new Recipe(cursor);
                results.add(recipe);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return results;
    }

    @Override
    protected void onPostExecute(List<Recipe> recipes) {
        Log.d(TAG, "onPostExecute: recipes: " + recipes);
        if (!recipes.isEmpty()) {
            Log.d(TAG, "onPostExecute: !recipes.isEmpty()");
            for (Recipe recipe : recipes) {
                System.out.println("Recipe.title = " + recipe.title);
                System.out.println("Recipe.authorUid = " + recipe.authorUid);
                System.out.println("Recipe.firebaseKey = " + recipe.firebaseKey);
                System.out.println("\n");
            }
        }
    }
}
