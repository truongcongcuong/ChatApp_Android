package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

public class InfoActivity extends AppCompatActivity {

    private UserSummaryDTO user;
    private Gson gson;
    private ImageView info_image;
    private TextView info_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        gson = new Gson();

        SharedPreferences sharedPreferencesUser = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        info_image = findViewById(R.id.info_image);
        info_name = findViewById(R.id.info_name);
        Glide.with(this).load(user.getImageUrl())
                .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                .into(info_image);
        info_name.setText(user.getDisplayName());
    }
}