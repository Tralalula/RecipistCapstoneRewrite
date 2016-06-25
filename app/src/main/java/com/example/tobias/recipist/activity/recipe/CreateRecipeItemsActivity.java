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
import com.example.tobias.recipist.model.Steps;
import com.example.tobias.recipist.util.Util;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Tobias on 25-06-2016.
 */
public class CreateRecipeItemsActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = CreateRecipeItemsActivity.class.getSimpleName();

    public static final String TYPE_INGREDIENTS = "TYPE INGREDIENTS";
    public static final String TYPE_STEPS = "TYPE STEPS";

    public static final String KEY_INGREDIENTS = "INGREDIENTS";
    public static final String KEY_STEPS = "STEPS";
    public static final String KEY_EDITTEXTS = "EDITTEXTS";
    public static final String KEY_SORTING = "SORTING";
    public static final String KEY_TYPE = "TYPE";

    public static final int REQUEST_CODE_INGREDIENTS = 2357;
    public static final int REQUEST_CODE_STEPS = 1113;

    private ArrayList<Ingredients.Ingredient> mOldIngredients = new ArrayList<>();
    private ArrayList<Ingredients.Ingredient> mNewIngredients = new ArrayList<>();

    private ArrayList<Steps.Step> mOldSteps = new ArrayList<>();
    private ArrayList<Steps.Step> mNewSteps = new ArrayList<>();

    private Menu mMenu;

    @BindView(R.id.create_recipe_items_toolbar) Toolbar mToolbar;
    @BindView(R.id.create_recipe_items_drag_linear_layout) DragLinearLayout mItemsDll;
    @BindView(R.id.create_recipe_items_button_add_item) Button mAddItemBtn;
    @BindView(R.id.create_recipe_items_floating_action_button_save) FloatingActionButton mSaveFab;

    private boolean mSorting;
    private String mCurrentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe_items);
        ButterKnife.bind(this);

        // Setup Toolbar.
        setSupportActionBar(mToolbar);

        mSorting = savedInstanceState != null && savedInstanceState.getBoolean(KEY_SORTING);

        // Set click listeners.
        mAddItemBtn.setOnClickListener(this);
        mSaveFab.setOnClickListener(this);

        Intent data = getIntent();
        if (data != null) {
            mCurrentType = data.getStringExtra(CreateRecipeItemsActivity.KEY_TYPE);

            if (Objects.equals(mCurrentType, TYPE_INGREDIENTS)) {
                ArrayList<Ingredients.Ingredient> ingredients = data.getParcelableArrayListExtra(KEY_INGREDIENTS);
                setupIngredients(ingredients);
            } else if (Objects.equals(mCurrentType, TYPE_STEPS)) {
                ArrayList<Steps.Step> steps = data.getParcelableArrayListExtra(KEY_STEPS);
                setupSteps(steps);
            }
            handleItems();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<EditText> editTexts = (ArrayList<EditText>) savedInstanceState.getSerializable(KEY_EDITTEXTS);
        mItemsDll.removeAllViews();
        if (editTexts != null) {
            for (EditText editText : editTexts) {
                ((ViewGroup) editText.getParent()).removeView(editText);
                Util.addView(mItemsDll, editText);
            }
        }
        handleItems();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<EditText> editTexts = new ArrayList<>();
        int count = mItemsDll.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mItemsDll.getChildAt(i);
            editTexts.add((EditText) child);
        }
        outState.putSerializable(KEY_EDITTEXTS, editTexts);
        outState.putBoolean(KEY_SORTING, mSorting);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_recipe_items, menu);
        mMenu = menu;
        setIcon();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_recipe_items_action_sort:
                mSorting = !mSorting;
                setIcon();
                handleItems();
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
            case R.id.create_recipe_items_button_add_item:
                addEmptyEditTextToDragLinearLayout();
                handleItems();
                break;
            case R.id.create_recipe_items_floating_action_button_save:
                if (Objects.equals(mCurrentType, TYPE_INGREDIENTS)) saveIngredients();
                else if (Objects.equals(mCurrentType, TYPE_STEPS)) saveSteps();
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

        int count = mItemsDll.getChildCount();
        for (int i = 0; i < count; i++) {
            EditText child = (EditText) mItemsDll.getChildAt(i);
            if (!Util.isEditTextEmpty(child)) {
                mNewIngredients.add(new Ingredients.Ingredient(child.getText().toString()));
            }
        }

        data.putParcelableArrayListExtra(KEY_INGREDIENTS, mNewIngredients);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void saveSteps() {
        Intent data = new Intent();
        mNewSteps = new ArrayList<>();

        int count = mItemsDll.getChildCount();
        for (int i = 0; i < count; i++) {
            EditText child = (EditText) mItemsDll.getChildAt(i);
            if (!Util.isEditTextEmpty(child)) {
                mNewSteps.add(new Steps.Step(child.getText().toString()));
            }
        }

        data.putParcelableArrayListExtra(KEY_STEPS, mNewSteps);
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

    private void setupSteps(ArrayList<Steps.Step> steps) {
        if (!steps.isEmpty()) {
            // Store received steps, in case user decides not to change steps anyways.
            mOldSteps = steps;
            for (Steps.Step step : steps) {
                addEditTextToDragLinearLayout(step.method);
            }
        } else {
            addEmptyEditTextToDragLinearLayout();
        }
    }

    private void handleItems() {
        int count = mItemsDll.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mItemsDll.getChildAt(i);
            EditText editText = (EditText) child;

            if (mSorting) {
                Util.addDrawableToTheRight(editText, getDrawable(R.drawable.ic_swap_vertical_black_24dp));
                mItemsDll.setViewDraggable(child, child);
            } else {
                Util.addDrawableToTheRight(editText, getDrawable(R.drawable.ic_delete_black_24dp));
                Util.makeChildDeletableByClickingOnRightDrawalbe(mItemsDll, editText);
            }
        }
    }

    private void setIcon() {
        if (mSorting) {
            mMenu.getItem(0).setIcon(getDrawable(R.drawable.ic_check_white_24dp));
        } else {
            mMenu.getItem(0).setIcon(getDrawable(R.drawable.ic_sort_black_24dp));
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
                mCurrentType
        );

        Util.addView(mItemsDll, editText);
    }

    private void addEmptyEditTextToDragLinearLayout() {
        addEditTextToDragLinearLayout("");
    }
}
