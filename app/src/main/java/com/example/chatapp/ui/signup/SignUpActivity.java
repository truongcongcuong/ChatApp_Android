package com.example.chatapp.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {
    private EditText edt_sign_up_re_enter_password;
    private EditText edt_sign_up_name;
    private EditText edt_sign_up_phone_number;
    private EditText edt_sign_up_enter_password;
    private ImageButton ibt_sign_up_next_step1;
    private ImageButton ibt_sign_up_back;
    private TextView txt_sign_up_error_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edt_sign_up_re_enter_password = findViewById(R.id.edt_sign_up_re_enter_password);
        edt_sign_up_name = findViewById(R.id.edt_sign_up_name);
        edt_sign_up_phone_number = findViewById(R.id.edt_sign_up_phone_number);
        edt_sign_up_enter_password = findViewById(R.id.edt_sign_up_enter_password);
        ibt_sign_up_next_step1 = findViewById(R.id.ibt_sign_up_next_step1);
        txt_sign_up_error_response = findViewById(R.id.txt_sign_up_error_response);
        ibt_sign_up_back = findViewById(R.id.ibt_sign_up_back);


        ibt_sign_up_next_step1.setOnClickListener(v -> {
            if (!edt_sign_up_enter_password.getText().toString().equals(edt_sign_up_re_enter_password.getText().toString())) {
                txt_sign_up_error_response.setText(R.string.check_password);
                return;
            }

            UserSignUpDTO user = new UserSignUpDTO();
            user.setPassword(edt_sign_up_enter_password.getText().toString());
            user.setDisplayName(edt_sign_up_name.getText().toString());
            user.setPhoneNumber(edt_sign_up_phone_number.getText().toString());
            sendSignUpUserToServer(user);
        });

        ibt_sign_up_back.setOnClickListener(v -> {
            finish();
        });
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
}