package com.example.tobias.recipist.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.recipe.ViewRecipeActivity;
import com.example.tobias.recipist.data.RecipistContract;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.util.Util;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by Tobias on 26-06-2016.
 */
@SuppressLint("NewApi")
public class DataProviderWidget implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;

    public DataProviderWidget(Context context, Intent intent) {
        mContext = context;
    }

    private void initData() {
        final long token = Binder.clearCallingIdentity();
        try {
            if (mCursor != null) mCursor.close();

            mCursor = mContext.getContentResolver().query(
                    RecipistContract.RecipeEntry.CONTENT_URI,
                    Recipe.RECIPE_COLUMNS,
                    null,
                    null,
                    null
            );
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        Log.e("DataProviderWdiget", "mCursor = " + mCursor);
        if (i == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(i)) {
            return null;
        }


        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

        Log.e("DataProviderWdiget", "mCursor.getString(Recipe.COL_TITLE) = " + mCursor.getString(Recipe.COL_TITLE));
        remoteViews.setTextViewText(R.id.widget_item_text_view_title, mCursor.getString(Recipe.COL_TITLE));
        remoteViews.setTextViewText(R.id.widget_item_text_view_publish, String.valueOf(mCursor.getString(Recipe.COL_PUBLISH)));
        remoteViews.setTextViewText(R.id.widget_item_text_view_time, Util.formatTime(Integer.parseInt(mCursor.getString(Recipe.COL_TIME))));

        try {
            Bitmap thumbnail = Picasso.with(mContext).load(mCursor.getString(Recipe.COL_THUMBNAIL_URL)).get();
            remoteViews.setImageViewBitmap(R.id.widget_item_image_view_thumbnail, thumbnail);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent launchIntent = new Intent(mContext, ViewRecipeActivity.class);
        launchIntent.putExtra(ViewRecipeActivity.KEY_RECIPE_FIREBASE_KEY, mCursor.getString(Recipe.COL_FIREBASE_KEY));
        launchIntent.putExtra(ViewRecipeActivity.KEY_RECIPE_OFFLINE_ID, mCursor.getInt(Recipe.COL_ID));
        launchIntent.putExtra(ViewRecipeActivity.KEY_TYPE, ViewRecipeActivity.TYPE_OFFLINE);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, i, launchIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_item_relative_layout_root_container, pendingIntent);

//        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), android.R.layout.simple_list_item_1);
//        remoteViews.setTextViewText(android.R.id.text1, mCursor.getString(Recipe.COL_TITLE));
//        remoteViews.setTextColor(android.R.id.text1, Color.BLACK);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        if (mCursor.moveToPosition(i)) return mCursor.getLong(Recipe.COL_ID);
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
