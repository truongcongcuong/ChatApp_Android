package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.FriendRequestAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.entity.FriendRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestActivity extends AppCompatActivity {
    ImageButton ibt_friend_request_back;
    TextView txt_fiend_request_total_friend_request;
    RecyclerView lsv_friend_request_list;
    String token;
    String totalFriendRequest;
    Gson gson;
    FriendRequestAdapter adapter;
    List<FriendRequest> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        gson = new Gson();

        ibt_friend_request_back = findViewById(R.id.ibt_friend_request_back);
        txt_fiend_request_total_friend_request = findViewById(R.id.txt_fiend_request_total_friend_request);
        lsv_friend_request_list = findViewById(R.id.lsv_friend_request_list);

        ibt_friend_request_back.setOnClickListener(v->finish());

        getListFriendRequest();
    }

    private void getListFriendRequest() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST+"?size=" + Integer.MAX_VALUE + "&page=0" ,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        totalFriendRequest = object.get("totalElements").toString();
                        Type listType = new TypeToken<List<FriendRequest>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        updateData();
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {

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

    private void updateData() {
        adapter = new FriendRequestAdapter(this, list, token);
        lsv_friend_request_list.setAdapter(adapter);
        lsv_friend_request_list.setLayoutManager(new LinearLayoutManager(this));

        txt_fiend_request_total_friend_request.setText(totalFriendRequest);

    }
}