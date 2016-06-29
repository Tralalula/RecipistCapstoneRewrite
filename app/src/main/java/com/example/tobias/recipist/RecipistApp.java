// https://stackoverflow.com/questions/37346363/java-lang-illegalstateexception-firebaseapp-with-name-default

package com.example.tobias.recipist;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Tobias on 24-06-2016.
 */
public class RecipistApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // setPeristenceEnabled is not enabled because when I try to find all recipes that are public,
        // I get a Firebase crash error, when I close the app and re-open it after 2 times.
        // It only happens when I try to find recipes that are public, if I just search for all recipes, no error.
//        if (!FirebaseApp.getApps(this).isEmpty()) {
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        }
    }
}