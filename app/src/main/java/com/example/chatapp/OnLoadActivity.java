package com.example.chatapp;

import android.annotation.SuppressLint;
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
import com.example.chatapp.utils.LanguageUtils;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OnLoadActivity extends AppCompatActivity {
    private static final int TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_load);
        handleSSLHandshake();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Animation top_animation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottom_animation = AnimationUtils.loadAnimation(this, R.anim.bot_animation);

        TextView txt_on_load_app_name = findViewById(R.id.txt_on_load_app_name);
        TextView txt_on_load_members = findViewById(R.id.txt_on_load_members);
        ImageView img_on_load_logo = findViewById(R.id.img_on_load_logo);

        img_on_load_logo.setAnimation(top_animation);
        txt_on_load_app_name.setAnimation(bottom_animation);
        txt_on_load_members.setAnimation(bottom_animation);

        SharedPreferences sharedPreferencesIsLogin = getSharedPreferences("is-login", MODE_PRIVATE);
        Log.e("login : ", sharedPreferencesIsLogin.getBoolean("status-login", false) + "");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LanguageUtils languageUtils = new LanguageUtils(OnLoadActivity.this);
                languageUtils.loadLocale();
                if (sharedPreferencesIsLogin.getBoolean("status-login", false)) {
                    Intent intent = new Intent(OnLoadActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(OnLoadActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, TIME_OUT);
    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

}