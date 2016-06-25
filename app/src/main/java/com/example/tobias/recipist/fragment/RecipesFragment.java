package com.example.tobias.recipist.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.recipe.ViewRecipeActivity;
import com.example.tobias.recipist.model.Recipe;
import com.example.tobias.recipist.util.FirebaseUtil;
import com.example.tobias.recipist.util.Util;
import com.example.tobias.recipist.viewholder.RecipeViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by Tobias on 24-06-2016.
 */
public class RecipesFragment extends Fragment {
    public static final String TAG = RecipesFragment.class.getSimpleName();

    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_LAYOUT_POSITION = "LAYOUT POSITION";

    public static final int TYPE_OVERVIEW = 1357;
    public static final int TYPE_MY = 2468;

    private RecyclerView mRecyclerView;

    private int mRecyclerViewPosition;
    private FirebaseRecyclerAdapter<Recipe, RecipeViewHolder> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipes, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recipes_recycler_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        if (savedInstanceState != null) {
            mRecyclerViewPosition = (int) savedInstanceState.getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);
        }

        switch (getArguments().getInt(KEY_TYPE)) {
            case TYPE_OVERVIEW:
                handleOverviewLayout();
                break;
            case TYPE_MY:
                handleMyLayout();
                break;
            default:
                throw new RuntimeException("Illegal recipe fragment type specified.");
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        int recyclerViewScrollPosition = getRecyclerViewScrollPosition();
        Log.d(TAG, "RecyclerView scroll position: " + recyclerViewScrollPosition);
        savedInstanceState.putSerializable(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) mAdapter.cleanup();
    }

    public static RecipesFragment newInstance(int type) {
        RecipesFragment fragment = new RecipesFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_TYPE, type);
        fragment.setArguments(arguments);
        return fragment;
    }

    private int getRecyclerViewScrollPosition() {
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
            return ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        return 0;
    }

    private void handleOverviewLayout() {
        Query recipesQuery = FirebaseUtil.getRecipesRef();

        System.out.println("QUERY QUERY 1 " + recipesQuery.toString());

        mAdapter = new FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(Recipe.class,
                R.layout.recipe_item, RecipeViewHolder.class, recipesQuery) {
            @Override
            protected void populateViewHolder(RecipeViewHolder viewHolder, Recipe model, int position) {
                final DatabaseReference recipeRef = getRef(position);
                final String recipeKey = recipeRef.getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "handleOverviewLayout: populateViewHolder: onClick: recipeKey: " + recipeKey);
                        Intent data = new Intent(getActivity(), ViewRecipeActivity.class);
                        data.putExtra(ViewRecipeActivity.KEY_RECIPE_FIREBASE_KEY, recipeKey);
                        startActivity(data);
                    }
                });

                viewHolder.bindToRecipe(mRecyclerView.getContext(), model);
            }
        };
    }

    private void handleMyLayout() {
        String currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId != null) {
            Query recipesQuery2 = FirebaseUtil.getUsersRecipesQuery();

            mAdapter = new FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(Recipe.class,
                    R.layout.recipe_item, RecipeViewHolder.class, recipesQuery2) {
                @Override
                protected void populateViewHolder(RecipeViewHolder viewHolder, Recipe model, int position) {
                    final DatabaseReference recipeRef = getRef(position);
                    final String recipeKey = recipeRef.getKey();

                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "handleOverviewLayout: populateViewHolder: onClick: recipeKey: " + recipeKey);
                            Intent data = new Intent(getActivity(), ViewRecipeActivity.class);
                            data.putExtra(ViewRecipeActivity.KEY_RECIPE_FIREBASE_KEY, recipeKey);
                            startActivity(data);
                        }
                    });

                    viewHolder.bindToRecipe(mRecyclerView.getContext(), model);
                }
            };
        }
    }
}
