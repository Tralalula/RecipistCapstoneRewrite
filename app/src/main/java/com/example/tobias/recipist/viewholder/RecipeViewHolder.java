package com.example.tobias.recipist.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.model.Recipe;
import com.squareup.picasso.Picasso;

/**
 * Created by Tobias on 24-06-2016.
 */
public class RecipeViewHolder extends RecyclerView.ViewHolder {
    public TextView mRecipeTitle;
    public ImageView mRecipeThumbnail;
    public TextView mRecipeProgress;
    public TextView mRecipeTime;

    public RecipeViewHolder(View itemView) {
        super(itemView);

        mRecipeTitle = (TextView) itemView.findViewById(R.id.recipe_title);
        mRecipeThumbnail = (ImageView) itemView.findViewById(R.id.recipe_thumbnail);
        mRecipeProgress = (TextView) itemView.findViewById(R.id.recipe_progress);
        mRecipeTime = (TextView) itemView.findViewById(R.id.recipe_time);
    }

    public void bindToRecipe(Context context, Recipe recipe) {
        String title = recipe.title;
        String thumbnailUrl = recipe.thumbnailImageUrl;
        String progress = String.valueOf(recipe.progress);
        String time = recipe.time;

        mRecipeTitle.setText(title);
        Picasso.with(context).load(thumbnailUrl).into(mRecipeThumbnail);
        mRecipeProgress.setText(progress);
        mRecipeTime.setText(time);
    }
}
