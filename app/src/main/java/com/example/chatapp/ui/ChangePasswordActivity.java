package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextView txt_change_password_error;
    private EditText edt_change_password_re_enter_password;
    private EditText edt_change_password_new_password;
    private EditText edt_change_password_old_password;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_change_password_activity);
        toolbar.setTitle(R.string.change_password);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_change_password_error = findViewById(R.id.txt_change_password_error);
        edt_change_password_re_enter_password = findViewById(R.id.edt_change_password_re_enter_password);
        edt_change_password_new_password = findViewById(R.id.edt_change_password_new_password);
        edt_change_password_old_password = findViewById(R.id.edt_change_password_old_password);
        Button btn_change_password_action = findViewById(R.id.btn_change_password_action);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        btn_change_password_action.setOnClickListener(v -> {
            if (validData()) {
                updatePasswordRequest(edt_change_password_old_password.getText().toString(), edt_change_password_new_password.getText().toString());
            }
        });
    }

    private void updatePasswordRequest(String oldPassword, String newPassword) {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_USER + "me/changePassword",
                response -> {
                    txt_change_password_error.setText(response.toString());

                }, error -> {
            Log.e("err", error.toString());
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && error != null) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject object = new JSONObject(res);
                    Log.e("message", object.toString());
                    txt_change_password_error.setText(object.getString("message"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("oldPass", oldPassword);
                map.put("newPass", newPassword);
                return map;

            }

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

    private boolean validData() {
        if (TextUtils.isEmpty(edt_change_password_new_password.getText().toString())) {
            txt_change_password_error.setText(getString(R.string.pls_enter_new_password));
            return false;
        }
        if (TextUtils.isEmpty(edt_change_password_old_password.getText().toString())) {
            txt_change_password_error.setText(getString(R.string.pls_enter_old_password));
            return false;
        }
        if (TextUtils.isEmpty(edt_change_password_re_enter_password.getText().toString())) {
            txt_change_password_error.setText(getString(R.string.pls_re_enter_password));
            return false;
        }
        if (!edt_change_password_new_password.getText().toString().equals(edt_change_password_re_enter_password.getText().toString())) {
            txt_change_password_error.setText(getString(R.string.check_password));
            return false;
        }
        return true;
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