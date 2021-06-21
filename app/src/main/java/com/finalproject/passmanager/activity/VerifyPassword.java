package com.finalproject.passmanager.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.finalproject.passmanager.MainActivity;
import com.finalproject.passmanager.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import org.jetbrains.annotations.NotNull;

public class VerifyPassword extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private TextView currentemail;
    private EditText enterpassword;
    private Button verifypassword;
    private FirebaseAuth authentication;
    private ProgressDialog progressDialog;
    private ToggleButton visibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_password);

        authentication = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.password_list));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_lock);

        currentemail = findViewById(R.id.tv_currentemail_verify);
        currentemail.setText(authentication.getCurrentUser().getEmail());

        enterpassword = findViewById(R.id.et_password_verify);

        visibility = findViewById(R.id.btn_toggle_passvisibility_verify);
        visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enterpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    enterpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        verifypassword = findViewById(R.id.bt_verifypassword_verify);
        verifypassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass_check = enterpassword.getText().toString().trim();
                if (pass_check.isEmpty()) {
                    enterpassword.setError("Password field must not be empty");
                    enterpassword.requestFocus();
                    return;
                } else {
                    progressDialog.setMessage("Verifying...");
                    progressDialog.show();
                    enterpassword.setEnabled(false);
                    authentication.signInWithEmailAndPassword(currentemail.getText().toString().trim(), enterpassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(VerifyPassword.this, MainActivity.class);
                                        progressDialog.dismiss();
                                        startActivity(intent);
                                    } else {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthInvalidCredentialsException invalidCredentialsException) {
                                            Toast.makeText(VerifyPassword.this, "Email and password do not match. Please recheck your credentials", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            enterpassword.setEnabled(true);
                                        } catch (Exception e) {
                                            Toast.makeText(VerifyPassword.this, "Something prevented you from logging in. Please try again", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            enterpassword.setEnabled(true);
                                        }
                                    }
                                }
                            });
                }

            }
        });

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                currentemail.setTextColor(getResources().getColor(R.color.white));
                verifypassword.setTextColor(getResources().getColor(R.color.white));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                currentemail.setTextColor(getResources().getColor(R.color.black));
                break;
        }
    }

    @Override
    public void onResume() {
        try{
            currentemail = findViewById(R.id.tv_currentemail_verify);
            currentemail.setText(authentication.getCurrentUser().getEmail());

            enterpassword.setText("");
            enterpassword.setEnabled(true);

            Toast.makeText(this, "Vault locked. Please log in", Toast.LENGTH_SHORT).show();

            enterpassword = findViewById(R.id.et_password_verify);
            verifypassword = findViewById(R.id.bt_verifypassword_verify);
            verifypassword.setEnabled(true);

        } catch (Exception e){
            finish();
        }
        super.onResume();
    }
}