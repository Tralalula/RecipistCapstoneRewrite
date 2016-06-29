// https://github.com/firebase/friendlypix/blob/master/android/app/src/main/java/com/google/firebase/samples/apps/friendlypix/NewPostActivity.java

package com.example.tobias.recipist.activity.recipe;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.BaseActivity;
import com.example.tobias.recipist.callback.TaskCallback;
import com.example.tobias.recipist.fragment.CreateRecipeUploadTaskFragment;
import com.example.tobias.recipist.model.Ingredients;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.model.Steps;
import com.example.tobias.recipist.util.FirebaseUtil;
import com.example.tobias.recipist.util.PicassoUtil;
import com.example.tobias.recipist.util.Util;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Tobias on 23-06-2016.
 */
public class CreateRecipeActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, TaskCallback, View.OnClickListener {
    public static String TAG = CreateRecipeActivity.class.getSimpleName();

    public static final String KEY_EDIT_RECIPE = "EDIT RECIPE";
    public static final String KEY_RECIPE_FIREBASE_KEY = "RECIPE FIREBASE KEY";
    public static final String KEY_FILE_URI = "FILE URI";
    public static final String KEY_RESIZED_BITMAP = "RESIZED BITMAP";
    public static final String KEY_THUMBNAIL_BITMAP = "THUMBNAIL BITMAP";
    public static final String KEY_SAVE_INGREDIENTS = "SAVE INGREDIENTS";
    public static final String KEY_SAVE_STEPS = "SAVE STEPS";
    public static final String KEY_EDITING = TAG + "EDITING";

    private static final String[] cameraPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 102;

    private static final int FULL_SIZE_MAX_DIMENSION = 1280;
    private static final int THUMBNAIL_MAX_DIMENSION = 120;

