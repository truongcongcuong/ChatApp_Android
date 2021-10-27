package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.dto.ViewProfileDto;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView img_view_profile_activity;
    private Button btn_friend_status_view_profile_activity;
    private String userId;
    private Gson gson;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);


        img_view_profile_activity = findViewById(R.id.img_view_profile_activity);
        Toolbar toolbar_view_profile_activity = findViewById(R.id.toolbar_view_profile_activity);
        btn_friend_status_view_profile_activity = findViewById(R.id.btn_friend_status_view_profile_activity);
        Button btn_chat_view_profile_activity = findViewById(R.id.btn_chat_view_profile_activity);

        toolbar_view_profile_activity.setTitleTextColor(Color.WHITE);
        toolbar_view_profile_activity.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar_view_profile_activity);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            userId = bundle.getString("userId");

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", Context.MODE_PRIVATE);
        UserSummaryDTO currentUser = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        if (userId != null) {
            getProfile(userId);
        }
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
        super.finish();
    }

    /*
    tạo menu trên thanh toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_profile_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_view_profile);

        menuItem.setOnMenuItemClickListener(item -> {

            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void getProfile(String userId) {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_USER + "viewProfile/" + userId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        ViewProfileDto viewProfileDto = gson.fromJson(res, ViewProfileDto.class);

                        btn_friend_status_view_profile_activity.setText(viewProfileDto.getFriendStatus().toString());
                        setTitle(viewProfileDto.getUser().getDisplayName());
                        Glide.with(this).load(viewProfileDto.getUser().getImageUrl())
                                .placeholder(R.drawable.image_placeholer)
                                .centerCrop().circleCrop()
                                .into(img_view_profile_activity);

                    } catch (NumberFormatException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {

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

}
