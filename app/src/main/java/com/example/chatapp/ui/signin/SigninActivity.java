package com.example.chatapp.ui.signin;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.R;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.main.MainActivity;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.Json;

public class SigninActivity extends AppCompatActivity {

    EditText edt_sign_in_user_name,edt_sign_in_password;
    Button btn_sign_in;
    ImageButton ibt_sign_in_back;
    TextView txt_sign_in_error;
    String username,password;
    String rfCookie;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        getSupportActionBar().hide();


        btn_sign_in = findViewById(R.id.btn_sign_in);
        edt_sign_in_user_name = findViewById(R.id.edt_sign_in_user_name);
        edt_sign_in_password = findViewById(R.id.edt_sign_in_password);
        ibt_sign_in_back = findViewById(R.id.ibt_sign_in_back);
        txt_sign_in_error = findViewById(R.id.txt_sign_in_error);



        btn_sign_in.setOnClickListener(v->{
            username = edt_sign_in_user_name.getText().toString();
            if(TextUtils.isEmpty(username)){
                txt_sign_in_error.setText(R.string.check_user_name_empty);
                return;
            }
            password = edt_sign_in_password.getText().toString();
            if(TextUtils.isEmpty(password)){
                txt_sign_in_error.setText(R.string.check_password_empty);
                return;
            }
            sendSignInRequest();
        });

        ibt_sign_in_back.setOnClickListener(v-> finish());
    }


    private void sendSignInRequest() {
        Log.e("a: ", "ádfasda");
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_AUTH+"signin",
                response -> {
                    try {

                        String res = URLDecoder.decode(URLEncoder.encode(response,"iso8859-1"),"UTF-8");
                        JSONObject object = new JSONObject(res);
                        Gson gson = new Gson();
                        UserSummaryDTO user = gson.fromJson(String.valueOf(object),UserSummaryDTO.class);
                        System.out.println("++++++++++++_____________________------------"+user.toString());



                        SharedPreferences sharedPreferencesUser = getSharedPreferences("user",MODE_PRIVATE);
                        SharedPreferences.Editor editor =sharedPreferencesUser.edit();
                        editor.putString("user-info", Json.encode(user)).apply();


                        SharedPreferences sharedPreferencesToken = getSharedPreferences("token",MODE_PRIVATE);
                        SharedPreferences.Editor editorToken = sharedPreferencesToken.edit();
                        editorToken.putString("access-token",user.getAccessToken()).apply();


                        SharedPreferences sharedPreferencesStatus = getSharedPreferences("status", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorStatus = sharedPreferencesStatus.edit();
                        editorStatus.putBoolean("status-code",true).apply();

                        SharedPreferences sharedPreferencesIsLogin = getSharedPreferences("is-login",MODE_PRIVATE);
                        SharedPreferences.Editor editorIsLogin = sharedPreferencesIsLogin.edit();
                        editorIsLogin.putBoolean("status-login",true).apply();

                        Intent intent = new Intent(SigninActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("EXIT",true);
                        startActivity(intent);
                        finish();

                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if(error instanceof ServerError && error != null){
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            txt_sign_in_error.setText(object.getString("message"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("username",username);
                map.put("password",password);

                return map;

            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.i("response",response.headers.toString());
                Map<String,String> responseHeader = response.headers;
                rfCookie = responseHeader.get("Set-Cookie");
                Log.i("rf-token",rfCookie);
                SharedPreferences sharedPreferencesToken = getSharedPreferences("token",MODE_PRIVATE);
                SharedPreferences.Editor editorToken = sharedPreferencesToken.edit();
                editorToken.putString("refresh-token",rfCookie).apply();
                return  super.parseNetworkResponse(response);
            }

        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
}