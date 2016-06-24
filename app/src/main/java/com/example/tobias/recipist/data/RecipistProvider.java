package com.example.tobias.recipist.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Tobias on 24-06-2016.
 */
public class RecipistProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RecipistDbHelper mDbHelper;

    private static final int RECIPES = 666;
    private static final int INGREDIENTS = 667;
    private static final int STEPS = 668;

    @Override
    public boolean onCreate() {
        mDbHelper = new RecipistDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case RECIPES:
                cursor = db.query(
                        RecipistContract.RecipeEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case INGREDIENTS:
                cursor = db.query(
                        RecipistContract.IngredientEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case STEPS:
                cursor = db.query(
                        RecipistContract.StepEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Query: Unknown Uri: " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case RECIPES:
                return RecipistContract.RecipeEntry.CONTENT_TYPE;
            case INGREDIENTS:
                return RecipistContract.IngredientEntry.CONTENT_TYPE;
            case STEPS:
                return RecipistContract.StepEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("GetType: Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case RECIPES:
                long _recipeId = db.insert(RecipistContract.RecipeEntry.TABLE_NAME, null, contentValues);
                if (_recipeId > 0) {
                    returnUri = RecipistContract.RecipeEntry.buildRecipeUri(_recipeId);
                } else {
                    failedToInsertRowInto(uri);
                }
                break;
            case INGREDIENTS:
                long _ingredientId = db.insert(RecipistContract.IngredientEntry.TABLE_NAME, null, contentValues);
                if (_ingredientId > 0) {
                    returnUri = RecipistContract.IngredientEntry.buildIngredientUri(_ingredientId);
                } else {
                    failedToInsertRowInto(uri);
                }
                break;
            case STEPS:
                long _stepId = db.insert(RecipistContract.StepEntry.TABLE_NAME, null, contentValues);
                if (_stepId > 0) {
                    returnUri = RecipistContract.StepEntry.buildIngredientUri(_stepId);
                } else {
                    failedToInsertRowInto(uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Insert: Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    private void failedToInsertRowInto(Uri uri) {
        throw new android.database.SQLException("Failed to insert row into: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int deletedRows;

        if (selection == null) selection = "1";
        switch (match) {
            case RECIPES:
                deletedRows = db.delete(
                        RecipistContract.RecipeEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case INGREDIENTS:
                deletedRows = db.delete(
                        RecipistContract.IngredientEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case STEPS:
                deletedRows = db.delete(
                        RecipistContract.StepEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Delete: Unknown Uri: " + uri);
        }

        if (deletedRows != 0) getContext().getContentResolver().notifyChange(uri, null);

        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if (selection == null) selection = "1";
        switch (match) {
            case RECIPES:
                rowsUpdated = db.update(
                        RecipistContract.RecipeEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            case INGREDIENTS:
                rowsUpdated = db.update(
                        RecipistContract.IngredientEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            case STEPS:
                rowsUpdated = db.update(
                        RecipistContract.StepEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Update: Unknown Uri: " + uri);
        }

        return rowsUpdated;
    }

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RecipistContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, RecipistContract.PATH_RECIPE, RECIPES);
        matcher.addURI(authority, RecipistContract.PATH_INGREDIENT, INGREDIENTS);
        matcher.addURI(authority, RecipistContract.PATH_STEP, STEPS);
        return matcher;
    }
}
