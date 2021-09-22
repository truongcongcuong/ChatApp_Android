package com.example.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.ui.HomePageActivity;
import com.example.chatapp.ui.main.MainActivity;

public class OnLoadActivity extends AppCompatActivity {
    TextView txt_on_load_app_name,txt_on_load_members;
    ImageView img_on_load_logo;
    Animation top_animation, bottom_animation;
    private static int TIME_OUT = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_load);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        top_animation = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottom_animation = AnimationUtils.loadAnimation(this,R.anim.bot_animation);

        txt_on_load_app_name = findViewById(R.id.txt_on_load_app_name);
        txt_on_load_members = findViewById(R.id.txt_on_load_members);
        img_on_load_logo = findViewById(R.id.img_on_load_logo);

        img_on_load_logo.setAnimation(top_animation);
        txt_on_load_app_name.setAnimation(bottom_animation);
        txt_on_load_members.setAnimation(bottom_animation);

        SharedPreferences sharedPreferencesIsLogin = getSharedPreferences("is-login",MODE_PRIVATE);
        Log.e("login : ", sharedPreferencesIsLogin.getBoolean("status-login",false)+"");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sharedPreferencesIsLogin.getBoolean("status-login",false)){
                    Intent intent = new Intent(OnLoadActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(OnLoadActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },TIME_OUT);
    }

}