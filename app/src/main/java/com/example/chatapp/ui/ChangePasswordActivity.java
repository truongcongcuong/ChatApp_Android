package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.google.android.material.textfield.TextInputLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextView txt_change_password_error;
    private TextInputLayout edt_change_password_re_enter_password;
    private TextInputLayout edt_change_password_new_password;
    private TextInputLayout edt_change_password_old_password;
    private String token;
    private ProgressBar change_password_progress_bar;
    private Button btn_change_password_action;
    private Timer timer;

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
        change_password_progress_bar = findViewById(R.id.change_password_progress_bar);
        change_password_progress_bar.setVisibility(View.GONE);
        btn_change_password_action = findViewById(R.id.btn_change_password_action);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        timer = new Timer();
        btn_change_password_action.setOnClickListener(v -> {
            txt_change_password_error.setText("");
            if (validData()) {
                btn_change_password_action.setVisibility(View.GONE);
                change_password_progress_bar.setVisibility(View.VISIBLE);
                String oldPass = edt_change_password_old_password.getEditText().getText().toString().trim();
                String newPass = edt_change_password_new_password.getEditText().getText().toString().trim();
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updatePasswordRequest(oldPass, newPass);
                    }
                }, 1000);
            }
        });
    }

    private void updatePasswordRequest(String oldPassword, String newPassword) {
        JSONObject objectRequest = new JSONObject();
        try {
            objectRequest.put("oldPass", oldPassword);
            objectRequest.put("newPass", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,
                Constant.API_USER + "me/changePassword",
                objectRequest,
                response -> {
                    btn_change_password_action.setVisibility(View.VISIBLE);
                    change_password_progress_bar.setVisibility(View.GONE);
                    try {
                        JSONObject object = new JSONObject(response.toString());
                        Log.e("message", object.toString());
                        txt_change_password_error.setTextColor(getResources().getColor(R.color.susscess));
                        txt_change_password_error.setText(object.getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            btn_change_password_action.setVisibility(View.VISIBLE);
            change_password_progress_bar.setVisibility(View.GONE);
            Log.e("err", error.toString());
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && error != null) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject object = new JSONObject(res);
                    Log.e("message", object.toString());
                    String fieldName = object.getString("field");
                    if (fieldName != null) {
                        if (fieldName.equalsIgnoreCase("oldPass")) {
                            edt_change_password_old_password.setError(object.getString("message"));
                            edt_change_password_old_password.requestFocus();
                        } else if (fieldName.equalsIgnoreCase("newPass")) {
                            edt_change_password_new_password.setError(object.getString("message"));
                            edt_change_password_new_password.requestFocus();
                        }
                    } else {
                        txt_change_password_error.setTextColor(Color.RED);
                        txt_change_password_error.setText(object.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
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
        if (TextUtils.isEmpty(edt_change_password_old_password.getEditText().getText().toString().trim())) {
            edt_change_password_old_password.setError(getString(R.string.pls_enter_old_password));
            edt_change_password_old_password.requestFocus();
            return false;
        }
        edt_change_password_old_password.setError(null);
        String newPass = edt_change_password_new_password.getEditText().getText().toString().trim();
        if (TextUtils.isEmpty(newPass)) {
            edt_change_password_new_password.setError(getString(R.string.pls_enter_new_password));
            edt_change_password_new_password.requestFocus();
            return false;
        }
        edt_change_password_new_password.setError(null);
        String reEnterNewPass = edt_change_password_re_enter_password.getEditText().getText().toString().trim();
        if (TextUtils.isEmpty(reEnterNewPass)) {
            edt_change_password_re_enter_password.setError(getString(R.string.pls_re_enter_password));
            edt_change_password_re_enter_password.requestFocus();
            return false;
        }
        edt_change_password_re_enter_password.setError(null);
        if (!newPass.equals(reEnterNewPass)) {
            edt_change_password_re_enter_password.setError(getString(R.string.check_password));
            edt_change_password_re_enter_password.requestFocus();
            return false;
        }
        edt_change_password_re_enter_password.setError(null);
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