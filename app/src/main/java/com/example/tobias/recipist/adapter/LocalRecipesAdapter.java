package com.example.tobias.recipist.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.recipe.ViewRecipeActivity;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.util.PicassoUtil;
import com.example.tobias.recipist.util.Util;
import com.example.tobias.recipist.viewholder.RecipeViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Tobias on 25-06-2016.
 */
public class LocalRecipesAdapter extends RecyclerView.Adapter<RecipeViewHolder> {
    private ArrayList<String> firebaseKeys = new ArrayList<>();
    private ArrayList<Integer> recipeIds = new ArrayList<>();

    private View mView;
    private Context mContext;
    private Cursor mCursor;

    public LocalRecipesAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mView = LayoutInflater.from(mContext).inflate(R.layout.recipe_item, parent, false);

        return new RecipeViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(final RecipeViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        PicassoUtil.loadImage(mCursor.getString(Recipe.COL_THUMBNAIL_URL), holder.mRecipeThumbnail);

        holder.mRecipeTitle.setText(mCursor.getString(Recipe.COL_TITLE));
        holder.mRecipeServings.setText(mCursor.getString(Recipe.COL_SERVINGS));
        holder.mRecipeTime.setText(Util.formatTime(Integer.parseInt(mCursor.getString(Recipe.COL_TIME))));

        firebaseKeys.add(mCursor.getString(Recipe.COL_FIREBASE_KEY));
        recipeIds.add(mCursor.getInt(Recipe.COL_ID));

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent(mContext, ViewRecipeActivity.class);
                data.putExtra(ViewRecipeActivity.KEY_RECIPE_FIREBASE_KEY, firebaseKeys.get(holder.getAdapterPosition()));
                data.putExtra(ViewRecipeActivity.KEY_TYPE, ViewRecipeActivity.TYPE_OFFLINE);
                mContext.startActivity(data);
            }
        });
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
