package com.example.tobias.recipist.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.loader.RecipeLoader;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.viewholder.RecipeViewHolder;
import com.squareup.picasso.Picasso;

/**
 * Created by Tobias on 25-06-2016.
 */
public class LocalRecipesAdapter extends RecyclerView.Adapter<RecipeViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    public LocalRecipesAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        Picasso.with(mContext)
                .load(mCursor.getString(Recipe.COL_THUMBNAIL_URL))
                .into(holder.mRecipeThumbnail);

        holder.mRecipeTitle.setText(mCursor.getString(Recipe.COL_TITLE));
        holder.mRecipeProgress.setText(String.valueOf(mCursor.getInt(Recipe.COL_PROGRESS)));
        holder.mRecipeTime.setText(mCursor.getString(Recipe.COL_TIME));
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(Recipe.COL_ID);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
