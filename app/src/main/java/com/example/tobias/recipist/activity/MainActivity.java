package com.example.tobias.recipist.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.BaseAdapter;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.auth.GoogleSignInActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
        startActivity(intent);
    }
}
