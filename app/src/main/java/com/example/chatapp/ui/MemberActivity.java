package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MemberAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MemberDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberActivity extends AppCompatActivity {

    private ImageButton ibt_back;
    private ImageButton ibt_add_member;
    private TextView title;
    private ListView lv_members;
    private String token;
    private Gson gson;
    private InboxDto inboxDto;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(0.5f)
                .build();

        Slidr.attach(this, config);

        ibt_back = findViewById(R.id.ibt_member_detail_back);
        ibt_add_member = findViewById(R.id.ibt_member_add);
        title = findViewById(R.id.txt_member_detail_title);
        lv_members = findViewById(R.id.lv_member);

        ibt_back.setOnClickListener(v -> onBackPressed());
        ibt_add_member.setOnClickListener(v -> {
            Toast.makeText(this, "add memeber", Toast.LENGTH_SHORT).show();
        });

        gson = new Gson();
        SharedPreferences sharedPreferencesToken = MemberActivity.this.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        title.setText("Thành viên");

        Bundle bundle = getIntent().getExtras();
        inboxDto = (InboxDto) bundle.getSerializable("dto");

        if (inboxDto != null) {
            StringRequest request = new StringRequest(Request.Method.GET, Constant.API_ROOM + "members/" + inboxDto.getRoom().getId(),
                    response -> {
                        try {
                            String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                            Type listType = new TypeToken<List<MemberDto>>() {
                            }.getType();
                            List<MemberDto> memberDtos = gson.fromJson(res, listType);

                            MemberAdapter adapter = new MemberAdapter(MemberActivity.this, R.layout.line_item_member, memberDtos);
                            lv_members.setAdapter(adapter);
                            lv_members.setOnItemClickListener((parent, view, pos, itemId) -> {

                            });
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.i("get members error", error.toString())) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(MemberActivity.this);
            requestQueue.add(request);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

}