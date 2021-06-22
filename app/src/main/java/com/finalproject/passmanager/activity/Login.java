package com.finalproject.passmanager.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.finalproject.passmanager.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import org.jetbrains.annotations.NotNull;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText email, password;
    private long backPressedTime;
    private TextView register;
    private Toast back;
    private Button login;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private ImageView logo;
    private ProgressDialog progressDialog;
    private ToggleButton visibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        progressDialog = new ProgressDialog(this);

        // initializing the toolbar and action bar elements
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.login_user));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_person);

        // initializing the buttons and necessary components which will later be used
        logo = findViewById(R.id.iv_logo_login);
        email = findViewById(R.id.tv_email_login);
        password = findViewById(R.id.tv_password_login);
        login = findViewById(R.id.bt_login_login);

        /*
        setOnClickListener means that when the button is clicked, it will do something.
        'this' passed as an parameter means that 'this' contains the information
        about what to do after the button has been clicked. The overridden 'onClick' method
        below defines it. I did not use the interface 'onClickListener' directly on the function
        call to avoid repetition and keep the code clean.
        */
        login.setOnClickListener(this);

        register = findViewById(R.id.bt_register_main);
        register.setOnClickListener(this);

        /*
        setOnCheckedChangeListener means that the button is a toggle button. It can return true or false
        (checked or not) depending on the current state of the button.
        */
        visibility = findViewById(R.id.btn_toggle_passvisibility_login);
        visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        // switch case for light Android theme and dark Android theme
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                register.setTextColor(getResources().getColor(R.color.white));
                login.setTextColor(getResources().getColor(R.color.white));
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_text_light));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                register.setTextColor(getResources().getColor(R.color.black));
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_text_dark));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_login_login) {
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
            userLogin();
        }
        if (v.getId() == R.id.bt_register_main) {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        }
    }

    private void userLogin() {
        String email_login, password_login;
        email_login = email.getText().toString().toLowerCase().trim();
        password_login = password.getText().toString();

        if (email_login.isEmpty()) {
            email.setError("Email field cannot be empty");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email_login).matches()) {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return;
        }
        if (password_login.isEmpty()) {
            password.setError("Password field cannot be empty");
            password.requestFocus();
            return;
        }

        /*
        If all of the above statements are passed, log in with Firebase authentication. Set the email and
        password fields to be disabled so the keyboard does not block the log in process. It will re-enable
        the fields again after it has successfully logged the user in or when it fails to log the user in.
        A progress dialog (loading) will be shown in the process of logging the user in (progressDialog).
        If it fails to log the user in, it will catch the exception thrown by the task.
        */
        email.setEnabled(false);
        password.setEnabled(false);
        authentication.signInWithEmailAndPassword(email_login, password_login).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                        email.setEnabled(true);
                        password.setEnabled(true);
                        Intent intent = new Intent(Login.this, VerifyPassword.class);
                        startActivity(intent);
                        progressDialog.dismiss();
                        finish();
                    } else {
                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                        Toast.makeText(Login.this, "Please verify your email address to continue using the app", Toast.LENGTH_SHORT).show();
                        email.setEnabled(true);
                        password.setEnabled(true);
                        progressDialog.dismiss();
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException invalidUserException) {
                        Toast.makeText(Login.this, "Account not found. Please register first", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException invalidCredentialsException) {
                        Toast.makeText(Login.this, "Email and password do not match. Please recheck your credentials", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Login.this, "Something prevented you from logging in. Please try again", Toast.LENGTH_SHORT).show();
                    }
                    email.setEnabled(true);
                    password.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });
    }

    // gets called when the back button on the navigation bar is pressed
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