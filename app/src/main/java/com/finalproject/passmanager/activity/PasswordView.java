package com.finalproject.passmanager.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.finalproject.passmanager.MainActivity;
import com.finalproject.passmanager.R;
import com.finalproject.passmanager.model.Password;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class PasswordView extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;
    private ActionBar actionBar;
    private FloatingActionButton edit;
    private Button username_copy, password_copy, note_copy;
    private ToggleButton melihatpassword;
    private TextView name, username, password, url, note, datemodified;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private String activity;
    private Password passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_view);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.item_info));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);

        username_copy = findViewById(R.id.btn_copy_username);
        password_copy = findViewById(R.id.btn_copy_password);
        note_copy = findViewById(R.id.btn_copy_note);
        melihatpassword = findViewById(R.id.btn_toggle_passvisibility_verify);

        name = findViewById(R.id.tv_itemname_add_edit);
        username = findViewById(R.id.tv_username_add_edit);
        password = findViewById(R.id.tv_password_add_edit);
        url = findViewById(R.id.tv_url_add_edit);
        note = findViewById(R.id.tv_note_add_edit);
        datemodified = findViewById(R.id.tv_datemodified);

        melihatpassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                name.setTextColor(getResources().getColor(R.color.white));
                username.setTextColor(getResources().getColor(R.color.white));
                password.setTextColor(getResources().getColor(R.color.white));
                url.setTextColor(getResources().getColor(R.color.white));
                note.setTextColor(getResources().getColor(R.color.white));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                name.setTextColor(getResources().getColor(R.color.black));
                username.setTextColor(getResources().getColor(R.color.black));
                password.setTextColor(getResources().getColor(R.color.black));
                url.setTextColor(getResources().getColor(R.color.black));
                note.setTextColor(getResources().getColor(R.color.black));
                break;
        }

        username_copy.setOnClickListener(this);
        password_copy.setOnClickListener(this);
        note_copy.setOnClickListener(this);

        Intent intent = getIntent();
        activity = intent.getStringExtra("id");

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("passwords");

        databaseReference.child(activity).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                try {
                    passwordView = snapshot.getValue(Password.class);
                    name.setText(passwordView.getItemName());
                    username.setText(passwordView.getUserName());
                    password.setText(passwordView.getPassword());
                    url.setText(passwordView.getURL());
                    note.setText(passwordView.getNote());
                    datemodified.setText(passwordView.getDate() + " " + passwordView.getTime());
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        edit = findViewById(R.id.btn_edititem);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordView.this, PasswordAddEdit.class);
                intent.putExtra("activity", "edit");
                intent.putExtra("id", getIntent().getStringExtra("id"));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            AlertDialog.Builder dialog_confirm_builder = new AlertDialog.Builder(this)
                    .setMessage("Delete the entry?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.child(activity)
                                    .removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable
                                                               @org.jetbrains.annotations.Nullable
                                                                       DatabaseError error,
                                                               @NonNull @NotNull DatabaseReference ref) {
                                            Toast.makeText(PasswordView.this,
                                                    "Entry deleted successfully",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                            dialog.cancel();
                                        }
                                    });
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog dialog_confirm = dialog_confirm_builder.create();
            dialog_confirm.setTitle("Confirmation");
            dialog_confirm.show();

            Button bt_no_dialog = dialog_confirm.getButton(DialogInterface.BUTTON_NEGATIVE);
            Button bt_yes_dialog = dialog_confirm.getButton(DialogInterface.BUTTON_POSITIVE);
            if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                bt_no_dialog.setTextColor(getResources().getColor(R.color.white));
                bt_yes_dialog.setTextColor(getResources().getColor(R.color.white));
            }
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_copy_username){
            ClipboardManager copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("passmanager username copy", username.getText().toString());
            copy.setPrimaryClip(clip);

            Toast.makeText(PasswordView.this, "Username copied", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.btn_copy_password){
            ClipboardManager copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("passmanager password copy", password.getText().toString());
            copy.setPrimaryClip(clip);

            Toast.makeText(PasswordView.this, "Password copied", Toast.LENGTH_SHORT).show();
        }
        if (v.getId() == R.id.btn_copy_note){
            ClipboardManager copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("passmanager note copy", note.getText().toString());
            copy.setPrimaryClip(clip);

            Toast.makeText(PasswordView.this, "Note copied", Toast.LENGTH_SHORT).show();
        }
    }
}