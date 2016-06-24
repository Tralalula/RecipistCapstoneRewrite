// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/FirebaseUtil.java

package com.example.tobias.recipist.util;

import com.example.tobias.recipist.model.Author;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by Tobias on 22-06-2016.
 */
public class FirebaseUtil {
    private static String RECIPES = "recipes";
    private static String USERS = "users";
    private static String AUTHOR_UID = "authorUid";

    private static FirebaseDatabase mDatabase;
    private static FirebaseAuth mAuth;

    public static FirebaseAuth getAuth() {
        if (mAuth == null) mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }

    public static FirebaseUser getCurrentUser() {
         return getAuth().getCurrentUser();
    }

    public static String getCurrentUserId() {
        if (getCurrentUser() != null) return getCurrentUser().getUid();
        return null;
    }

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) mDatabase = FirebaseDatabase.getInstance();
        return mDatabase;
    }

    public static DatabaseReference getBaseRef() {
        return getDatabase().getReference();
    }

    public static Author getAuthor() {
        FirebaseUser author = getCurrentUser();
        if (author != null) return new Author(author.getEmail(), author.getDisplayName(), author.getUid());
        return null;
    }

    public static DatabaseReference getCurrentUserRef() {
        if (getCurrentUserId() != null) return getBaseRef().child(USERS).child(getCurrentUserId());
        return null;
    }

    public static DatabaseReference getRecipesRef() {
        return getBaseRef().child(RECIPES);
    }

    public static String getRecipesPath() {
        return RECIPES + "/";
    }

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child(USERS);
    }

    public static String getUsersPath() {
        return USERS + "/";
    }

    public static Query getUsersRecipesQuery() {
        return getRecipesRef().orderByChild(AUTHOR_UID).equalTo(getCurrentUserId());
    }
}
