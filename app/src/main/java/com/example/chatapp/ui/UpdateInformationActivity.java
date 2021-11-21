package com.example.chatapp.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.UserDetailDTO;
import com.example.chatapp.dto.UserUpdateDTO;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lombok.SneakyThrows;

public class UpdateInformationActivity extends AppCompatActivity {
    private TextInputLayout edt_edit_profile_display_name;
    private TextInputLayout edt_edit_profile_username;
    private TextInputLayout edt_edit_profile_birthday;
    private TextView update_info_txt_error;
    private ProgressBar update_info_progress_bar;
    private Button btn_edit_profile_save;
    private RadioButton rbt_edit_profile_gender_male;
    private RadioButton rbt_edit_profile_gender_female;
    private UserDetailDTO userDetailDTO;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private String token;
    private Date birthday;
    private Gson gson;
    private Timer timer;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

        timer = new Timer();
        gson = new Gson();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            userDetailDTO = (UserDetailDTO) bundle.getSerializable("user");

        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        ImageView img_edit_profile_avt = findViewById(R.id.img_edit_profile_avt);
        edt_edit_profile_display_name = findViewById(R.id.edt_edit_profile_display_name);
        edt_edit_profile_username = findViewById(R.id.edt_edit_profile_username);
        edt_edit_profile_birthday = findViewById(R.id.edt_edit_profile_birthday);
        rbt_edit_profile_gender_male = findViewById(R.id.rbt_edit_profile_gender_male);
        rbt_edit_profile_gender_female = findViewById(R.id.rbt_edit_profile_gender_female);
        btn_edit_profile_save = findViewById(R.id.btn_edit_profile_save);
        update_info_txt_error = findViewById(R.id.update_info_txt_error);
        update_info_progress_bar = findViewById(R.id.update_info_progress_bar);
        update_info_progress_bar.setVisibility(View.GONE);

        Glide.with(this).load(userDetailDTO.getImageUrl())
                .placeholder(R.drawable.img_avatar_placeholer)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop().circleCrop().into(img_edit_profile_avt);

        displayInformation();

        edt_edit_profile_birthday.getEditText().setOnClickListener(v -> {
            int year;
            int month;
            int date;
            Calendar calendar = Calendar.getInstance();
            try {
                Date d = sdf.parse(edt_edit_profile_birthday.getEditText().getText().toString());
                calendar.setTime(d);
            } catch (ParseException ignored) {
            }
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            date = calendar.get(Calendar.DAY_OF_MONTH);

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
                month++;
                birthday = sdf.parse(year + "-" + month + "-" + dayOfMonth);
                Log.e("birthday : ", birthday.toString());
                String s = String.format("%d-%02d-%02d", year, month, dayOfMonth);
                edt_edit_profile_birthday.getEditText().setText(s);
            }
        };

        btn_edit_profile_save.setOnClickListener(v -> {
            if (edt_edit_profile_display_name.getEditText().getText().toString().trim().isEmpty()) {
                edt_edit_profile_display_name.setError(getString(R.string.check_name_empty));
                return;
            }
            edt_edit_profile_display_name.setError(null);
            String gender;
            if (rbt_edit_profile_gender_male.isChecked())
                gender = rbt_edit_profile_gender_male.getText().toString();
            else
                gender = rbt_edit_profile_gender_female.getText().toString();
            UserUpdateDTO dto = UserUpdateDTO.builder()
                    .dateOfBirth(edt_edit_profile_birthday.getEditText().getText().toString())
                    .displayName(edt_edit_profile_display_name.getEditText().getText().toString())
                    .email(userDetailDTO.getEmail())
                    .username(edt_edit_profile_username.getEditText().getText().toString())
                    .gender(gender)
                    .build();
            Log.e("user builder : ", dto.toString());
            btn_edit_profile_save.setVisibility(View.GONE);
            update_info_progress_bar.setVisibility(View.VISIBLE);
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateInfo(dto);
                }
            }, 1500);
        });

    }

    private void displayInformation() {
        if (userDetailDTO.getDateOfBirth() != null) {
            try {
                edt_edit_profile_birthday.getEditText().setText(sdf.format(sdf2.parse(userDetailDTO.getDateOfBirth())));
            } catch (ParseException e) {
                edt_edit_profile_birthday.getEditText().setText("");
            }
        }
        edt_edit_profile_display_name.getEditText().setText(userDetailDTO.getDisplayName());
        edt_edit_profile_username.getEditText().setText(userDetailDTO.getUsername());
        if (userDetailDTO.getUsername() != null && !userDetailDTO.getUsername().trim().isEmpty()) {
            edt_edit_profile_username.setEnabled(false);
        }
        if ("nam".equalsIgnoreCase(userDetailDTO.getGender()))
            rbt_edit_profile_gender_male.setChecked(true);
        else
            rbt_edit_profile_gender_female.setChecked(true);
    }

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
                    btn_edit_profile_save.setVisibility(View.VISIBLE);
                    update_info_progress_bar.setVisibility(View.GONE);
                    userDetailDTO = gson.fromJson(response.toString(), UserDetailDTO.class);
                    update_info_txt_error.setTextColor(getResources().getColor(R.color.susscess));
                    update_info_txt_error.setText(getString(R.string.update_info_success));
                    displayInformation();
                    sendBroadcastUpdateSuccess(userDetailDTO);
                },
                error -> {
                    btn_edit_profile_save.setVisibility(View.VISIBLE);
                    update_info_progress_bar.setVisibility(View.GONE);
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            update_info_txt_error.setTextColor(Color.RED);
                            update_info_txt_error.setText(object.get("message").toString());
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

    private void sendBroadcastUpdateSuccess(UserDetailDTO userDetailDTO) {
        UpdateInformationActivity.this.runOnUiThread(() -> {
            Intent intent = new Intent("user/update/success");
            Bundle bundle = new Bundle();
            bundle.putSerializable("dto", userDetailDTO);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(UpdateInformationActivity.this).sendBroadcast(intent);
        });
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