package com.example.tobias.recipist.activity.recipe;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.BaseActivity;
import com.example.tobias.recipist.data.RecipistContract;
import com.example.tobias.recipist.loader.RecipeLoader;
import com.example.tobias.recipist.model.Ingredients;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.model.Steps;
import com.example.tobias.recipist.util.FirebaseUtil;
import com.example.tobias.recipist.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Tobias on 25-06-2016.
 */
public class ViewRecipeActivity extends BaseActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ViewRecipeActivity.class.getSimpleName();

    public static final String TYPE_ONLINE = TAG + "TYPE ONLINE";
    public static final String TYPE_OFFLINE = TAG + "TYPE OFFLINE";

    public static final String KEY_RECIPE_FIREBASE_KEY = TAG + "RECIPE FIREBASE KEY";
    public static final String KEY_TYPE = TAG + "TYPE";
    public static final String KEY_RECIPE_OFFLINE_ID = TAG + "RECIPE OFFLINE ID";

    @BindView(R.id.view_recipe_image_view_image) ImageView mPhotoImgVw;
    @BindView(R.id.view_recipe_text_view_title) TextView mTitleTxtVw;
    @BindView(R.id.view_recipe_text_view_progress) TextView mProgressTxtVw;
    @BindView(R.id.view_recipe_text_view_time) TextView mTimeTxtVw;
    @BindView(R.id.view_recipe_text_view_servings) TextView mServingsTxtVw;
    @BindView(R.id.view_recipe_linear_layout_ingredients_list) LinearLayout mIngredientsLinLt;
    @BindView(R.id.view_recipe_linear_layout_steps_list) LinearLayout mStepsLinLt;
    @BindView(R.id.view_recipe_floating_action_button_edit) FloatingActionButton mEditFab;

    private DatabaseReference mRecipeRef;
    private ValueEventListener mRecipeListener;
    private String mRecipeFirebaseKey;
    private Recipe mRecipe;

    private String mCurrentType;

    private Cursor mCursor;
    private long mRecipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        ButterKnife.bind(this);

        Intent data = getIntent();
        if (data == null) {
            throw new IllegalArgumentException("Intent is null.. Did you pass a recipe firebase key?");
        }

        mCurrentType = data.getStringExtra(KEY_TYPE);

        mRecipeFirebaseKey = data.getStringExtra(KEY_RECIPE_FIREBASE_KEY);
        Log.e(TAG, "mRecipeFirebaseKey = " + mRecipeFirebaseKey);
        if (Objects.equals(mCurrentType, TYPE_ONLINE)) {
            mRecipeRef = FirebaseUtil.getRecipesRef().child(mRecipeFirebaseKey);
        } else if (Objects.equals(mCurrentType, TYPE_OFFLINE)) {
            getSupportLoaderManager().initLoader(0, null, this);
            mRecipeId = (long) data.getIntExtra(KEY_RECIPE_OFFLINE_ID, -1);
        }

        // Set click listeners.
        mEditFab.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Objects.equals(mCurrentType, TYPE_ONLINE)) {
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String notSpecified = "Not specified";
                    mRecipe = dataSnapshot.getValue(Recipe.class);
                    String title = mRecipe.title;
                    String progress;
                    if (mRecipe.progress == 0) progress = "In Progress";
                    else progress = "Completed";
                    String time = mRecipe.time;
                    String servings = mRecipe.servings;

                    ArrayList<Ingredients.Ingredient> ingredients = mRecipe.ingredients;
                    ArrayList<Steps.Step> steps = mRecipe.steps;

                    Picasso.with(ViewRecipeActivity.this)
                            .load(mRecipe.fullSizeImageUrl)
                            .into(mPhotoImgVw);

                    if (Util.isNullOrEmpty(title)) title = notSpecified;
                    if (Util.isNullOrEmpty(time)) time = notSpecified;
                    if (Util.isNullOrEmpty(servings)) servings = notSpecified;

                    mTitleTxtVw.setText(title);
                    mProgressTxtVw.setText(progress);
                    mTimeTxtVw.setText(time);
                    mServingsTxtVw.setText(servings);

                    handleIngredients(mIngredientsLinLt, ingredients);
                    handleSteps(mStepsLinLt, steps);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadRecipe:onCancelled", databaseError.toException());
                    Toast.makeText(ViewRecipeActivity.this, "Couldn't load recipe!", Toast.LENGTH_SHORT).show();
                }
            };
            mRecipeRef.addValueEventListener(listener);

            // Keep copy of recipe listener so we can remove it when the app stops
            mRecipeListener = listener;
        } else if (Objects.equals(mCurrentType, TYPE_OFFLINE)) {
            handleOfflineBinding();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mRecipeListener != null) mRecipeRef.removeEventListener(mRecipeListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_recipe_floating_action_button_edit:
//                editRecipe();
                break;
        }
    }

    private void handleOfflineBinding() {
        if (mCursor != null) {
            Picasso.with(ViewRecipeActivity.this)
                    .load(mCursor.getString(Recipe.COL_FULL_SIZE_IMAGE_URL))
                    .into(mPhotoImgVw);

            mTitleTxtVw.setText(mCursor.getString(Recipe.COL_TITLE));
            String progress;
            if (mCursor.getInt(Recipe.COL_PROGRESS) == 0) progress = "In Progress";
            else progress = "Completed";
            mProgressTxtVw.setText(progress);
            mTimeTxtVw.setText(mCursor.getString(Recipe.COL_TIME));
            mServingsTxtVw.setText(mCursor.getString(Recipe.COL_SERVINGS));
            mCursor.close();
            mCursor = null;
        }
    }

    private void handleIngredients(LinearLayout linearLayout, ArrayList<Ingredients.Ingredient> ingredients) {
        linearLayout.removeAllViews();

        if (ingredients == null || ingredients.isEmpty()) {
            addEmptyTextViewToLinearLayout(mIngredientsLinLt);
        } else {
            for (Ingredients.Ingredient ingredient : ingredients) {
                addTextViewToLinearLayout(mIngredientsLinLt, ingredient.ingredient);
            }
        }
    }

    private void handleSteps(LinearLayout linearLayout, ArrayList<Steps.Step> steps) {
        linearLayout.removeAllViews();

        if (steps == null || steps.isEmpty()) {
            addEmptyTextViewToLinearLayout(mStepsLinLt);
        } else {
            for (Steps.Step step : steps) {
                addTextViewToLinearLayout(mStepsLinLt, step.method);
            }
        }
    }

    private void addTextViewToLinearLayout(LinearLayout linearLayout, String text) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = Util.setupTextView(
                this,
                layoutParams,
                text,
                getResources().getColor(R.color.textSecondary)
        );

        Util.addView(linearLayout, textView);
    }

    private void addEmptyTextViewToLinearLayout(LinearLayout linearLayout) {
        addTextViewToLinearLayout(linearLayout, "");
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY + "=?";
        String[] selectionArgs = {mRecipeFirebaseKey};

        return new CursorLoader(
                this,
                RecipistContract.RecipeEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;

        if (mCursor != null && mCursor.moveToFirst()) {
            handleOfflineBinding();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }
}