package com.finalproject.passmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleObserver;

import com.finalproject.passmanager.activity.VerifyPassword;
import com.finalproject.passmanager.fragment.Account;
import com.finalproject.passmanager.fragment.Generator;
import com.finalproject.passmanager.fragment.PasswordList;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements LifecycleObserver {

    private Toast back;
    private long backPressedTime;
    private BottomNavigationView bottomnav;
    private Handler handler = new Handler();
    private int timeout;
    private long end;
    private int[] timeouts_value = {60000, 300000, 600000, 300000, 1200000, 1800000, 999};
    private static boolean requireVerify;

    public static boolean isRequireVerify() {
        return requireVerify;
    }

    public static void setRequireVerify(boolean requireVerify) {
        MainActivity.requireVerify = requireVerify;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomnav = findViewById(R.id.bottom_navigation);
        bottomnav.setOnNavigationItemSelectedListener(navigation);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        setTimeout(timeouts_value[sharedPreferences.getInt("timeout", 6)]);
        setEnd(System.currentTimeMillis() + getTimeout());

        lock.run();
        check_timeout.run();

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                break;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new PasswordList()).commit();
        }

        OrientationEventListener a = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                setRequireVerify(false);
            }
        };
    }

    private Runnable check_timeout = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
//            Toast.makeText(MainActivity.this, String.valueOf(sharedPreferences.getInt("timeout", 6)), Toast.LENGTH_SHORT).show();
//            Toast.makeText(MainActivity.this, String.valueOf(System.currentTimeMillis()), Toast.LENGTH_SHORT).show();
////            Toast.makeText(MainActivity.this, String.valueOf(getStart()), Toast.LENGTH_SHORT).show();
//            Toast.makeText(MainActivity.this, String.valueOf(getEnd()), Toast.LENGTH_SHORT).show();
            if (timeouts_value[sharedPreferences.getInt("timeout", 6)] != getTimeout()) {
//                Toast.makeText(MainActivity.this, "value changed from " + String.valueOf(getTimeout()) + " -> "
//                        + String.valueOf(timeouts_value[sharedPreferences.getInt("timeout", 6)]), Toast.LENGTH_SHORT).show();
                setTimeout(timeouts_value[sharedPreferences.getInt("timeout", 6)]);
                setEnd(System.currentTimeMillis() + getTimeout());
            }
            handler.postDelayed(this, 500);
        }
    };

    private Runnable lock = new Runnable() {
        @Override
        public void run() {
            if (getTimeout() != 999) {
                if (System.currentTimeMillis() > getEnd()) {
                    Toast.makeText(MainActivity.this, "Vault timed out. Please log in again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), VerifyPassword.class);
                    finish();
                    startActivity(intent);
                    stopRunnable();
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        }
    };

    private Runnable verify = new Runnable() {
        @Override
        public void run() {
            setRequireVerify(true);
            handler.postDelayed(this, 500);
        }
    };

    private void stopRunnable() {
        handler.removeCallbacks(lock);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequireVerify(false);
            verify.run();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequireVerify(false);
            verify.run();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.removeCallbacks(verify);
        MainActivity.setRequireVerify(true);
    }

    @Override
    public void onPause() {
        if (getTimeout() == 999 && isRequireVerify()) {
            Intent intent = new Intent(MainActivity.this, VerifyPassword.class);
            startActivity(intent);
            finish();
        }
        MainActivity.setRequireVerify(true);
        super.onPause();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigation = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    fragment = new PasswordList();
                    break;
                case R.id.nav_generator:
                    fragment = new Generator();
                    break;
                case R.id.nav_account:
                    fragment = new Account();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, fragment).commit();
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            back.cancel();
            super.onBackPressed();
            return;
        } else {
            back = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            back.show();
        }
        backPressedTime = System.currentTimeMillis();
    }


}