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
public class Steps {
    public ArrayList<Step> results;

    @IgnoreExtraProperties
    public static class Step implements Parcelable {
        public static final String[] STEP_COLUMENS = {
                RecipistContract.StepEntry._ID,
                RecipistContract.StepEntry.COLUMN_RECIPE_FIREBASE_KEY,
                RecipistContract.StepEntry.COLUMN_ORDER_NUMBER,
                RecipistContract.StepEntry.COLUMN_METHOD
        };

        public static final int COL_ID = 0;
        public static final int COL_RECIPE_FIREBASE_KEY = 1;
        public static final int COL_ORDER_NUMBER = 2;
        public static final int COL_METHOD = 3;

        public String recipeFirebaseKey;
        public int orderNumber;
        public String method;

        public Step() {
            // Empty default constructor, necessary for Firebase to be able to deserialize recipes.
        }

        public Step(String method) {
            this.method = method;
        }

        public Step(String recipeFirebaseKey, int orderNumber, String method) {
            this.recipeFirebaseKey = recipeFirebaseKey;
            this.orderNumber = orderNumber;
            this.method = method;
        }

        public Step(Cursor cursor) {
            this.recipeFirebaseKey = cursor.getString(COL_RECIPE_FIREBASE_KEY);
            this.orderNumber = cursor.getInt(COL_ORDER_NUMBER);
            this.method = cursor.getString(COL_METHOD);
        }

        public Step(Parcel source) {
            method = source.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(method);
        }

        public static final Parcelable.Creator<Step> CREATOR = new Parcelable.Creator<Step>() {
            @Override
            public Step createFromParcel(Parcel source) {
                return new Step(source);
            }

            @Override
            public Step[] newArray(int size) {
                return new Step[size];
            }
        };
    }
}