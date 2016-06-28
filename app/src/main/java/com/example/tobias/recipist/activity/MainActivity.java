package com.example.tobias.recipist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.recipe.CreateRecipeActivity;
import com.example.tobias.recipist.adapter.MainPageAdapter;
import com.example.tobias.recipist.fragment.LocalRecipesFragment;
import com.example.tobias.recipist.fragment.FirebaseRecipeFragment;
import com.example.tobias.recipist.fragment.PublicFirebaseRecipesFragment;

public class MainActivity extends BaseActivity {
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
        mMainPageAdapter.addFragment(new PublicFirebaseRecipesFragment(), "overview");
//        mMainPageAdapter.addFragment(FirebaseRecipeFragment.newInstance(FirebaseRecipeFragment.TYPE_MY), "my recipes");
        mMainPageAdapter.addFragment(new LocalRecipesFragment(), "local recipes");

        // Initialize views.
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.main_tab_layout);

        // Setup views.
        mViewPager.setAdapter(mMainPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

//        new FetchMyRecipesTask(this).execute();
//
//        if (FirebaseUtil.getCurrentUser() != null) {
//            Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
//            startActivity(intent);
//        } else {
//            Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
//            startActivity(intent);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_action_new_recipe:
                newRecipe();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void newRecipe() {
        Intent data = new Intent(MainActivity.this, CreateRecipeActivity.class);
        startActivity(data);
    }
}