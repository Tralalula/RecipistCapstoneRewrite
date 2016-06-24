package com.example.tobias.recipist.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Tobias on 24-06-2016.
 */
public class RecipistContract {
    public static final String CONTENT_AUTHORITY = "com.example.tobias.recipist";
    public static final String PATH_RECIPE = "recipe";
    public static final String PATH_INGREDIENT = "ingredient";
    public static final String PATH_STEP = "step";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class RecipeEntry implements BaseColumns {
        public static final String TABLE_NAME = "recipe";

        public static final String COLUMN_AUTHOR_UID = "author_uid";
        public static final String COLUMN_FULL_SIZE_IMAGE_URL = "full_size_image_url";
        public static final String COLUMN_FULL_SIZE_IMAGE_STORAGE_URL = "full_size_image_storage_url";
        public static final String COLUMN_THUMBNAIL_IMAGE_URL = "thumbnail_image_url";
        public static final String COLUMN_THUMBNAIL_IMAGE_STORAGE_URL = "thumbnail_image_storage_url";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_PROGRESS = "progress";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_SERVINGS = "servings";
        public static final String COLUMN_FIREBASE_KEY = "firebase_key";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECIPE;

        public static Uri buildRecipeUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class IngredientEntry implements BaseColumns {
        public static final String TABLE_NAME = "ingredient";

        public static final String COLUMN_RECIPE_FIREBASE_KEY = "recipe_id";
        public static final String COLUMN_ORDER_NUMBER = "order_number";
        public static final String COLUMN_INGREDIENT = "ingredient";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INGREDIENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INGREDIENT;

        public static Uri buildIngredientUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class StepEntry implements BaseColumns {
        public static final String TABLE_NAME = "step";

        public static final String COLUMN_RECIPE_FIREBASE_KEY = "recipe_id";
        public static final String COLUMN_ORDER_NUMBER = "order_number";
        public static final String COLUMN_METHOD = "method";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STEP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STEP;

        public static Uri buildIngredientUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}