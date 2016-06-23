package com.example.tobias.recipist.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

/**
 * Created by Tobias on 23-06-2016.
 */
@IgnoreExtraProperties
public class Recipe implements Parcelable {
    public Author author;

    public String fullSizeImageUrl;
    public String fullSizeImageStorageUrl;
    public String thumbnailImageUrl;
    public String thumbnailImageStorageUrl;

    public String title;
    public String progress;
    public String time;
    public String servings;

    public Recipe() {
        // Empty default constructor, necessary for Firebase to be able to deserialize recipes.
    }

    public Recipe(Author author,
                  String fullSizeImageUrl, String fullSizeImageStorageUrl,
                  String thumbnailImageUrl, String thumbnailImageStorageUrl,
                  String title, String progress, String time, String servings) {
        this.author = author;
        this.fullSizeImageUrl = fullSizeImageUrl;
        this.fullSizeImageStorageUrl = fullSizeImageStorageUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.thumbnailImageStorageUrl = thumbnailImageStorageUrl;
        this.title = title;
        this.progress = progress;
        this.time = time;
        this.servings = servings;
    }

    protected Recipe(Parcel in) {
        author = in.readParcelable(Author.class.getClassLoader());
        fullSizeImageUrl = in.readString();
        fullSizeImageStorageUrl = in.readString();
        thumbnailImageUrl = in.readString();
        thumbnailImageStorageUrl = in.readString();
        title = in.readString();
        progress = in.readString();
        time = in.readString();
        servings = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(author, flags);
        dest.writeString(fullSizeImageUrl);
        dest.writeString(fullSizeImageStorageUrl);
        dest.writeString(thumbnailImageUrl);
        dest.writeString(thumbnailImageStorageUrl);
        dest.writeString(title);
        dest.writeString(progress);
        dest.writeString(time);
        dest.writeString(servings);
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
