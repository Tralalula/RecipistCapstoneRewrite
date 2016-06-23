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
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
    }
}
