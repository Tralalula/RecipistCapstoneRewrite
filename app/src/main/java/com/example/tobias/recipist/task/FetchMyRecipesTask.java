package com.example.tobias.recipist.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.example.tobias.recipist.data.RecipistContract;
import com.example.tobias.recipist.model.Ingredients;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.model.Steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        Cursor recipeCursor = mContext.getContentResolver().query(
                RecipistContract.RecipeEntry.CONTENT_URI,
                Recipe.RECIPE_COLUMNS,
                null,
                null,
                null
        );

        Cursor ingredientsCursor = mContext.getContentResolver().query(
                RecipistContract.IngredientEntry.CONTENT_URI,
                Ingredients.Ingredient.INGREDIENT_COLUMNS,
                null,
                null,
                null
        );

        Cursor stepsCursor = mContext.getContentResolver().query(
                RecipistContract.StepEntry.CONTENT_URI,
                Steps.Step.STEP_COLUMENS,
                null,
                null,
                null
        );

        return getRecipesData(recipeCursor, ingredientsCursor, stepsCursor);
    }

    private List<Recipe> getRecipesData(Cursor recipeCursor, Cursor ingredientsCursor, Cursor stepsCursor) {
        Log.d(TAG, "getRecipesData: cursor: " + recipeCursor);
        List<Recipe> results = new ArrayList<>();
        if (recipeCursor != null && recipeCursor.moveToFirst()) {
            Log.d(TAG, "getRecipesData: cursor != null && cursor.moveToFirst");
            do {
                Recipe recipe = new Recipe(recipeCursor);

                // Add ingredients to recipe.
                ArrayList<Ingredients.Ingredient> ingredients = new ArrayList<>();
                if (ingredientsCursor != null && ingredientsCursor.moveToFirst()) {
                    do {
                        Ingredients.Ingredient ingredient = new Ingredients.Ingredient(ingredientsCursor);
                        if (Objects.equals(ingredient.recipeFirebaseKey, recipe.firebaseKey)) {
                            ingredients.add(ingredient);
                        }
                    } while (ingredientsCursor.moveToNext());
                }
                recipe.ingredients = ingredients;

                // Add steps to recipe.
                ArrayList<Steps.Step> steps = new ArrayList<>();
                if (stepsCursor != null && stepsCursor.moveToFirst()) {
                    do {
                        Steps.Step step = new Steps.Step(stepsCursor);
                        if (Objects.equals(step.recipeFirebaseKey, recipe.firebaseKey)) {
                            steps.add(step);
                        }
                    } while (stepsCursor.moveToNext());
                }
                recipe.steps = steps;

                results.add(recipe);
            } while (recipeCursor.moveToNext());
            recipeCursor.close();
        }
        return results;
    }

    @Override
    protected void onPostExecute(List<Recipe> recipes) {
        Log.d(TAG, "onPostExecute: recipes: " + recipes);
        if (!recipes.isEmpty()) {
            Log.d(TAG, "onPostExecute: !recipes.isEmpty()");

            // Print recipe for debugging purposes..
            for (Recipe recipe : recipes) {
                System.out.println("Recipe.title = " + recipe.title);
                System.out.println("Recipe.authorUid = " + recipe.authorUid);
                System.out.println("Recipe.firebaseKey = " + recipe.firebaseKey);

                // Print recipe ingredients for debugging purposes..
                if (!recipe.ingredients.isEmpty()) {
                    for (Ingredients.Ingredient ingredient : recipe.ingredients) {
                        System.out.println("Recipe.ingredients.ingredient.recipeFirebaseKey = " + ingredient.recipeFirebaseKey);
                        System.out.println("Recipe.ingredients.ingredient.orderNumber = " + ingredient.orderNumber);
                        System.out.println("Recipe.ingredients.ingredient.ingredient = " + ingredient.ingredient);
                        System.out.println("\n");
                    }
                }

                // Print recipe steps for debugging purposes..
                if (!recipe.steps.isEmpty()) {
                    for (Steps.Step step : recipe.steps) {
                        System.out.println("Recipe.steps.step.recipeFirebaseKey = " + step.recipeFirebaseKey);
                        System.out.println("Recipe.steps.step.orderNumber = " + step.orderNumber);
                        System.out.println("Recipe.steps.step.method = " + step.method);
                        System.out.println("\n");
                    }
                }

                System.out.println("\n");
            }
        }
    }
}
