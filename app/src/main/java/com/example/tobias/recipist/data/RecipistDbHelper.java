package com.example.tobias.recipist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tobias on 24-06-2016.
 */
public class RecipistDbHelper extends SQLiteOpenHelper {
    public static int DATABASE_VERSION = 3;
    public static String DATABASE_NAME = "recipist.db";

    public RecipistDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_RECIPE_TABLE = "CREATE TABLE " +
                RecipistContract.RecipeEntry.TABLE_NAME + " (" +
                RecipistContract.RecipeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RecipistContract.RecipeEntry.COLUMN_AUTHOR_UID + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_FULL_SIZE_IMAGE_URL + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_FULL_SIZE_IMAGE_STORAGE_URL + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_THUMBNAIL_IMAGE_URL + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_THUMBNAIL_IMAGE_STORAGE_URL + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_PROGRESS + " INTEGER NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_SERVINGS + " TEXT NOT NULL, " +
                RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY + " TEXT NOT NULL);";

        final String SQL_CREATE_INGREDIENT_TABLE = "CREATE TABLE " +
                RecipistContract.IngredientEntry.TABLE_NAME + " (" +
                RecipistContract.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RecipistContract.IngredientEntry.COLUMN_RECIPE_FIREBASE_KEY + " TEXT NOT NULL, " +
                RecipistContract.IngredientEntry.COLUMN_ORDER_NUMBER + " INTEGER NOT NULL, " +
                RecipistContract.IngredientEntry.COLUMN_INGREDIENT + " TEXT NOT NULL);";

        final String SQL_CREATE_STEP_TABLE = "CREATE TABLE " +
                RecipistContract.StepEntry.TABLE_NAME + " (" +
                RecipistContract.StepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RecipistContract.StepEntry.COLUMN_RECIPE_FIREBASE_KEY + " TEXT NOT NULL, " +
                RecipistContract.StepEntry.COLUMN_ORDER_NUMBER + " INTEGER NOT NULL, " +
                RecipistContract.StepEntry.COLUMN_METHOD + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_RECIPE_TABLE);
        db.execSQL(SQL_CREATE_INGREDIENT_TABLE);
        db.execSQL(SQL_CREATE_STEP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecipistContract.RecipeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecipistContract.IngredientEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecipistContract.StepEntry.TABLE_NAME);

        onCreate(db);
    }
}
