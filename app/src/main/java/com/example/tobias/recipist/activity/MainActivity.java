package com.example.tobias.recipist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.auth.GoogleSignInActivity;
import com.example.tobias.recipist.activity.recipe.CreateRecipeActivity;
import com.example.tobias.recipist.adapter.MainPageAdapter;
import com.example.tobias.recipist.fragment.RecipesFragment;
import com.example.tobias.recipist.task.FetchMyRecipesTask;
import com.example.tobias.recipist.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private MainPageAdapter mMainPageAdapter;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize & setup Toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Initialize & setup MainPageAdapter.
        mMainPageAdapter = new MainPageAdapter(getSupportFragmentManager());
        mMainPageAdapter.addFragment(RecipesFragment.newInstance(RecipesFragment.TYPE_OVERVIEW), "overview");
        mMainPageAdapter.addFragment(RecipesFragment.newInstance(RecipesFragment.TYPE_MY), "my recipes");

        // Initialize views.
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.main_tab_layout);

        // Setup views.
        mViewPager.setAdapter(mMainPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        new FetchMyRecipesTask(this).execute();

        if (FirebaseUtil.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
            startActivity(intent);
        }
    }
}