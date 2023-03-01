package com.task.tascura;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    // Activity for displaying custom splash screen.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sets the color of the navigation bar.
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorText));
        // Sets the color of the status bar.
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorText));

        new Handler().postDelayed(() ->
        {
            // Delays the start of the MainActivity by one second to display the splash screen.
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 1000);
    }
}