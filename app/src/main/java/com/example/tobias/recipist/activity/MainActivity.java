package com.example.tobias.recipist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.auth.GoogleSignInActivity;
import com.example.tobias.recipist.activity.recipe.CreateRecipeActivity;
import com.example.tobias.recipist.adapter.MainPageAdapter;
import com.example.tobias.recipist.fragment.LocalRecipesFragment;
import com.example.tobias.recipist.fragment.FirebaseRecipeFragment;
import com.example.tobias.recipist.fragment.PublicFirebaseRecipesFragment;
import com.example.tobias.recipist.util.FirebaseUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String KEY_FORCE_VIEW_PAGER_ITEM = TAG + "FORCE VIEW PAGER ITEM";

    private MainPageAdapter mMainPageAdapter;

    @BindView(R.id.main_view_pager) ViewPager mViewPager;
    @BindView(R.id.main_tab_layout) TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Initialize & setup Toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Initialize & setup MainPageAdapter.
        mMainPageAdapter = new MainPageAdapter(getSupportFragmentManager());
        mMainPageAdapter.addFragment(new PublicFirebaseRecipesFragment(), getString(R.string.main_public_recipes_tab_title));
        mMainPageAdapter.addFragment(new LocalRecipesFragment(), getString(R.string.main_local_recipes_tab_title));

        // Setup views.
        mViewPager.setAdapter(mMainPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        Intent data = getIntent();
        if (data != null) {
            int viewPagerItemId = data.getIntExtra(KEY_FORCE_VIEW_PAGER_ITEM, 0);
            mViewPager.setCurrentItem(viewPagerItemId);
        }

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