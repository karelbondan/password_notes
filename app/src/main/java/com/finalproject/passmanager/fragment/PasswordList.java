package com.finalproject.passmanager.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.finalproject.passmanager.activity.Login;
import com.finalproject.passmanager.MainActivity;
import com.finalproject.passmanager.adapter.PasswordAdapter;
import com.finalproject.passmanager.activity.PasswordAddEdit;
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

import java.util.ArrayList;
import java.util.Collections;

public class PasswordList extends Fragment implements Filterable {

    private static final String TAG = "password lists";
    private FloatingActionButton floating_additem;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private TextView singleitempass, gettingpass, nothinghere;
    private AppCompatActivity activity;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private PasswordAdapter mAdapter;
    private ArrayList<Password> passwordslist;
    private ArrayList<Password> passwordsAll;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.activity_password_list, container, false);

        passwordslist = new ArrayList<Password>();

        toolbar = view.findViewById(R.id.toolbar);
        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        swipeRefreshLayout = view.findViewById(R.id.refresh_swipe_list);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                createAndRefresh(view);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        actionBar = activity.getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.password_list));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_lock);


        floating_additem = view.findViewById(R.id.floating_additem);
        floating_additem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), PasswordAddEdit.class);
                intent.putExtra("activity", "add");
                MainActivity.setRequireVerify(false);
                startActivity(intent);
            }
        });

        recyclerView = view.findViewById(R.id.password_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new PasswordAdapter(passwordslist, view.getContext());
        recyclerView.setAdapter(mAdapter);

        singleitempass = view.findViewById(R.id.tv_itemusername_singlelayout);

        createAndRefresh(view);
        Log.d(TAG, "onCreate: " + passwordslist.toString());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.search_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getFilter().filter(newText);
                return false;
            }
        });
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    SpannableString string = new SpannableString(menu.getItem(i).getTitle().toString());
                    string.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, string.length(), 0);
                    menuItem.setTitle(string);
                }
                break;
        }
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sinkron) {
            createAndRefresh(getView());
            Toast.makeText(getContext(), "Synced successfully", Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId() == R.id.keluar) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out. Please login again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), Login.class);
            MainActivity.setRequireVerify(false);
            getActivity().finish();
            startActivity(intent);
        }
        return true;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Password> filteredPasswords = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredPasswords.addAll(passwordsAll);
            } else {
                String filtertext = constraint.toString().toLowerCase().trim();
                for (Password pass : passwordsAll) {
                    if (pass.getItemName().toLowerCase().contains(filtertext) ||
                            pass.getUserName().toLowerCase().contains(filtertext)) {
                        filteredPasswords.add(pass);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredPasswords;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            passwordslist.clear();
            passwordslist.addAll((ArrayList<Password>) results.values);
            mAdapter.notifyDataSetChanged();
        }
    };

    public void createAndRefresh(View view) {
        gettingpass = view.findViewById(R.id.tv_gettingpass_passlist);
        progressBar = view.findViewById(R.id.progress_circular_passlist);
        nothinghere = view.findViewById(R.id.tv_nothinghereyet);
        nothinghere.setVisibility(View.GONE);

        gettingpass.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("passwords");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                passwordslist.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Password passwords = dataSnapshot.getValue(Password.class);
                    passwordslist.add(passwords);
                }
                Collections.sort(passwordslist, Password.sortDescending);
                passwordsAll = new ArrayList<>(passwordslist);
                mAdapter.notifyDataSetChanged();
                gettingpass.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                if (passwordslist.isEmpty()) {
                    nothinghere.setVisibility(View.VISIBLE);
                } else {
                    nothinghere.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}