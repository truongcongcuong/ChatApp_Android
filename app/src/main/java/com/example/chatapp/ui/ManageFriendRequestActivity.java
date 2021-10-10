package com.example.chatapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.ManageFriendRequestAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.FriendRequestSentDto;
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

public class ManageFriendRequestActivity extends AppCompatActivity {
    ImageButton ibt_manage_friend_request_back;
    RecyclerView rcv_manage_friend_request;
    List<FriendRequestSentDto> list;
    String token;
    Gson gson = new Gson();
    ManageFriendRequestAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friend_request);

        ibt_manage_friend_request_back = findViewById(R.id.ibt_manage_friend_request_back);
        rcv_manage_friend_request = findViewById(R.id.rcv_manage_friend_request);
        ibt_manage_friend_request_back.setOnClickListener(v->finish());


        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();
        getDataFromServer();


    }

    private void getDataFromServer() {
        Log.e("Load-data","true");
        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST+"/sent"+"?size=" + Integer.MAX_VALUE + "&page=0",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response,"iso8859-1"),"UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        Type listType = new TypeToken<List<FriendRequestSentDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        setDataToRCV();
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }

                },error -> {
                    error.printStackTrace();

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

    private void setDataToRCV() {
        adapter = new ManageFriendRequestAdapter(list,this);
        rcv_manage_friend_request.setAdapter(adapter);
        rcv_manage_friend_request.setLayoutManager(new LinearLayoutManager(this));
    }
}