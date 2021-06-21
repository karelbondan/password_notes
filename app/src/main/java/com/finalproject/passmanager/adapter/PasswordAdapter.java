package com.finalproject.passmanager.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.finalproject.passmanager.MainActivity;
import com.finalproject.passmanager.activity.PasswordView;
import com.finalproject.passmanager.R;
import com.finalproject.passmanager.model.Password;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.MyViewHolder> {

    List<Password> passwords;
    Context context;

    public PasswordAdapter(ArrayList<Password> passwords, Context context) {
        this.passwords = passwords;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_password, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PasswordAdapter.MyViewHolder holder, int position) {
        holder.itemname.setText(passwords.get(position).getItemName());
        holder.username.setText(passwords.get(position).getUserName());
        holder.parentbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PasswordView.class);
                intent.putExtra("activity", "edit");
                intent.putExtra("id", passwords.get(position).getItemid());
                MainActivity.setRequireVerify(false);
                context.startActivity(intent);
            }
        });

        if (passwords.get(position).getURL().toLowerCase().trim().contains("google")) {
            Glide.with(this.context).load("https://icons.duckduckgo.com/ip3/www.google.com.ico").into(holder.icon);
        } else {
            Glide.with(this.context).load("https://www.google.com/s2/favicons?sz=64&domain_url=" + passwords.get(position).getURL())
                    .error(Glide.with(this.context).load(R.drawable.ic_web_default))
                    .into(holder.icon);
        }

        switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                holder.itemname.setTextColor(context.getResources().getColor(R.color.white));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                holder.itemname.setTextColor(context.getResources().getColor(R.color.black));
                break;
        }

//        holder.parentbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "you clicked this", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView itemname, username;
        Button parentbutton;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_webicon);
            itemname = itemView.findViewById(R.id.tv_itemname_singlelayout);
            username = itemView.findViewById(R.id.tv_itemusername_singlelayout);
            parentbutton = itemView.findViewById(R.id.button_parentlayout);
        }
    }
}
