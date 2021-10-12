package com.example.chatapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapterVertical;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MenuItem;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.ChatActivity;
import com.example.chatapp.ui.ViewProfileActivity;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileDialog extends Dialog {

    private UserProfileDto userProfileDto;
    private UserSummaryDTO user;
    private Gson gson;
    private String token;
    private Context context;
    private ListView listView;
    private List<MenuItem> menuItems;
    private MenuButtonAdapterVertical menuAdapter;
    private ImageView profile_image_dialog;
    private TextView profile_name_dialog;


    private ProfileDialog(@NonNull Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ProfileDialog(@NonNull Context context, UserProfileDto userProfileDto, List<MenuItem> listMenu) {
        super(context);
        this.userProfileDto = userProfileDto;
        this.context = context;

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        if (listMenu == null || listMenu.isEmpty()) {
            menuItems = new ArrayList<>();
            menuItems.add(MenuItem.builder()
                    .key("chat")
                    .name("Nhắn tin")
                    .imageResource(R.drawable.ic_round_message_24_blue)
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("viewProfile")
                    .name("Xem trang cá nhân")
                    .imageResource(R.drawable.ic_baseline_profile_circle_24_orange)
                    .build());
        } else {
            menuItems = listMenu;
        }

        setContentView(R.layout.layout_profile_dialog);
        listView = findViewById(R.id.lv_profile_dialog);
        profile_image_dialog = findViewById(R.id.profile_image_dialog);
        profile_name_dialog = findViewById(R.id.profile_name_dialog);

        menuAdapter = new MenuButtonAdapterVertical(context, R.layout.line_item_menu_button_vertical, menuItems);
        listView.setAdapter(menuAdapter);
        if (userProfileDto != null) {
            Glide.with(context).load(userProfileDto.getImageUrl())
                    .placeholder(R.drawable.image_placeholer)
                    .centerCrop().circleCrop()
                    .into(profile_image_dialog);
            profile_name_dialog.setText(userProfileDto.getDisplayName());

            listView.setOnItemClickListener((parent, view, position, itemId) -> {
                MenuItem item = menuItems.get(position);
                if (item.getKey().equals("chat") && userProfileDto != null)
                    getInboxWith(userProfileDto.getId());
                else if (item.getKey().equals("viewProfile")) {
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userProfileDto.getId());
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        getWindow().setBackgroundDrawableResource(R.drawable.dark_background_dialog_circle);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setAttributes(layoutParams);

    }

    private void getInboxWith(String anotherUserId) {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "/with/" + anotherUserId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        InboxDto dto = gson.fromJson(res, InboxDto.class);

                        Intent intent = new Intent(context, ChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", dto);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("friend list error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

}