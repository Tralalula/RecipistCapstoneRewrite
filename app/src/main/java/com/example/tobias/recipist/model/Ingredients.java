package com.example.tobias.recipist.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.tobias.recipist.data.RecipistContract;

import java.util.ArrayList;

/**
 * Created by Tobias on 23-06-2016.
 */
public class Ingredients {
    public ArrayList<Ingredient> results;

    public static class Ingredient implements Parcelable {
        public static final String[] INGREDIENT_COLUMNS = {
                RecipistContract.IngredientEntry._ID,
                RecipistContract.IngredientEntry.COLUMN_RECIPE_FIREBASE_KEY,
                RecipistContract.IngredientEntry.COLUMN_ORDER_NUMBER,
                RecipistContract.IngredientEntry.COLUMN_INGREDIENT
        };

        public static final int COL_ID = 0;
        public static final int COL_RECIPE_FIREBASE_KEY = 1;
        public static final int COL_ORDER_NUMBER = 2;
        public static final int COL_INGREDIENT = 3;

        public String recipeFirebaseKey;
        public int orderNumber;
        public String ingredient;

        public Ingredient() {
            // Empty default constructor, necessary for Firebase to be able to deserialize recipes.
        }

        public Ingredient(String ingredient) {
            this.ingredient = ingredient;
        }

        public Ingredient(String recipeFirebaseKey, int orderNumber, String ingredient) {
            this.recipeFirebaseKey = recipeFirebaseKey;
            this.orderNumber = orderNumber;
            this.ingredient = ingredient;
        }

        public Ingredient(Cursor cursor) {
            recipeFirebaseKey = cursor.getString(COL_RECIPE_FIREBASE_KEY);
            orderNumber = cursor.getInt(COL_ORDER_NUMBER);
            ingredient = cursor.getString(COL_INGREDIENT);
        }

        private Ingredient(Parcel source) {
            ingredient = source.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(ingredient);
        }

        public static final Parcelable.Creator<Ingredient> CREATOR = new Parcelable.Creator<Ingredient>() {
            @Override
            public Ingredient createFromParcel(Parcel source) {
                return new Ingredient(source);
            }

            @Override
            public Ingredient[] newArray(int size) {
                return new Ingredient[size];
            }
        };
    }
}