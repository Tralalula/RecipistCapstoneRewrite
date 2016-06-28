package com.example.tobias.recipist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.tobias.recipist.model.Ingredients;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.model.Steps;

import java.util.List;

/**
 * Created by Tobias on 24-06-2016.
 */
public class RecipistDbHandler {
    public static String TAG = RecipistDbHandler.class.getSimpleName();

    private Context mContext;

    public RecipistDbHandler(Context context) {
        mContext = context;
    }

    public void addRecipeToDb(Recipe recipe, List<Ingredients.Ingredient> ingredients, List<Steps.Step> steps) {
        Log.d(TAG, "addRecipeToDb: Start");
        if (recipe != null) {
            Cursor cursor = recipeExistsInDb(recipe.firebaseKey);
            if (!cursor.moveToFirst()) {
                ContentValues recipeValues = new ContentValues();
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_AUTHOR_UID], recipe.authorUid);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_FULL_SIZE_IMAGE_URL], recipe.fullSizeImageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_FULL_SIZE_IMAGE_STORAGE_URL], recipe.fullSizeImageStorageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_THUMBNAIL_URL], recipe.thumbnailImageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_THUMBNAIL_STORAGE_URL], recipe.thumbnailImageStorageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_TITLE], recipe.title);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_PUBLISH], recipe.publish);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_TIME], recipe.time);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_SERVINGS], recipe.servings);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_FIREBASE_KEY], recipe.firebaseKey);
                mContext.getContentResolver().insert(RecipistContract.RecipeEntry.CONTENT_URI, recipeValues);

                for (int i = 0; i < ingredients.size(); i++) {
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put(Ingredients.Ingredient.INGREDIENT_COLUMNS[Ingredients.Ingredient.COL_RECIPE_FIREBASE_KEY], recipe.firebaseKey);
                    ingredientValues.put(Ingredients.Ingredient.INGREDIENT_COLUMNS[Ingredients.Ingredient.COL_ORDER_NUMBER], i);
                    ingredientValues.put(Ingredients.Ingredient.INGREDIENT_COLUMNS[Ingredients.Ingredient.COL_INGREDIENT], ingredients.get(i).ingredient);
                    mContext.getContentResolver().insert(RecipistContract.IngredientEntry.CONTENT_URI, ingredientValues);
                }

                for (int i = 0; i < steps.size(); i++) {
                    ContentValues stepValues = new ContentValues();
                    stepValues.put(Steps.Step.STEP_COLUMENS[Steps.Step.COL_RECIPE_FIREBASE_KEY], recipe.firebaseKey);
                    stepValues.put(Steps.Step.STEP_COLUMENS[Steps.Step.COL_ORDER_NUMBER], i);
                    stepValues.put(Steps.Step.STEP_COLUMENS[Steps.Step.COL_METHOD], steps.get(i).method);
                    mContext.getContentResolver().insert(RecipistContract.StepEntry.CONTENT_URI, stepValues);
                }
            }
            cursor.close();
        }
        Log.d(TAG, "addRecipeToDb: End");
    }

    public void updateRecipeInDb(Recipe recipe, List<Ingredients.Ingredient> ingredients, List<Steps.Step> steps) {
        Log.d(TAG, "updateRecipeInDb: Start");
        if (recipe != null) {
            Cursor cursor = recipeExistsInDb(recipe.firebaseKey);
            if (cursor.moveToFirst()) {
                String selection = RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY + "=?";
                String[] selectionArgs = {recipe.firebaseKey};

                ContentValues recipeValues = new ContentValues();
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_AUTHOR_UID], recipe.authorUid);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_FULL_SIZE_IMAGE_URL], recipe.fullSizeImageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_FULL_SIZE_IMAGE_STORAGE_URL], recipe.fullSizeImageStorageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_THUMBNAIL_URL], recipe.thumbnailImageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_THUMBNAIL_STORAGE_URL], recipe.thumbnailImageStorageUrl);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_TITLE], recipe.title);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_PUBLISH], recipe.publish);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_TIME], recipe.time);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_SERVINGS], recipe.servings);
                recipeValues.put(Recipe.RECIPE_COLUMNS[Recipe.COL_FIREBASE_KEY], recipe.firebaseKey);
                mContext.getContentResolver().update(RecipistContract.RecipeEntry.CONTENT_URI, recipeValues, selection, selectionArgs);


                String ingredientDeleteWhere = RecipistContract.IngredientEntry.COLUMN_RECIPE_FIREBASE_KEY + "=?";
                mContext.getContentResolver().delete(RecipistContract.IngredientEntry.CONTENT_URI, ingredientDeleteWhere, selectionArgs);

                for (int i = 0; i < ingredients.size(); i++) {
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put(Ingredients.Ingredient.INGREDIENT_COLUMNS[Ingredients.Ingredient.COL_RECIPE_FIREBASE_KEY], recipe.firebaseKey);
                    ingredientValues.put(Ingredients.Ingredient.INGREDIENT_COLUMNS[Ingredients.Ingredient.COL_ORDER_NUMBER], i);
                    ingredientValues.put(Ingredients.Ingredient.INGREDIENT_COLUMNS[Ingredients.Ingredient.COL_INGREDIENT], ingredients.get(i).ingredient);
                    mContext.getContentResolver().insert(RecipistContract.IngredientEntry.CONTENT_URI, ingredientValues);
                }

                String stepDeleteWhere = RecipistContract.StepEntry.COLUMN_RECIPE_FIREBASE_KEY + "=?";
                mContext.getContentResolver().delete(RecipistContract.StepEntry.CONTENT_URI, stepDeleteWhere, selectionArgs);

                for (int i = 0; i < steps.size(); i++) {
                    ContentValues stepValues = new ContentValues();
                    stepValues.put(Steps.Step.STEP_COLUMENS[Steps.Step.COL_RECIPE_FIREBASE_KEY], recipe.firebaseKey);
                    stepValues.put(Steps.Step.STEP_COLUMENS[Steps.Step.COL_ORDER_NUMBER], i);
                    stepValues.put(Steps.Step.STEP_COLUMENS[Steps.Step.COL_METHOD], steps.get(i).method);
                    mContext.getContentResolver().insert(RecipistContract.StepEntry.CONTENT_URI, stepValues);
                }
            }
            cursor.close();
        }
        Log.d(TAG, "updateRecipeInDb: End");
    }

    private Cursor recipeExistsInDb(String firebaseKey) {
        return mContext.getContentResolver().query(
                RecipistContract.RecipeEntry.CONTENT_URI,
                new String[]{
                        RecipistContract.RecipeEntry._ID,
                        RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY
                },
                RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY + " = ?",
                new String[]{firebaseKey},
                null
        );
    }

    public boolean deleteRecipe(String firebaseKey) {
        Cursor cursor = recipeExistsInDb(firebaseKey);
        if (!cursor.moveToFirst()) return false;

        String selection = RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY + "=?";
        String[] selectionArgs = {firebaseKey};

        // Delete recipe
        mContext.getContentResolver().delete(
                RecipistContract.RecipeEntry.CONTENT_URI,
                selection,
                selectionArgs
        );

        // Delete the ingredients associated with the recipe
        String ingredientsSelection = RecipistContract.IngredientEntry.COLUMN_RECIPE_FIREBASE_KEY + "=?";
        mContext.getContentResolver().delete(
                RecipistContract.IngredientEntry.CONTENT_URI,
                ingredientsSelection,
                selectionArgs
        );

        // Delete the steps associated with the recipe
        String stepsSelection = RecipistContract.StepEntry.COLUMN_RECIPE_FIREBASE_KEY + "=?";
        mContext.getContentResolver().delete(
                RecipistContract.StepEntry.CONTENT_URI,
                stepsSelection,
                selectionArgs
        );

        cursor.close();

        return true;
    }

}
