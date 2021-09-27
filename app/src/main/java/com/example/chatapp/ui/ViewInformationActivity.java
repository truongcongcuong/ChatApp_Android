package com.example.chatapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuInformationAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.MenuItem;
import com.example.chatapp.dto.UserDetailDTO;
import com.example.chatapp.entity.User;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewInformationActivity extends AppCompatActivity {
    ImageButton ibt_update_infor_back;
    ImageView img_update_infor_avt;
    Button btn_update_infor;
    ListView lsv_update_infor;
    List<MenuItem> items;
    NestedScrollView nsv_update_infor;
    String userToString,token;
    User user;
    Gson gson;
    UserDetailDTO userDetailDTO;
    public static final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information);
        getSupportActionBar().hide();

        lsv_update_infor = findViewById(R.id.lsv_update_infor);
        ibt_update_infor_back = findViewById(R.id.ibt_update_infor_back);
        img_update_infor_avt = findViewById(R.id.img_update_infor_avt);
        btn_update_infor = findViewById(R.id.btn_update_infor);
        nsv_update_infor = findViewById(R.id.nsv_update_infor);

//        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", MODE_PRIVATE);
//        userToString = sharedPreferencesUser.getString("user-infor",null);
        gson = new Gson();
//        user = gson.fromJson(userToString,User.class);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        getInformationDetail();
        ibt_update_infor_back.setOnClickListener(v-> finish());
        img_update_infor_avt.setOnClickListener(v-> showDialogChangeAvt());
        btn_update_infor.setOnClickListener(v->{
            Intent intent = new Intent(ViewInformationActivity.this, UpdateInformationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user",userDetailDTO);
            Log.e("user bundle :",userDetailDTO.toString());
            intent.putExtras(bundle);
            startActivity(intent);
        });


    }


    private void showDialogChangeAvt() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_avatar);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView txt_change_avt_view = dialog.findViewById(R.id.txt_change_avt_view);
        TextView txt_change_avt_take_photo = dialog.findViewById(R.id.txt_change_avt_take_photo);
        TextView txt_change_avt_from_gallery = dialog.findViewById(R.id.txt_change_avt_from_gallery);

        txt_change_avt_view.setOnClickListener(v-> {
//            txt_change_avt_view.setBackgroundColor(getResources().getColor(R.color.gray));
            Toast.makeText(this,"Action View photo active",Toast.LENGTH_LONG).show();
        });
        txt_change_avt_take_photo.setOnClickListener(v-> {
            Toast.makeText(this, "Action take photo active", Toast.LENGTH_LONG).show();
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,GalleryPick);
        });
        txt_change_avt_from_gallery.setOnClickListener(v-> Toast.makeText(this,"Action take photo from gallery active",Toast.LENGTH_LONG).show());


        dialog.show();
    }

    private void getInformationDetail() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_USER +"me",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        this.userDetailDTO = gson.fromJson(String.valueOf(object),UserDetailDTO.class);
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                    updateItems();
                },
                error -> {
                    Log.e("Error information : ",error.toString());
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void updateItems() {
        items = new ArrayList<>();
        items.add(MenuItem.builder()
                .key(getResources().getString(R.string.name))
                .name(userDetailDTO.getDisplayName())
                .build());
        items.add(MenuItem.builder()
                .key(getResources().getString(R.string.username))
                .name(userDetailDTO.getUsername())
                .build());
        items.add(MenuItem.builder()
                .key(getResources().getString(R.string.gender))
                .name(userDetailDTO.getGender())
                .build());
        items.add(MenuItem.builder()
                .key(getResources().getString(R.string.birthday))
                .name(userDetailDTO.getDateOfBirth())
                .build());
        items.add(MenuItem.builder()
                .key(getResources().getString(R.string.mobile))
                .name(userDetailDTO.getPhoneNumber())
                .build());
        MenuInformationAdapter adapter = new MenuInformationAdapter(this,items,R.layout.line_item_menu_button);
        lsv_update_infor.setAdapter(adapter);
        Glide.with(this)
                .load(userDetailDTO.getImageUrl())
                .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                .into(img_update_infor_avt);

    }
}