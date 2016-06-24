package com.example.tobias.recipist.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Tobias on 23-06-2016.
 */
public class Ingredients {
    public ArrayList<Ingredient> results;

    public static class Ingredient implements Parcelable {
        public String ingredient;

        public Ingredient() {
            // Empty default constructor, necessary for Firebase to be able to deserialize recipes.
        }

        private Ingredient(Parcel source) {
            ingredient = source.readString();
        }

        public Ingredient(String ingredient) {
            this.ingredient = ingredient;
        }

        public String getIngredient() {
            return ingredient;
        }

        public void setIngredient(String ingredient) {
            this.ingredient = ingredient;
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