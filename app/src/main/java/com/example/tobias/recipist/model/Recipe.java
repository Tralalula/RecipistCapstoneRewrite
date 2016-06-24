package com.example.tobias.recipist.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.tobias.recipist.data.RecipistContract;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

/**
 * Created by Tobias on 23-06-2016.
 */
@IgnoreExtraProperties
public class Recipe implements Parcelable {
    public static final String[] RECIPE_COLUMNS = {
            RecipistContract.RecipeEntry._ID,
            RecipistContract.RecipeEntry.COLUMN_AUTHOR_UID,
            RecipistContract.RecipeEntry.COLUMN_FULL_SIZE_IMAGE_URL,
            RecipistContract.RecipeEntry.COLUMN_FULL_SIZE_IMAGE_STORAGE_URL,
            RecipistContract.RecipeEntry.COLUMN_THUMBNAIL_IMAGE_URL,
            RecipistContract.RecipeEntry.COLUMN_THUMBNAIL_IMAGE_STORAGE_URL,
            RecipistContract.RecipeEntry.COLUMN_TITLE,
            RecipistContract.RecipeEntry.COLUMN_PROGRESS,
            RecipistContract.RecipeEntry.COLUMN_TIME,
            RecipistContract.RecipeEntry.COLUMN_SERVINGS,
            RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY
    };
    public static final int COL_ID = 0;
    public static final int COL_AUTHOR_UID = 1;
    public static final int COL_FULL_SIZE_IMAGE_URL = 2;
    public static final int COL_FULL_SIZE_IMAGE_STORAGE_URL = 3;
    public static final int COL_THUMBNAIL_URL = 4;
    public static final int COL_THUMBNAIL_STORAGE_URL = 5;
    public static final int COL_TITLE = 6;
    public static final int COL_PROGRESS = 7;
    public static final int COL_TIME = 8;
    public static final int COL_SERVINGS = 9;
    public static final int COL_FIREBASE_KEY = 10;

    public Author author;
    public String authorUid;

    public String fullSizeImageUrl;
    public String fullSizeImageStorageUrl;
    public String thumbnailImageUrl;
    public String thumbnailImageStorageUrl;

    public String title;
    public int progress;
    public String time;
    public String servings;

    public ArrayList<Ingredients.Ingredient> ingredients;
    public ArrayList<Steps.Step> steps;

    public String firebaseKey;


    public Recipe() {
        // Empty default constructor, necessary for Firebase to be able to deserialize recipes.
    }

    public Recipe(Author author,
                  String fullSizeImageUrl, String fullSizeImageStorageUrl,
                  String thumbnailImageUrl, String thumbnailImageStorageUrl,
                  String title, int progress, String time, String servings,
                  ArrayList<Ingredients.Ingredient> ingredients, ArrayList<Steps.Step> steps,
                  String firebaseKey) {
        this.author = author;
        this.authorUid = this.author.uid;
        this.fullSizeImageUrl = fullSizeImageUrl;
        this.fullSizeImageStorageUrl = fullSizeImageStorageUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.thumbnailImageStorageUrl = thumbnailImageStorageUrl;
        this.title = title;
        this.progress = progress;
        this.time = time;
        this.servings = servings;
        this.ingredients = ingredients;
        this.steps = steps;
        this.firebaseKey = firebaseKey;
    }

    public Recipe(Cursor cursor) {
        authorUid = cursor.getString(COL_AUTHOR_UID);
        fullSizeImageUrl = cursor.getString(COL_FULL_SIZE_IMAGE_URL);
        fullSizeImageStorageUrl = cursor.getString(COL_FULL_SIZE_IMAGE_STORAGE_URL);
        thumbnailImageUrl = cursor.getString(COL_THUMBNAIL_URL);
        thumbnailImageStorageUrl = cursor.getString(COL_THUMBNAIL_STORAGE_URL);
        title = cursor.getString(COL_TITLE);
        progress = cursor.getInt(COL_PROGRESS);
        time = cursor.getString(COL_TIME);
        servings = cursor.getString(COL_SERVINGS);
        firebaseKey = cursor.getString(COL_FIREBASE_KEY);
    }

    protected Recipe(Parcel in) {
        author = in.readParcelable(Author.class.getClassLoader());
        authorUid = in.readString();
        fullSizeImageUrl = in.readString();
        fullSizeImageStorageUrl = in.readString();
        thumbnailImageUrl = in.readString();
        thumbnailImageStorageUrl = in.readString();
        title = in.readString();
        progress = in.readInt();
        time = in.readString();
        servings = in.readString();
        ingredients = in.createTypedArrayList(Ingredients.Ingredient.CREATOR);
        steps = in.createTypedArrayList(Steps.Step.CREATOR);
        firebaseKey = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(author, flags);
        dest.writeString(authorUid);
        dest.writeString(fullSizeImageUrl);
        dest.writeString(fullSizeImageStorageUrl);
        dest.writeString(thumbnailImageUrl);
        dest.writeString(thumbnailImageStorageUrl);
        dest.writeString(title);
        dest.writeInt(progress);
        dest.writeString(time);
        dest.writeString(servings);
        dest.writeTypedList(ingredients);
        dest.writeTypedList(steps);
        dest.writeString(firebaseKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };
}
