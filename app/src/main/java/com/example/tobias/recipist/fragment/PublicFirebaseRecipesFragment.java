package com.example.tobias.recipist.fragment;

import com.example.tobias.recipist.util.FirebaseUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Tobias on 28-06-2016.
 */
public class PublicFirebaseRecipesFragment extends FirebaseRecipeFragment {
    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return FirebaseUtil.getPublicRecipesQuery();  // TODO: Fix so this can be used without the app crashing..
//        return FirebaseUtil.getRecipesRef();
    }
}
