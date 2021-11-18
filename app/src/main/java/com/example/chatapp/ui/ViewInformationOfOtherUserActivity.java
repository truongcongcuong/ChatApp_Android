package com.example.chatapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuInformationAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.MyMenuItem;
import com.example.chatapp.dto.ViewProfileDto;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

public class ViewInformationOfOtherUserActivity extends AppCompatActivity {
    private ImageView view_info_other_user_avt;
    private ListView view_info_other_user_lv;
    //    private String userToString;
    private String token;
    private String otherUserId;
    //    private User user;
    private Gson gson;
    private ViewProfileDto viewProfileDto;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_information_of_other_user);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            otherUserId = bundle.getString("otherUserId");
        }

        Toolbar toolbar = findViewById(R.id.toolbar_view_info_other_user);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RelativeLayout view_info_other_user_top_layout = findViewById(R.id.view_info_other_user_top_layout);
        Glide.with(this).load(R.drawable.bg_infor)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        view_info_other_user_top_layout.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        view_info_other_user_lv = findViewById(R.id.lsv_view_info_other_user);
        view_info_other_user_avt = findViewById(R.id.view_info_other_user_avt);
        gson = new Gson();

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        getInformationDetail();

        view_info_other_user_avt.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewImageActivity.class);
            Bundle bundleViewImage = new Bundle();
            bundleViewImage.putString("activityTitle", viewProfileDto.getDisplayName());
            bundleViewImage.putString("activitySubTitle", getString(R.string.avatar));
            bundleViewImage.putString("imageUrl", viewProfileDto.getImageUrl());
            intent.putExtras(bundleViewImage);
            startActivity(intent);
        });

    }

    private void getInformationDetail() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_USER + "viewProfile/" + otherUserId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        viewProfileDto = gson.fromJson(res, ViewProfileDto.class);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    updateItems();
                },
                error -> {
                    Log.e("Error information : ", error.toString());
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    @SneakyThrows
    private void updateItems() {
        setTitle(viewProfileDto.getDisplayName());
        List<MyMenuItem> items = new ArrayList<>();
        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.name))
                .name(viewProfileDto.getDisplayName())
                .build());
        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.username))
                .name(viewProfileDto.getUsername())
                .build());
        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.gender))
                .name(viewProfileDto.getGender())
                .build());
        if (viewProfileDto.getDateOfBirth() != null) {
            items.add(MyMenuItem.builder()
                    .key(getResources().getString(R.string.birthday))
                    .name(sdf.format(sdfFull.parse(viewProfileDto.getDateOfBirth())))
                    .build());
        }
        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.mobile))
                .name(viewProfileDto.getPhoneNumber())
                .build());
        MenuInformationAdapter adapter = new MenuInformationAdapter(this, items, R.layout.line_item_menu_button_vertical);
        view_info_other_user_lv.setAdapter(adapter);
        Glide.with(this)
                .load(viewProfileDto.getImageUrl())
                .centerCrop().circleCrop().placeholder(R.drawable.img_avatar_placeholer)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view_info_other_user_avt);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

}