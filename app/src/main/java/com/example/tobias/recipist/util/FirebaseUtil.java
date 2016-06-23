// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/FirebaseUtil.java

package com.example.tobias.recipist.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Tobias on 22-06-2016.
 */
public class FirebaseUtil {
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
}
