package com.example.chatapp.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.ui.signin.SigninActivity;
import com.example.chatapp.ui.signup.SignUpActivity;

public class HomePageActivity extends AppCompatActivity {

    private Button btn_home_page_sign_in;
    private Button btn_home_page_sign_up;
    private Button btn_home_page_language_english;
    private Button btn_home_page_language_vietnamese;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        getSupportActionBar().hide();

        btn_home_page_language_vietnamese = findViewById(R.id.btn_home_page_language_vietnamese);
        btn_home_page_sign_in = findViewById(R.id.btn_home_page_sign_in);
        btn_home_page_sign_up = findViewById(R.id.btn_home_page_sign_up);
        btn_home_page_language_english = findViewById(R.id.btn_home_page_language_english);

        btn_home_page_language_vietnamese.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));

        btn_home_page_sign_in.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, SigninActivity.class);
            startActivity(intent);
        });

        btn_home_page_sign_up.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        btn_home_page_language_vietnamese.setOnClickListener(v -> {
            showDialogSignupSuccess();
        });

    }

    private void showDialogSignupSuccess() {
        AlertDialog alertDialog = new AlertDialog.Builder(HomePageActivity.this)
                .setTitle(R.string.notification_feature_error)
                .setMessage(R.string.feature_error_content)
                .setNegativeButton(R.string.accept, null)
                .create();
        alertDialog.show();
    }
}