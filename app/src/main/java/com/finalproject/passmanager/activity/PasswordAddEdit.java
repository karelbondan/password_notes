package com.finalproject.passmanager.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.finalproject.passmanager.R;
import com.finalproject.passmanager.model.Password;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PasswordAddEdit extends AppCompatActivity {

    private FloatingActionButton confirm;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private String activity, id;
    private EditText itemname, username, password, urlink, note;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private Password passwordEdit;
    private ToggleButton visibility;
    private final Handler timeout_handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_add_edit);

        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        itemname = findViewById(R.id.tv_itemname_add_edit);
        username = findViewById(R.id.tv_username_add_edit);
        password = findViewById(R.id.tv_password_add_edit);
        urlink = findViewById(R.id.tv_url_add_edit);
        note = findViewById(R.id.tv_note_add_edit);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("passwords");

        Intent getintent = getIntent();
        activity = getintent.getStringExtra("activity");
        if (activity.toLowerCase().equals("edit")) {
            actionBar = getSupportActionBar();
            actionBar.setTitle(getResources().getString(R.string.edit_item));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);

            id = getintent.getStringExtra("id");
            databaseReference.child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    passwordEdit = snapshot.getValue(Password.class);
                    itemname.setText(passwordEdit.getItemName());
                    username.setText(passwordEdit.getUserName());
                    password.setText(passwordEdit.getPassword());
                    urlink.setText(passwordEdit.getURL());
                    note.setText(passwordEdit.getNote());
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            visibility = findViewById(R.id.btn_toggle_passvisibility_addedit);
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

            confirm = findViewById(R.id.confirm_add);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // edit entry then update database, if success then finish else throw error message
                    if (itemname.getText().toString().trim().isEmpty()) {
                        itemname.setError("Must be at least one letter");
                        itemname.requestFocus();
                    } else {
//                        Passwords update_pass = new Passwords(id, itemname.getText().toString(), username.getText().toString(),
//                                password.getText().toString(), urlink.getText().toString(), note.getText().toString(),
//                                Passwords.getItemDate(), Passwords.getItemTime());

                        Map<String, Object> update_pass = new HashMap<>();
                        update_pass.put("itemName", itemname.getText().toString());
                        update_pass.put("userName", username.getText().toString());
                        update_pass.put("password", password.getText().toString());
                        update_pass.put("url", urlink.getText().toString());
                        update_pass.put("note", note.getText().toString());
                        update_pass.put("date", Password.getItemDate());
                        update_pass.put("time", Password.getItemTime());

                        databaseReference.child(id).updateChildren(update_pass)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(PasswordAddEdit.this, "Entry updated successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(PasswordAddEdit.this, "Failed to update entry. Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
//                    finish();
                }
            });

        } else {
            actionBar = getSupportActionBar();
            actionBar.setTitle(getResources().getString(R.string.add_item));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);

            confirm = findViewById(R.id.confirm_add);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // add to database, if success then finish else throw error message

                    String itemname_check = itemname.getText().toString().trim();
                    if (itemname_check.isEmpty()) {
                        itemname.setError("Must be at least one letter");
                        itemname.requestFocus();
                        return;
                    }
                    addItem();
                }
            });

        }
    }

    private void addItem() {
        String id = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("passwords").push().getKey();
        Password pass = new Password(id, itemname.getText().toString().trim(), username.getText().toString().trim(), password.getText().toString().trim(), urlink.getText().toString().trim(), note.getText().toString().trim(), Password.getItemDate(), Password.getItemTime());
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("passwords").child(id).setValue(pass).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void avoid) {
                Toast.makeText(PasswordAddEdit.this, "Entry added", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(PasswordAddEdit.this, "Failed to add entry. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}