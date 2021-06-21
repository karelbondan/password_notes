package com.finalproject.passmanager.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.finalproject.passmanager.MainActivity;
import com.finalproject.passmanager.R;

import java.util.Random;

public class Generator extends Fragment {

    TextView generatedPass, lengthpass;
    SeekBar seekBar;
    CheckBox incDigit, incPunc;
    Button copyPass;
    String pass;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private AppCompatActivity activity;
    private int length = 6;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_generator, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        actionBar = activity.getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.pass_generator));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_refresh);

        seekBar = view.findViewById(R.id.seekbar_generator);
        copyPass = view.findViewById(R.id.bt_copypass_generator);
        generatedPass = view.findViewById(R.id.tv_passgenerator_generator);
        lengthpass = view.findViewById(R.id.tv_passwordlength_generator);
        incDigit = view.findViewById(R.id.cb_includedigit_generator);
        incPunc = view.findViewById(R.id.cb_includepunc_generator);

        generatedPass.setText(getResources().getString(R.string.generated_pass));

        incDigit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pass = generatePassword(getLength(), incDigit, incPunc);
                generatedPass.setText(pass);
            }
        });

        incPunc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pass = generatePassword(getLength(), incDigit, incPunc);
                generatedPass.setText(pass);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int passwordLength, boolean fromUser) {
                if (passwordLength <= 6) {
                    passwordLength = 6;
                }
                setLength(passwordLength);
                pass = generatePassword(passwordLength, incDigit, incPunc);
                generatedPass.setText(pass);
                lengthpass.setText(String.valueOf(passwordLength));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        copyPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager copy = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("passmanager password copy", generatedPass.getText().toString());
                copy.setPrimaryClip(clip);

                Toast.makeText(view.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.drawable.ic_refresh) {
            MainActivity.setRequireVerify(false);
        }
        return true;
    }

    private String generatePassword(int passwordLength, CheckBox incDigit, CheckBox incPunc) {
        String alphabet = "abcdefghjkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ";
        String digit = "23456789";
        String punctuations = "!@#$%^&*";

        char[] alphabet_final = alphabet.toCharArray();
        char[] digit_final = digit.toCharArray();
        char[] punctuations_final = punctuations.toCharArray();

        int index = 0;
        long digits = 0, puncs = 0;

        char[] password = new char[passwordLength];
        double checker = (double) passwordLength * 25 / 100;
        long checker_int = Math.round(checker);

        // randomize digits
        if (incDigit.isChecked()) {
            digits = checker_int;
            for (int j = 0; j < digits; j++) {
                int rdn = new Random().nextInt(digit_final.length);
                password[index] = digit_final[rdn];
                index++;
            }
        }

        // randomize punctuations
        if (incPunc.isChecked()) {
            puncs = checker_int;
            for (int j = 0; j < puncs; j++) {
                int rdn = new Random().nextInt(punctuations_final.length);
                password[index] = punctuations_final[rdn];
                index++;
            }
        }

        // randomize alphabet
        for (int j = 1; j <= passwordLength - (digits + puncs); j++) {
            int rdn = new Random().nextInt(alphabet_final.length);
            password[index] = alphabet_final[rdn];
            index++;
        }

        String finalpass = "";

        char[] password_final = randomize(password, passwordLength);
        for (Object value : password_final) {
            finalpass += value;
        }
        return finalpass;
    }

    private char[] randomize(char[] password, int length) {
        char[] password_final = new char[length];
        int index = 0;
        do {
            int rdn = new Random().nextInt(length);
            if (password[rdn] != '\u0000') {
                password_final[index] = password[rdn];
                password[rdn] = '\u0000';    // \u0000 is a NULL character, since there's no way to remove an element from an array
                index++;
            }
        } while (index < length);
        return password_final;
    }
}