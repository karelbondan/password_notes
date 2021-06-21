package com.finalproject.passmanager;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.finalproject.passmanager.activity.Login;
import com.finalproject.passmanager.activity.VerifyPassword;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private final Handler handler = new Handler();
    private long time_end = System.currentTimeMillis() + 1000;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.iv_logo_splash);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_light));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_dark));
                break;
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FirebaseAuth.getInstance().getCurrentUser() != null && System.currentTimeMillis() > time_end) {
                    Intent intent = new Intent(SplashScreen.this, VerifyPassword.class);
                    startActivity(intent);
                } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashScreen.this, Login.class);
                    startActivity(intent);
                }
            }
        }, 2000L);
    }
}