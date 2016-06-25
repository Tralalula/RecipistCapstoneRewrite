package com.example.tobias.recipist.loader;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

import com.example.tobias.recipist.data.RecipistContract;
import com.example.tobias.recipist.model.Recipe;

/**
 * Created by Tobias on 25-06-2016.
 */
public class RecipeLoader extends CursorLoader {
    public RecipeLoader(Context context, Uri uri) {
        super(context, uri, Recipe.RECIPE_COLUMNS, null, null, null);
    }

    public static RecipeLoader newAllRecipesInstance(Context context) {
        return new RecipeLoader(context, RecipistContract.RecipeEntry.CONTENT_URI);
    }

    public static RecipeLoader newInstanceForRecipeId(Context context, long recipeId) {
        return new RecipeLoader(context, RecipistContract.RecipeEntry.buildRecipeUri(recipeId));
    }
}
