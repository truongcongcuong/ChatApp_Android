package com.example.chatapp.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

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
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.UserDetailDTO;
import com.example.chatapp.dto.UserUpdateDTO;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

public class UpdateInformationActivity extends AppCompatActivity {
    private EditText edt_edit_profile_display_name;
    private EditText edt_edit_profile_birthday;
    private RadioButton rbt_edit_profile_gender_male;
    private RadioButton rbt_edit_profile_gender_female;
    private UserDetailDTO userDetailDTO;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private String token;
    private Date birthday;
    private Gson gson;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information2);

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_update_information2_activity);
        toolbar.setTitle(R.string.edit_profile);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        gson = new Gson();

        Bundle bundle = getIntent().getExtras();
        userDetailDTO = (UserDetailDTO) bundle.getSerializable("user");

        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        ImageView img_edit_profile_avt = findViewById(R.id.img_edit_profile_avt);
        edt_edit_profile_display_name = findViewById(R.id.edt_edit_profile_display_name);
        edt_edit_profile_birthday = findViewById(R.id.edt_edit_profile_birthday);
        rbt_edit_profile_gender_male = findViewById(R.id.rbt_edit_profile_gender_male);
        rbt_edit_profile_gender_female = findViewById(R.id.rbt_edit_profile_gender_female);
        Button btn_edit_profile_save = findViewById(R.id.btn_edit_profile_save);

        Glide.with(this).load(userDetailDTO.getImageUrl())
                .placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(img_edit_profile_avt);

        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (userDetailDTO.getDateOfBirth() != null)
            edt_edit_profile_birthday.setText(sdf.format(sdf2.parse(userDetailDTO.getDateOfBirth())));
        edt_edit_profile_display_name.setText(userDetailDTO.getDisplayName());
        if ("female".equalsIgnoreCase(userDetailDTO.getGender()))
            rbt_edit_profile_gender_female.setChecked(true);
        else
            rbt_edit_profile_gender_male.setChecked(true);

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
                    .build();
            Log.e("user builder : ", dto.toString());
            updateInfo(dto);
        });

    }

    /*private void updateInfo(UserUpdateDTO dto) {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_USER + "me",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        Log.e("update infor : ", res.toString());
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences sharedPreferencesUser = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor sharedPreferencesUserEditor = sharedPreferencesUser.edit();
                    sharedPreferencesUserEditor.putString("user-info",Json.encode(dto)).apply();
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
    }*/

    private void updateInfo(UserUpdateDTO dto) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(gson.toJson(dto));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, Constant.API_USER + "me",
                jsonObject,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response.toString(), "iso8859-1"), "UTF-8");
                        Log.e("update infor : ", res);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
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

        };

        RequestQueue queue = Volley.newRequestQueue(this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
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