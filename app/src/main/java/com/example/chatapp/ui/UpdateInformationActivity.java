package com.example.chatapp.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.UserDetailDTO;
import com.example.chatapp.dto.UserUpdateDTO;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.Json;
import lombok.SneakyThrows;

public class UpdateInformationActivity extends AppCompatActivity {
    private ImageButton ibt_edit_profile_back;
    private ImageView img_edit_profile_avt;
    private EditText edt_edit_profile_display_name;
    private EditText edt_edit_profile_birthday;
    private EditText edt_edit_profile_username;
    private RadioButton rbt_edit_profile_gender_male;
    private RadioButton rbt_edit_profile_gender_female;
    private Button btn_edit_profile_save;
    private UserDetailDTO userDetailDTO;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private String token;
    private Date birthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information2);

        Bundle bundle = getIntent().getExtras();
        userDetailDTO = (UserDetailDTO) bundle.getSerializable("user");

        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        ibt_edit_profile_back = findViewById(R.id.ibt_edit_profile_back);
        img_edit_profile_avt = findViewById(R.id.img_edit_profile_avt);
        edt_edit_profile_display_name = findViewById(R.id.edt_edit_profile_display_name);
        edt_edit_profile_birthday = findViewById(R.id.edt_edit_profile_birthday);
        edt_edit_profile_username = findViewById(R.id.edt_edit_profile_username);
        rbt_edit_profile_gender_male = findViewById(R.id.rbt_edit_profile_gender_male);
        rbt_edit_profile_gender_female = findViewById(R.id.rbt_edit_profile_gender_female);
        btn_edit_profile_save = findViewById(R.id.btn_edit_profile_save);

        Glide.with(this).load(userDetailDTO.getImageUrl())
                .centerCrop().circleCrop().into(img_edit_profile_avt);

        edt_edit_profile_birthday.setText(userDetailDTO.getDateOfBirth());
        edt_edit_profile_display_name.setText(userDetailDTO.getDisplayName());
        if (userDetailDTO.getGender().equalsIgnoreCase("female"))
            rbt_edit_profile_gender_female.setChecked(true);
        else
            rbt_edit_profile_gender_male.setChecked(true);

        if (userDetailDTO.getUsername() != null || !TextUtils.isEmpty(userDetailDTO.getUsername()))
            edt_edit_profile_username.setText(userDetailDTO.getUsername());

        edt_edit_profile_birthday.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    onDateSetListener,
                    year, month, date);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SneakyThrows
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                birthday = sdf.parse(year + "-" + month + "-" + dayOfMonth);
                Log.e("birthday : ", birthday.toString());
                edt_edit_profile_birthday.setText(year + "-" + month + "-" + dayOfMonth);
            }
        };

        btn_edit_profile_save.setOnClickListener(v -> {
            String gender;
            if (rbt_edit_profile_gender_male.isChecked())
                gender = rbt_edit_profile_gender_male.getText().toString();
            else gender = rbt_edit_profile_gender_female.getText().toString();
            UserUpdateDTO dto = UserUpdateDTO.builder()
                    .dateOfBirth(edt_edit_profile_birthday.getText().toString())
                    .displayName(edt_edit_profile_display_name.getText().toString())
                    .email(userDetailDTO.getEmail())
                    .gender(gender)
                    .username(edt_edit_profile_username.getText().toString())
                    .build();
            Log.e("user builder : ", dto.toString());
            updateInfo(dto);
        });

        ibt_edit_profile_back.setOnClickListener(v -> finish());


    }

    private void updateInfo(UserUpdateDTO dto) {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_USER + "me",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        Log.e("update infor : ", res.toString());
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
//                            JSONObject object = new JSONObject(res);
                            Log.e("eror : ", res);
                            showAlertDialogError(res);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("displayName", dto.getDisplayName());
                map.put("dateOfBirth",dto.getDateOfBirth());
                map.put("email", dto.getEmail());
                map.put("gender", dto.getGender());
                map.put("username", dto.getUsername());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }


    private void showAlertDialogError(String toString) {
        AlertDialog dialog = new AlertDialog.Builder(this).
                setTitle(getString(R.string.error_dialog_title))
                .setMessage(toString)
                .setNegativeButton(R.string.confirm_button, null)
                .create();
        dialog.show();
    }

}