package com.example.tobias.recipist.activity.recipe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.BaseActivity;
import com.example.tobias.recipist.data.RecipistContract;
import com.example.tobias.recipist.data.RecipistDbHandler;
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

    private static final int KEY_RECIPE_LOADER = 0;
    private static final int KEY_INGREDIENTS_LOADER = 1;
    private static final int KEY_STEPS_LOADER = 2;

    @BindView(R.id.view_recipe_toolbar) Toolbar mToolbar;
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

    private ArrayList<Ingredients.Ingredient> mIngredients;
    private ArrayList<Steps.Step> mSteps;

    private String mCurrentType;

    private Cursor mCursor;
    private long mRecipeId;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

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
            getSupportLoaderManager().initLoader(KEY_RECIPE_LOADER, null, this);
            getSupportLoaderManager().initLoader(KEY_INGREDIENTS_LOADER, null, this);
            getSupportLoaderManager().initLoader(KEY_STEPS_LOADER, null, this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_recipe, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_recipe_action_remove:
                removeRecipe(this).show();
                return true;
            case Util.TOOLBAR_NAVIGATION_ICON_CLICK_ID:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_recipe_floating_action_button_edit:
                editRecipe();
                break;
        }
    }

    private void editRecipe() {
        if (mCurrentType.equals(TYPE_OFFLINE)) {
            mRecipe.ingredients = mIngredients;
            mRecipe.steps = mSteps;
        }

        Intent data = new Intent(ViewRecipeActivity.this, CreateRecipeActivity.class);
        data.putExtra(CreateRecipeActivity.KEY_EDIT_RECIPE, mRecipe);
        data.putExtra(CreateRecipeActivity.KEY_RECIPE_FIREBASE_KEY, mRecipeFirebaseKey);
        startActivity(data);
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

            mRecipe = new Recipe(mCursor);
            mCursor.close();
            mCursor = null;
        }
    }

    private void handleOfflineIngredients() {
        ArrayList<Ingredients.Ingredient> ingredients = new ArrayList<>();
        do {
            ingredients.add(new Ingredients.Ingredient(mCursor));
        } while (mCursor.moveToNext());

        handleIngredients(mIngredientsLinLt, ingredients);
        mIngredients = ingredients;

        mCursor.close();
        mCursor = null;
    }

    private void handleOfflineSteps() {
        ArrayList<Steps.Step> steps = new ArrayList<>();
        do {
            steps.add(new Steps.Step(mCursor));
        } while (mCursor.moveToNext());

        handleSteps(mStepsLinLt, steps);
        mSteps = steps;

        mCursor.close();
        mCursor = null;
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
        String[] selectionArgs = {mRecipeFirebaseKey};

        switch (id) {
            case KEY_RECIPE_LOADER:
                String recipeSelection = RecipistContract.RecipeEntry.COLUMN_FIREBASE_KEY + "=?";
                return new CursorLoader(
                        this,
                        RecipistContract.RecipeEntry.CONTENT_URI,
                        null,
                        recipeSelection,
                        selectionArgs,
                        null
                );
            case KEY_INGREDIENTS_LOADER:
                String ingredientsSelection = RecipistContract.IngredientEntry.COLUMN_RECIPE_FIREBASE_KEY + "=?";
                return new CursorLoader(
                        this,
                        RecipistContract.IngredientEntry.CONTENT_URI,
                        null,
                        ingredientsSelection,
                        selectionArgs,
                        null
                );
            case KEY_STEPS_LOADER:
                String stepsSelection = RecipistContract.StepEntry.COLUMN_RECIPE_FIREBASE_KEY + "=?";
                return new CursorLoader(
                        this,
                        RecipistContract.StepEntry.CONTENT_URI,
                        null,
                        stepsSelection,
                        selectionArgs,
                        null
                );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        switch (loader.getId()) {
            case KEY_RECIPE_LOADER:
                if (mCursor != null && mCursor.moveToFirst()) {
                    handleOfflineBinding();
                }
                break;
            case KEY_INGREDIENTS_LOADER:
                if (mCursor != null && mCursor.moveToFirst()) {
                    handleOfflineIngredients();
                }
                break;
            case KEY_STEPS_LOADER:
                if (mCursor != null && mCursor.moveToFirst()) {
                    handleOfflineSteps();
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

    private AlertDialog removeRecipe(Context context) {
        AlertDialog removeRecipeDialogBox = new AlertDialog.Builder(context)
                .setTitle("Remove Recipe")
                .setMessage("Are you sure you want to remove the recipe \"" + mRecipe.title + "\" ?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        deleteRecipe();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        return removeRecipeDialogBox;
    }

    private void deleteRecipe() {
        new RecipistDbHandler(this).deleteRecipe(mRecipeFirebaseKey);
        FirebaseUtil.getRecipesRef().child(mRecipeFirebaseKey).setValue(null);
    }
}