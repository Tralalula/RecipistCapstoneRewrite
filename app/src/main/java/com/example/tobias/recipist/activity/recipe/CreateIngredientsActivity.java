package com.example.tobias.recipist.activity.recipe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.BaseActivity;
import com.example.tobias.recipist.model.Ingredients;
import com.example.tobias.recipist.util.Util;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Tobias on 25-06-2016.
 */
public class CreateIngredientsActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = CreateIngredientsActivity.class.getSimpleName();

    public static final String KEY_INGREDIENTS = "INGREDIENTS";
    public static final String KEY_EDITTEXTS = "EDITTEXTS";
    public static final String KEY_SORTING = "SORTING";
    public static final String KEY_SORTING_SAVED_STATE = "SORTING SAVED STATE";

    public static final int REQUEST_CODE_INGREDIENTS = 2357;

    private ArrayList<Ingredients.Ingredient> mOldIngredients = new ArrayList<>();
    private ArrayList<Ingredients.Ingredient> mNewIngredients = new ArrayList<>();

    private Menu mMenu;

    @BindView(R.id.create_ingredients_toolbar) Toolbar mToolbar;
    @BindView(R.id.create_ingredients_drag_linear_layout) DragLinearLayout mIngredientsDll;
    @BindView(R.id.create_ingredients_app_compat_button_add_ingredient) Button mAddIngredientsBtn;
    @BindView(R.id.create_ingredients_floating_action_button_save) FloatingActionButton mSaveFab;

    private boolean mSorting;
    private boolean mSortingSavedSate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ingredients);
        ButterKnife.bind(this);

        // Setup Toolbar.
        setSupportActionBar(mToolbar);

        if (!mSortingSavedSate) mSorting = false; // Ingredients shouldn't be sortable at start.

        // Set click listeners.
        mAddIngredientsBtn.setOnClickListener(this);
        mSaveFab.setOnClickListener(this);

        Intent data = getIntent();
        if (data != null) {
            ArrayList<Ingredients.Ingredient> ingredients = data.getParcelableArrayListExtra(KEY_INGREDIENTS);
            setupIngredients(ingredients);
        }

        handleIngredients();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<EditText> editTexts = (ArrayList<EditText>) savedInstanceState.getSerializable(KEY_EDITTEXTS);
        mIngredientsDll.removeAllViews();
        if (editTexts != null) {
            for (EditText editText : editTexts) {
                ((ViewGroup) editText.getParent()).removeView(editText);
                Util.addView(mIngredientsDll, editText);
            }
        }

        mSorting = savedInstanceState.getBoolean(KEY_SORTING);
        mSortingSavedSate = savedInstanceState.getBoolean(KEY_SORTING_SAVED_STATE);
        handleIngredients();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<EditText> editTexts = new ArrayList<>();
        int count = mIngredientsDll.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mIngredientsDll.getChildAt(i);
            editTexts.add((EditText) child);
        }
        outState.putSerializable(KEY_EDITTEXTS, editTexts);
        outState.putBoolean(KEY_SORTING, mSorting);

        outState.putBoolean(KEY_SORTING_SAVED_STATE, true);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_ingredients, menu);
        mMenu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_ingredients_action_sort:
                changeMode();
                handleIngredients();
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
            case R.id.create_ingredients_app_compat_button_add_ingredient:
                addEmptyEditTextToDragLinearLayout();
                handleIngredients();
                break;
            case R.id.create_ingredients_floating_action_button_save:
                saveIngredients();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(KEY_INGREDIENTS, mOldIngredients);
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }

    private void saveIngredients() {
        Intent data = new Intent();
        mNewIngredients = new ArrayList<>();

        int count = mIngredientsDll.getChildCount();
        for (int i = 0; i < count; i++) {
            EditText child = (EditText) mIngredientsDll.getChildAt(i);
            if (!Util.isEditTextEmpty(child)) {
                mNewIngredients.add(new Ingredients.Ingredient(child.getText().toString()));
            }
        }

        data.putParcelableArrayListExtra(KEY_INGREDIENTS, mNewIngredients);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void setupIngredients(ArrayList<Ingredients.Ingredient> ingredients) {
        if (!ingredients.isEmpty()) {
            // Store received ingredients, in case user decides not to change ingredients anyways.
            mOldIngredients = ingredients;
            for (Ingredients.Ingredient ingredient : ingredients) {
                addEditTextToDragLinearLayout(ingredient.ingredient);
            }
        } else {
            addEmptyEditTextToDragLinearLayout();
        }
    }

    private void handleIngredients() {
        int count = mIngredientsDll.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mIngredientsDll.getChildAt(i);
            EditText editText = (EditText) child;

            if (mSorting) {
                Util.addDrawableToTheRight(editText, getDrawable(R.drawable.ic_swap_vertical_black_24dp));
                mIngredientsDll.setViewDraggable(child, child);
            } else {
                Util.addDrawableToTheRight(editText, getDrawable(R.drawable.ic_delete_black_24dp));
                Util.makeChildDeletableByClickingOnRightDrawalbe(mIngredientsDll, editText);
            }
        }
    }

    private void changeMode() {
        if (mSorting) {
            mMenu.getItem(0).setIcon(getDrawable(R.drawable.ic_sort_black_24dp));
            mSorting = false;
        } else {
            mMenu.getItem(0).setIcon(getDrawable(R.drawable.ic_check_white_24dp));
            mSorting = true;
        }
    }

    private void addEditTextToDragLinearLayout(String text) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText editText = Util.setupEditText(
                this,
                layoutParams,
                text,
                1,
                InputType.TYPE_CLASS_TEXT,
                "Hinty Hint"
        );

        Util.addView(mIngredientsDll, editText);
    }

    private void addEmptyEditTextToDragLinearLayout() {
        addEditTextToDragLinearLayout("");
    }
}
