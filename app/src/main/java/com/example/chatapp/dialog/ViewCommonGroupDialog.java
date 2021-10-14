package com.example.chatapp.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.CommonGroupAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.UserProfileDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCommonGroupDialog extends Dialog {

    private UserProfileDto userProfileDto;
    private List<InboxDto> commonGroup;
    private Gson gson;
    private String token;
    private Context context;
    private CommonGroupAdapter commonGroupAdapter;

    private ViewCommonGroupDialog(@NonNull Context context) {
        super(context);
    }

    public ViewCommonGroupDialog(@NonNull Context context, UserProfileDto userProfileDto) {
        super(context);
        this.userProfileDto = userProfileDto;
        this.context = context;

        gson = new Gson();

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        setContentView(R.layout.layout_view_common_group);
        RecyclerView recyclerView = findViewById(R.id.rcv_common_group_dialog);
        TextView titleOfDialog = findViewById(R.id.txt_common_group_dialog_title);
        ImageView imv_close = findViewById(R.id.imv_commonGroup_close);

        imv_close.setOnClickListener(v -> cancel());

        commonGroupAdapter = new CommonGroupAdapter(context, commonGroup);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(commonGroupAdapter);
//        recyclerView.setOnItemClickListener((parent, view, pos, itemId) -> {
//
//        });
        titleOfDialog.setText("Nhóm chung với " + userProfileDto.getDisplayName());

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        layoutParams.height = (int) (displayHeight * 0.5f);
        getWindow().setAttributes(layoutParams);
        getListCommonGroup();

    }

    private void getListCommonGroup() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_ROOM + "commonGroup/" + userProfileDto.getId(),
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONArray array = new JSONArray(res);
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        commonGroup = gson.fromJson(array.toString(), listType);
                        Log.d("===", commonGroup.toString());
                        commonGroupAdapter.setList(commonGroup);
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("common group error", error.toString())) {
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
