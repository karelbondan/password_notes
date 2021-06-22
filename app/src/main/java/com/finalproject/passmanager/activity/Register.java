package com.finalproject.passmanager.activity;

import android.app.ProgressDialog;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private TextView email, login;
    private ImageView logo;
    private EditText pass, reenterpass;
    private Button reg;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private ToggleButton visible_pass, visible_pass_reenter;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        FirebaseApp.initializeApp(Register.this);

        progressDialog = new ProgressDialog(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.register_user));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);

        logo = findViewById(R.id.iv_logo_register);
        email = findViewById(R.id.tv_email_register);
        pass = findViewById(R.id.tv_password_register);
        reenterpass = findViewById(R.id.et_reenterpass_register);
        reg = findViewById(R.id.bt_register_register);
        login = findViewById(R.id.bt_login_register);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        visible_pass = findViewById(R.id.btn_toggle_passvisibility_pass_register);
        visible_pass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        visible_pass_reenter = findViewById(R.id.btn_toggle_passvisibility_reenter_register);
        visible_pass_reenter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    reenterpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    reenterpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                login.setTextColor(getResources().getColor(R.color.white));
                reg.setTextColor(getResources().getColor(R.color.white));
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_text_light));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                login.setTextColor(getResources().getColor(R.color.black));
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_text_dark));
                break;
        }
    }

    private void register() {
        String email_check, password_check, reenterpass_check;
        email_check = email.getText().toString().toLowerCase().trim();
        password_check = pass.getText().toString();
        reenterpass_check = reenterpass.getText().toString();

        if (email_check.isEmpty()) {
            email.setError("Email must not be empty");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email_check).matches()) {
            email.setError("Please provide a valid email");
            email.requestFocus();
            return;
        }

        if (password_check.isEmpty()) {
            pass.setError("Password must not be empty");
            pass.requestFocus();
            return;
        }

        if (password_check.length() < 6) {
            pass.setError("Password length must be equal or greater than 6 characters");
            pass.requestFocus();
            return;
        }

        if (!reenterpass_check.equals(password_check)) {
            reenterpass.setError("Passwords do not match");
            reenterpass.requestFocus();
            return;
        }

        progressDialog.setMessage("Registering...");
        progressDialog.show();
        pass.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        pass.setEnabled(false);
        email.setEnabled(false);
        reg.setVisibility(View.GONE);
        mAuth.createUserWithEmailAndPassword(email_check, password_check)
                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> new_user = new HashMap<>();
                            new_user.put("email", email_check);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(new_user).addOnCompleteListener(Register.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, email_check + " has been registered. " +
                                                "Please check your inbox to continue with the registration", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                        mAuth.signOut();
                                        finish();
                                    } else {
                                        Toast.makeText(Register.this, "Failed to register " + email_check +
                                                ". Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                    pass.setVisibility(View.VISIBLE);
                                    email.setVisibility(View.VISIBLE);
                                    pass.setEnabled(true);
                                    email.setEnabled(true);
                                    reg.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException malFormed) {
                                Toast.makeText(Register.this, "Invalid email or password",
                                        Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthUserCollisionException existEmail) {
                                Toast.makeText(Register.this, "Email already registered. " +
                                        "Please login or use another email", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(Register.this, "Register failed. Please try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                            pass.setVisibility(View.VISIBLE);
                            email.setVisibility(View.VISIBLE);
                            pass.setEnabled(true);
                            email.setEnabled(true);
                            reg.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.cancel, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.cancel) {
            finish();
        }
        return true;
    }
}