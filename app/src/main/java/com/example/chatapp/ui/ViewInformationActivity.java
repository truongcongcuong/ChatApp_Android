package com.example.chatapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

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
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuInformationAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dialog.ChangeAvatarDialog;
import com.example.chatapp.dto.MyMenuItem;
import com.example.chatapp.dto.UserDetailDTO;
import com.example.chatapp.utils.MultiPartFileRequest;
import com.example.chatapp.utils.PathUtil;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

public class ViewInformationActivity extends AppCompatActivity {
    private ImageView img_update_info_avt;
    private ListView lsv_update_info;
    //    private String userToString;
    private String token;
    //    private User user;
    private Gson gson;
    private UserDetailDTO userDetailDTO;
    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_update_info_activity);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        lsv_update_info = findViewById(R.id.lsv_update_infor);
        img_update_info_avt = findViewById(R.id.img_update_infor_avt);
        Button btn_update_info = findViewById(R.id.btn_update_infor);
//        NestedScrollView nsv_update_infor = findViewById(R.id.nsv_update_infor);

//        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", MODE_PRIVATE);
//        userToString = sharedPreferencesUser.getString("user-infor",null);
        gson = new Gson();
//        user = gson.fromJson(userToString,User.class);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        getInformationDetail();
        img_update_info_avt.setOnClickListener(v -> showDialogChangeAvt());
        btn_update_info.setOnClickListener(v -> {
            Intent intent = new Intent(ViewInformationActivity.this, UpdateInformationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", userDetailDTO);
            Log.e("user bundle :", userDetailDTO.toString());
            intent.putExtras(bundle);
            startActivity(intent);
        });

    }

    private void showDialogChangeAvt() {
        ChangeAvatarDialog changeAvatarDialog = new ChangeAvatarDialog(this, REQUEST_GALLERY, REQUEST_IMAGE_CAPTURE, null, userDetailDTO);
        changeAvatarDialog.show();
    }

    private void getInformationDetail() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_USER + "me",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        this.userDetailDTO = gson.fromJson(String.valueOf(object), UserDetailDTO.class);
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                    updateItems();
                },
                error -> {
                    Log.e("Error information : ", error.toString());
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

    @SneakyThrows
    private void updateItems() {
        setTitle(userDetailDTO.getDisplayName());
        List<MyMenuItem> items = new ArrayList<>();
        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.name))
                .name(userDetailDTO.getDisplayName())
                .build());
        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.username))
                .name(userDetailDTO.getUsername())
                .build());
        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.gender))
                .name(userDetailDTO.getGender())
                .build());
        if (userDetailDTO.getDateOfBirth() != null) {
            items.add(MyMenuItem.builder()
                    .key(getResources().getString(R.string.birthday))
                    .name(sdf.format(sdfFull.parse(userDetailDTO.getDateOfBirth())))
                    .build());
        }

        items.add(MyMenuItem.builder()
                .key(getResources().getString(R.string.mobile))
                .name(userDetailDTO.getPhoneNumber())
                .build());
        MenuInformationAdapter adapter = new MenuInformationAdapter(this, items, R.layout.line_item_menu_button_vertical);
        lsv_update_info.setAdapter(adapter);
        Glide.with(this)
                .load(userDetailDTO.getImageUrl())
                .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                .into(img_update_info_avt);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("test 110,", "errrr");
        List<File> files = new ArrayList<>();
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get("data");
                    Log.e("uri camera", selectedImage.toString());
                    Glide.with(this).load(selectedImage)
                            .centerCrop().circleCrop().into(img_update_info_avt);
                    files.add(new File(PathUtil.getPath(this, getImageUri(this, selectedImage))));
                    userDetailDTO.setImageUrl(getImageUri(this, selectedImage).toString());
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Glide.with(this).load(selectedImage)
                            .centerCrop().circleCrop().into(img_update_info_avt);
                    Log.e("gallery", "done");
                    files.add(new File(PathUtil.getPath(this, selectedImage)));
                }
                break;
        }
        uploadMultiFiles(files);
    }

    private void uploadMultiFiles(List<File> files) {
        MultiPartFileRequest<String> restApiMultiPartRequest =
                new MultiPartFileRequest<String>(Request.Method.PUT, Constant.API_USER + "me/changeImage",
                        new HashMap<>(), // danh sách request param
                        files,
                        response -> {
                            Log.i("upload succ", "success");
                        },
                        error -> {
                            Log.i("upload error", "error");
                            NetworkResponse response = error.networkResponse;
                            if (error instanceof ServerError) {
                                try {
                                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
//                                    JSONObject object = new JSONObject(res);
                                    Log.e("400 : ", res);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }) {

                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + token);
                        return map;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        return params;
                    }
                };

//        restApiMultiPartRequest.setRetryPolicy(new DefaultRetryPolicy(0, 1, 2));//10000
        Volley.newRequestQueue(this).add(restApiMultiPartRequest);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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