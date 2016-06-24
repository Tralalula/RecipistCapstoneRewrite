package com.example.tobias.recipist.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Tobias on 23-06-2016.
 */
@IgnoreExtraProperties
public class Author implements Parcelable {
    public String email;
    public String fullName;
    public String uid;

    public Author() {
        // Empty default constructor, necessary for Firebase to be able to deserialize recipes.
    }

    public Author(String email, String fullName, String uid) {
        this.email = email;
        this.fullName = fullName;
        this.uid = uid;
    }

    protected Author(Parcel in) {
        email = in.readString();
        fullName = in.readString();
        uid = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(fullName);
        dest.writeString(uid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUid() {
        return uid;
    }
}
