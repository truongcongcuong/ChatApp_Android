package com.example.chatapp.ui.signup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.example.chatapp.dto.UserSignUpDTO;
import com.google.android.material.textfield.TextInputLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {
    private TextInputLayout edt_sign_up_re_enter_password;
    private TextInputLayout edt_sign_up_name;
    private TextInputLayout edt_sign_up_phone_number;
    private TextInputLayout edt_sign_up_enter_password;
    private TextView txt_sign_up_error_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_signup_activity);
        toolbar.setTitle(getString(R.string.create_account));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        edt_sign_up_re_enter_password = findViewById(R.id.edt_sign_up_re_enter_password);
        edt_sign_up_name = findViewById(R.id.edt_sign_up_name);
        edt_sign_up_phone_number = findViewById(R.id.edt_sign_up_phone_number);
        edt_sign_up_enter_password = findViewById(R.id.edt_sign_up_enter_password);
        ImageButton ibt_sign_up_next_step1 = findViewById(R.id.ibt_sign_up_next_step1);
        txt_sign_up_error_response = findViewById(R.id.txt_sign_up_error_response);

        ibt_sign_up_next_step1.setOnClickListener(v -> {
            if (valid()) {
                UserSignUpDTO user = new UserSignUpDTO();
                user.setPassword(edt_sign_up_enter_password.getEditText().getText().toString().trim());
                user.setDisplayName(edt_sign_up_name.getEditText().getText().toString().trim());
                user.setPhoneNumber(edt_sign_up_phone_number.getEditText().getText().toString().trim());
                sendSignUpUserToServer(user);
            }
        });

    }

    private boolean valid() {
        txt_sign_up_error_response.setText("");
        if (edt_sign_up_name.getEditText().getText().toString().trim().isEmpty()) {
            edt_sign_up_name.setError(getString(R.string.check_name_empty));
            return false;
        }
        edt_sign_up_name.setError(null);

        if (edt_sign_up_enter_password.getEditText().getText().toString().trim().isEmpty()) {
            edt_sign_up_enter_password.setError(getString(R.string.check_password_empty));
            return false;
        }
        if (!edt_sign_up_enter_password.getEditText().getText().toString().trim().matches("[\\w]{8,}")) {
            edt_sign_up_enter_password.setError(getString(R.string.change_password_detail_8_char));
            return false;
        }
        edt_sign_up_enter_password.setError(null);

        if (!edt_sign_up_enter_password.getEditText().getText().toString().trim().equals(edt_sign_up_re_enter_password.getEditText().getText().toString())) {
            edt_sign_up_re_enter_password.setError(getString(R.string.check_password));
            return false;
        }
        edt_sign_up_re_enter_password.setError(null);

        if (edt_sign_up_phone_number.getEditText().getText().toString().trim().isEmpty()) {
            edt_sign_up_phone_number.setError(getString(R.string.check_phone_empty));
            return false;
        }
        edt_sign_up_phone_number.setError(null);

        if (!edt_sign_up_phone_number.getEditText().getText().toString().trim().matches("[0-9]{10,11}")) {
            edt_sign_up_phone_number.setError(getString(R.string.check_phone_regex));
            return false;
        }
        edt_sign_up_phone_number.setError(null);
        return true;
    }

    private void sendSignUpUserToServer(UserSignUpDTO user) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_SIGNUP + "save_information",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        user.setId((object.getString("id")));
                        Intent intent = new Intent(SignUpActivity.this, SignUpStep2Activity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user", user);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && error != null) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject object = new JSONObject(res);
                    txt_sign_up_error_response.setText(object.getString("message"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("displayName", user.getDisplayName());
                map.put("password", user.getPassword());
                map.put("phoneNumber", user.getPhoneNumber());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
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