    @BindView(R.id.create_recipe_collapsing_toolbar_layout) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.create_recipe_toolbar) Toolbar mToolbar;
    @BindView(R.id.create_recipe_image_view_image) ImageView mPhotoImageView;
    @BindView(R.id.create_recipe_edit_text_title) EditText mTitleEditText;
    @BindView(R.id.create_recipe_switch_publish) Switch mPublishSwitch;
    @BindView(R.id.create_recipe_edit_text_time) EditText mTimeEditText;
    @BindView(R.id.create_recipe_edit_text_servings) EditText mServingsEditText;
    @BindView(R.id.create_recipe_button_edit_ingredients) Button mEditIngredientsBtn;
    @BindView(R.id.create_recipe_linear_layout_ingredients_list) LinearLayout mIngredientsLinearLayout;
    @BindView(R.id.create_recipe_button_edit_steps) Button mEditStepsBtn;
    @BindView(R.id.create_recipe_linear_layout_steps_list) LinearLayout mStepsLinearLayout;
    @BindView(R.id.create_recipe_floating_action_button_submit) FloatingActionButton mSubmitFab;

    private ArrayList<Ingredients.Ingredient> mIngredients;
    private ArrayList<Steps.Step> mSteps;

    private Uri mFileUri;
    private Bitmap mResizedBitmap;
    private Bitmap mThumbnailBitmap;
    private boolean mNewImage;

    private CreateRecipeUploadTaskFragment mUploadTaskFragment;

    private Recipe mOldRecipe;

    private boolean mEditing;
    private String mRecipeFirebaseKey;

    private String mReturnFirebaseKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);
        ButterKnife.bind(this);

        // Setup Toolbar.
        setSupportActionBar(mToolbar);

        // Find the retained fragment on activity restarts.
        FragmentManager fragmentManager = getSupportFragmentManager();
        mUploadTaskFragment = (CreateRecipeUploadTaskFragment) fragmentManager.findFragmentByTag(CreateRecipeUploadTaskFragment.TAG);

        // Create the fragment and data the first time.
        if (mUploadTaskFragment == null) {
            mUploadTaskFragment = new CreateRecipeUploadTaskFragment();
            fragmentManager.beginTransaction().add(mUploadTaskFragment, CreateRecipeUploadTaskFragment.TAG).commit();
        }

        // Editing recipe
        Intent data = getIntent();
        Recipe recipe = data.getParcelableExtra(KEY_EDIT_RECIPE);
        mRecipeFirebaseKey = data.getStringExtra(KEY_RECIPE_FIREBASE_KEY);
        if (recipe != null && mRecipeFirebaseKey != null) {
            mOldRecipe = recipe;
            mEditing = true;
            editRecipe(recipe);
        }

        // Set click listeners.
        mCollapsingToolbarLayout.setOnClickListener(this);
        mToolbar.setOnClickListener(this);
        mPhotoImageView.setOnClickListener(this);
        mSubmitFab.setOnClickListener(this);
        mEditIngredientsBtn.setOnClickListener(this);
        mEditStepsBtn.setOnClickListener(this);

        if (mIngredients == null) {
            mIngredients = new ArrayList<>();
            updateIngredients();
        }

        if (mSteps == null) {
            mSteps = new ArrayList<>();
            updateSteps();
        }

        Bitmap selectedBitmap = mUploadTaskFragment.getSelectedBitmap();
        if (selectedBitmap != null) {
            mPhotoImageView.setImageBitmap(selectedBitmap);
            mResizedBitmap = selectedBitmap;
        }

        Bitmap thumbnail = mUploadTaskFragment.getThumbnail();
        if (thumbnail != null) mThumbnailBitmap = thumbnail;
    }

    private void editRecipe(Recipe recipe) {
        String imageUrl = recipe.fullSizeImageUrl;
        String title = recipe.title;
        boolean publish = false;
        if (recipe.publish == 1) publish = true;
        String time = recipe.time;
        String servings = recipe.servings;

        mIngredients = recipe.ingredients;
        mSteps = recipe.steps;

        PicassoUtil.loadImage(imageUrl, mPhotoImageView);

        if (!Util.isNullOrEmpty(title)) mTitleEditText.setText(title);
        mPublishSwitch.setChecked(publish);
        if (!Util.isNullOrEmpty(time)) mTimeEditText.setText(time);
        if (!Util.isNullOrEmpty(servings)) mServingsEditText.setText(servings);

//        mResizedBitmap

        updateIngredients();
        updateSteps();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mEditing = savedInstanceState.getBoolean(KEY_EDITING);
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mResizedBitmap = savedInstanceState.getParcelable(KEY_RESIZED_BITMAP);
            mThumbnailBitmap = savedInstanceState.getParcelable(KEY_THUMBNAIL_BITMAP);
            mIngredients = savedInstanceState.getParcelableArrayList(KEY_SAVE_INGREDIENTS);
            mSteps = savedInstanceState.getParcelableArrayList(KEY_SAVE_STEPS);

            if (mResizedBitmap != null) mPhotoImageView.setImageBitmap(mResizedBitmap);
            updateIngredients();
            updateSteps();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_EDITING, mEditing);
        outState.putParcelable(KEY_FILE_URI, mFileUri);
        outState.putParcelable(KEY_RESIZED_BITMAP, mResizedBitmap);
        outState.putParcelable(KEY_THUMBNAIL_BITMAP, mThumbnailBitmap);
        outState.putParcelableArrayList(KEY_SAVE_INGREDIENTS, mIngredients);
        outState.putParcelableArrayList(KEY_SAVE_STEPS, mSteps);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        // Store the data in the fragment
        if (mResizedBitmap != null) mUploadTaskFragment.setSelectedBitmap(mResizedBitmap);
        if (mThumbnailBitmap != null) mUploadTaskFragment.setThumbnail(mThumbnailBitmap);

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_IMAGE:
                    boolean isCamera;

                    if (data == null) {
                        isCamera = true;
                    } else {
                        isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
                    }

                    if (!isCamera) mFileUri = data.getData();

                    Log.d(TAG, "Received file uri: " + mFileUri.getPath());

                    mUploadTaskFragment.resizeBitmap(mFileUri, THUMBNAIL_MAX_DIMENSION);
                    mUploadTaskFragment.resizeBitmap(mFileUri, FULL_SIZE_MAX_DIMENSION);
                    break;
                case CreateRecipeItemsActivity.REQUEST_CODE_INGREDIENTS:
                    mIngredients = data.getParcelableArrayListExtra(CreateRecipeItemsActivity.KEY_INGREDIENTS);
                    updateIngredients();
                    break;
                case CreateRecipeItemsActivity.REQUEST_CODE_STEPS:
                    mSteps = data.getParcelableArrayListExtra(CreateRecipeItemsActivity.KEY_STEPS);
                    updateSteps();
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_recipe_collapsing_toolbar_layout:
            case R.id.create_recipe_toolbar:
            case R.id.create_recipe_image_view_image:
                selectImage();
                break;
            case R.id.create_recipe_floating_action_button_submit:
                submitRecipe();
                break;
            case R.id.create_recipe_button_edit_ingredients:
                editIngredients();
                break;
            case R.id.create_recipe_button_edit_steps:
                editSteps();
                break;
        }
    }

    private void updateIngredients() {
        mIngredientsLinearLayout.removeAllViews();
        if (mIngredients == null || mIngredients.isEmpty()) {
            Util.addRecipeItemTextViewToLinearLayout(this, mIngredientsLinearLayout, getString(R.string.vc_recipe_no_ingredients_added_yet));
        } else {
            for (Ingredients.Ingredient ingredient : mIngredients) {
                Util.addRecipeItemTextViewToLinearLayout(this, mIngredientsLinearLayout, ingredient.ingredient);
            }
        }
    }

    private void updateSteps() {
        mStepsLinearLayout.removeAllViews();
        if (mSteps == null || mSteps.isEmpty()) {
            Util.addRecipeItemTextViewToLinearLayout(this, mStepsLinearLayout, getString(R.string.vc_recipe_no_steps_added_yet));
        } else {
            for (int i = 0; i < mSteps.size(); i++) {
                String text = "<strong>" + (i + 1) + ".</strong> " + mSteps.get(i).method;
                if (i != mSteps.size() - 1) text += "<br>";

                Util.addRecipeItemTextViewToLinearLayout(this, mStepsLinearLayout, Html.fromHtml(text));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Util.TOOLBAR_NAVIGATION_ICON_CLICK_ID:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @AfterPermissionGranted(REQUEST_CODE_CAMERA_PERMISSIONS)
    private void selectImage() {
        // Check for camera permissions.
        if (!EasyPermissions.hasPermissions(this, cameraPermissions)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.create_recipe_camera_permission_message),
                    REQUEST_CODE_CAMERA_PERMISSIONS, cameraPermissions
            );
            return;
        }

        // Choose file storage location.
        File file = new File(getExternalCacheDir(), UUID.randomUUID().toString());
        mFileUri = Uri.fromFile(file);

        // Camera
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> cameraList = packageManager.queryIntentActivities(captureIntent, 0);

        for (ResolveInfo resolveInfo : cameraList) {
            final String packageName = resolveInfo.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);

            intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            cameraIntents.add(intent);
        }

        // Image Picker
        Intent pickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent chooserIntent = Intent.createChooser(pickerIntent, getString(R.string.create_recipe_choose_image));
        chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[cameraIntents.size()])
        );
        startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
    }

    private void submitRecipe() {
        if (FirebaseUtil.getCurrentUser() == null) {
            Toast.makeText(CreateRecipeActivity.this,
                    getString(R.string.sign_in_first),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (mResizedBitmap == null && !mEditing) {
            Toast.makeText(CreateRecipeActivity.this,
                    getString(R.string.create_recipe_select_image_first),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String title = mTitleEditText.getText().toString();
        String time = mTimeEditText.getText().toString();
        String servings = mServingsEditText.getText().toString();

        if (TextUtils.isEmpty(title)) {
            mTitleEditText.setError(getString(R.string.required));
            return;
        }

        if (TextUtils.isEmpty(time)) {
            mTimeEditText.setError(getString(R.string.required));
            return;
        }

        if (TextUtils.isEmpty(servings)) {
            mServingsEditText.setError(getString(R.string.required));
            return;
        }

        if (mIngredients == null || mIngredients.isEmpty()) {
            Toast.makeText(CreateRecipeActivity.this,
                    getString(R.string.create_recipe_add_an_ingredient),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (mSteps == null || mSteps.isEmpty()) {
            Toast.makeText(CreateRecipeActivity.this,
                    getString(R.string.create_recipe_add_a_step),
                    Toast.LENGTH_SHORT).show();
            return;
        }


        showProgressDialog();
        mSubmitFab.setEnabled(true);

        int publish = 0;
        if (mPublishSwitch.isChecked()) publish = 1;

        if (mEditing && !mNewImage) {
            mUploadTaskFragment.uploadRecipe(
                    mOldRecipe.fullSizeImageUrl,
                    mOldRecipe.fullSizeImageStorageUrl,
                    mOldRecipe.thumbnailImageUrl,
                    mOldRecipe.thumbnailImageStorageUrl,
                    title,
                    publish,
                    time,
                    servings,
                    mIngredients,
                    mSteps,
                    mEditing,
                    mRecipeFirebaseKey);
        } else {
            Long timestamp = System.currentTimeMillis();

            String bitmapPath = "/" + FirebaseUtil.getCurrentUserId() + "/" + getString(R.string.create_recipe_firebase_storage_full_dir) + "/" + timestamp.toString() + "/";
            String thumbnailPath = "/" + FirebaseUtil.getCurrentUserId() + "/" + getString(R.string.create_recipe_firebase_storage_thumb_dir) + "/" + timestamp.toString() + "/";

            mUploadTaskFragment.uploadRecipe(
                    mResizedBitmap,
                    bitmapPath,
                    mThumbnailBitmap,
                    thumbnailPath,
                    mFileUri.getLastPathSegment(),
                    title,
                    publish,
                    time,
                    servings,
                    mIngredients,
                    mSteps,
                    mEditing,
                    mRecipeFirebaseKey
            );
        }
    }

    private void editIngredients() {
        Intent data = new Intent(this, CreateRecipeItemsActivity.class);
        data.putExtra(CreateRecipeItemsActivity.KEY_TYPE, CreateRecipeItemsActivity.TYPE_INGREDIENTS);
        data.putParcelableArrayListExtra(CreateRecipeItemsActivity.KEY_INGREDIENTS, mIngredients);
        startActivityForResult(data, CreateRecipeItemsActivity.REQUEST_CODE_INGREDIENTS);
    }

    private void editSteps() {
        Intent data = new Intent(this, CreateRecipeItemsActivity.class);
        data.putExtra(CreateRecipeItemsActivity.KEY_TYPE, CreateRecipeItemsActivity.TYPE_STEPS);
        data.putParcelableArrayListExtra(CreateRecipeItemsActivity.KEY_STEPS, mSteps);
        startActivityForResult(data, CreateRecipeItemsActivity.REQUEST_CODE_STEPS);
    }

    @Override
    public void onBitmapResized(Bitmap resizedBitmap, int maxDimension) {
        if (resizedBitmap == null) {
            Log.e(TAG, "Couldn't resize bitmap in background task");
            Toast.makeText(CreateRecipeActivity.this,
                    getString(R.string.create_recipe_couldnt_resize_bitmap),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (maxDimension == THUMBNAIL_MAX_DIMENSION) {
            mThumbnailBitmap = resizedBitmap;
        } else if (maxDimension == FULL_SIZE_MAX_DIMENSION) {
            mResizedBitmap = resizedBitmap;
            mPhotoImageView.setImageBitmap(mResizedBitmap);
        }

        if (mThumbnailBitmap != null && mResizedBitmap != null) {
            mSubmitFab.setEnabled(true);
        }

        mNewImage = true;
    }

    @Override
    public void onRecipeUploaded(final String error, final String firebaseKey) {
        CreateRecipeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSubmitFab.setEnabled(true);
                hideProgressDialog();
                if (error == null) {
                    mReturnFirebaseKey = firebaseKey;
                    if (mEditing) {
                        Toast.makeText(CreateRecipeActivity.this,
                                getString(R.string.create_recipe_recipe_edited),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CreateRecipeActivity.this,
                                getString(R.string.create_recipe_recipe_created),
                                Toast.LENGTH_SHORT).show();
                    }

                    Intent data = new Intent(CreateRecipeActivity.this, ViewRecipeActivity.class);
                    data.putExtra(ViewRecipeActivity.KEY_RECIPE_FIREBASE_KEY, mReturnFirebaseKey);
                    data.putExtra(ViewRecipeActivity.KEY_TYPE, ViewRecipeActivity.TYPE_OFFLINE);
                    startActivity(data);
                    finish();
                } else {
                    Toast.makeText(CreateRecipeActivity.this,
                            error,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
