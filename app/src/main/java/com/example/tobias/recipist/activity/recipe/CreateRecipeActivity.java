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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.BaseActivity;
import com.example.tobias.recipist.callback.TaskCallback;
import com.example.tobias.recipist.fragment.CreateRecipeUploadTaskFragment;
import com.example.tobias.recipist.util.FirebaseUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Tobias on 23-06-2016.
 */
public class CreateRecipeActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, TaskCallback, View.OnClickListener {
    public static String TAG = CreateRecipeActivity.class.getSimpleName();

    private static final String[] cameraPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 102;

    private static final int FULL_SIZE_MAX_DIMENSION = 1280;
    private static final int THUMBNAIL_MAX_DIMENSION = 640;

    private ImageView mPhotoImgView;
    private FloatingActionButton mSubmitFab;

    private Uri mFileUri;
    private Bitmap mResizedBitmap;
    private Bitmap mThumbnailBitmap;

    private CreateRecipeUploadTaskFragment mUploadTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        // Initialize & setup Toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_recipe_toolbar);
        setSupportActionBar(toolbar);

        // Find the retained fragment on activity restarts.
        FragmentManager fragmentManager = getSupportFragmentManager();
        mUploadTaskFragment = (CreateRecipeUploadTaskFragment) fragmentManager.findFragmentByTag(CreateRecipeUploadTaskFragment.TAG);

        // Create the fragment and data the first time.
        if (mUploadTaskFragment == null) {
            mUploadTaskFragment = new CreateRecipeUploadTaskFragment();
            fragmentManager.beginTransaction().add(mUploadTaskFragment, CreateRecipeUploadTaskFragment.TAG).commit();
        }

        // Initialize views.
        mPhotoImgView = (ImageView) findViewById(R.id.create_recipe_image_view_image);
        mSubmitFab = (FloatingActionButton) findViewById(R.id.create_recipe_floating_action_button_submit);

        // Set click listeners.
        mPhotoImgView.setOnClickListener(this);
        mSubmitFab.setOnClickListener(this);

        Bitmap selectedBitmap = mUploadTaskFragment.getSelectedBitmap();
        if (selectedBitmap != null) {
            mPhotoImgView.setImageBitmap(selectedBitmap);
            mResizedBitmap = selectedBitmap;
        }

        Bitmap thumbnail = mUploadTaskFragment.getThumbnail();
        if (thumbnail != null) mThumbnailBitmap = thumbnail;
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

        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
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
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_recipe_image_view_image:
                pickImage();
                break;
            case R.id.create_recipe_floating_action_button_submit:
                submitRecipe();
                break;
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_CAMERA_PERMISSIONS)
    private void pickImage() {
        // Check for camera permissions.
        if (!EasyPermissions.hasPermissions(this, cameraPermissions)) {
            EasyPermissions.requestPermissions(this,
                    "This will upload a picture from your camera",
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

        Intent chooserIntent = Intent.createChooser(pickerIntent, "Choose Image");
        chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[cameraIntents.size()])
        );
        startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
    }

    private void submitRecipe() {
        if (mResizedBitmap == null) {
            Toast.makeText(CreateRecipeActivity.this,
                    "Select an image first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog();
        mSubmitFab.setEnabled(true);

        Long timestamp = System.currentTimeMillis();

        String bitmapPath = "/" + FirebaseUtil.getCurrentUserId() + "/full/" + timestamp.toString() + "/";
        String thumbnailPath = "/" + FirebaseUtil.getCurrentUserId() + "/thumb/" + timestamp.toString() + "/";
        mUploadTaskFragment.uploadRecipe(
                mResizedBitmap,
                bitmapPath,
                mThumbnailBitmap,
                thumbnailPath,
                mFileUri.getLastPathSegment()
        );
    }

    @Override
    public void onBitmapResized(Bitmap resizedBitmap, int maxDimension) {
        if (resizedBitmap == null) {
            Log.e(TAG, "Couldn't resize bitmap in background task");
            Toast.makeText(CreateRecipeActivity.this,
                    "Couldn't resize bitmap.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (maxDimension == THUMBNAIL_MAX_DIMENSION) {
            mThumbnailBitmap = resizedBitmap;
        } else if (maxDimension == FULL_SIZE_MAX_DIMENSION) {
            mResizedBitmap = resizedBitmap;
            mPhotoImgView.setImageBitmap(mResizedBitmap);
        }

        if (mThumbnailBitmap != null && mResizedBitmap != null) {
            mSubmitFab.setEnabled(true);
        }
    }

    @Override
    public void onRecipeUploaded(final String error) {
        CreateRecipeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSubmitFab.setEnabled(true);
                hideProgressDialog();
                if (error == null) {
                    Toast.makeText(CreateRecipeActivity.this,
                            "Recipe created!",
                            Toast.LENGTH_SHORT).show();
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
