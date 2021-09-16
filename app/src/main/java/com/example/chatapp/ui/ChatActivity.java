package com.example.chatapp.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.CroppedDrawable;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.cons.SendData;
import com.example.chatapp.cons.WebsocketClient;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MemberDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MessageSendToServer;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.MemberInRoom;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.Json;


public class ChatActivity extends AppCompatActivity implements MessageAdapter.LoadEarlierMessages, SendData {

    ImageView img_chat_user_avt;
    TextView txt_chat_user_name;
    RecyclerView rcv_chat_list;
    EditText edt_chat_message_send;
    ImageButton ibt_chat_send_message, ibt_chat_back;
    Toolbar tlb_chat;
    private String enteredMessage, displayName, url;
    InboxDto dto;
    GetNewAccessToken getNewAccessToken;
    MessageAdapter adapter;
    Gson gson;
    int page = 0;
    UserSummaryDTO user;
    LinearLayoutManager linearLayoutManager;
    String access_token;
    WebsocketClient websocketClient;
    Map<String, CroppedDrawable> members = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().hide();

        ibt_chat_back = findViewById(R.id.ibt_chat_back);
        txt_chat_user_name = findViewById(R.id.txt_chat_user_name);
        rcv_chat_list = findViewById(R.id.rcv_chat_list);
        edt_chat_message_send = findViewById(R.id.edt_chat_message_send);
        ibt_chat_send_message = findViewById(R.id.ibt_chat_send_message);
        tlb_chat = findViewById(R.id.tlb_chat);
        img_chat_user_avt = findViewById(R.id.img_chat_user_avt);

        gson = new Gson();

        Bundle bundle = getIntent().getExtras();
        dto = (InboxDto) bundle.getSerializable("dto");
       /* Log.e("memberDto", dto.toString());
        for(MemberDto memberDto :dto.getRoom().getMembers()){
            MemberInRoom memberInRoom = new MemberInRoom();
            Bitmap bitmap = null;
                try {
                    URL urlOnl = new URL(memberDto.getUser().getImageUrl());
                    bitmap = BitmapFactory.decodeStream(urlOnl.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CroppedDrawable cd = new CroppedDrawable(bitmap);
                memberInRoom.setDrawable(cd);
                members.put(memberDto.getUser().getId(), cd);
        }*/


        Log.e("user ", dto.toString());
        getMemberInRoom();

        getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();

        if (dto.getRoom().getType().equalsIgnoreCase("GROUP")) {
            displayName = dto.getRoom().getName();
            url = dto.getRoom().getImageUrl();
        } else {
            displayName = dto.getRoom().getTo().getDisplayName();
            url = dto.getRoom().getTo().getImageUrl();
        }
        txt_chat_user_name.setText(displayName);
        try {
            URL urlOnl = new URL(url);
            Bitmap bitmap = BitmapFactory.decodeStream(urlOnl.openConnection().getInputStream());
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            CroppedDrawable cd = new CroppedDrawable(bitmap);
            img_chat_user_avt.setImageDrawable(cd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", MODE_PRIVATE);
        access_token = sharedPreferencesToken.getString("access_token", null);


        updateList();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rcv_chat_list.setLayoutManager(linearLayoutManager);

//         setup socket


        // evt button

        ibt_chat_back.setOnClickListener(v -> {
            finish();
        });

        ibt_chat_send_message.setOnClickListener(v -> {
            String message = edt_chat_message_send.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                Log.e("test send pt : ", "suscc");
                sendMessage(message);
            } else Log.e("test send pt : ", "failed");
        });

        websocketClient = new WebsocketClient();
        websocketClient.connect(user.getId(), user.getAccessToken(),ChatActivity.this);
    }

    private void getMemberInRoom() {
        Map<String, CroppedDrawable> members = new HashMap();
        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = sharedPreferencesToken.getString("access-token", null);
        StringRequest request = new StringRequest(Request.Method.GET,Constant.API_GET_MEMBERS+dto.getRoom().getId(),
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONArray array = new JSONArray(res);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject objectInbox = new JSONObject(String.valueOf(array.getJSONObject(i)));
                            MemberDto memberDto = gson.fromJson(objectInbox.toString(), MemberDto.class);
                            Bitmap bitmap = null;
                            try {
                                URL urlOnl = new URL(memberDto.getUser().getImageUrl());
                                bitmap = BitmapFactory.decodeStream(urlOnl.openConnection().getInputStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            CroppedDrawable cd = new CroppedDrawable(bitmap);
                            members.put(memberDto.getUser().getId(),cd);
                        }
                        saveMap(members);

                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                },error -> {

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        Log.e("members ou1",members.toString());
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
        Log.e("members ou2",members.toString());
    }

    private void sendMessage(String message) {
        MessageSendToServer messageSendToServer = new MessageSendToServer();
        messageSendToServer.setContent(message);
        messageSendToServer.setRoomId(dto.getRoom().getId());
        messageSendToServer.setType("TEXT");
        Log.e("send : ", Json.encode(messageSendToServer));
        websocketClient.send(Json.encode(messageSendToServer));

    }


    private void loadMoreData() {
        page++;
        adapter.setLoadEarlierMsgs(true);
        updateList();
    }


    private void updateList() {
        List<MessageDto> list = new ArrayList<>();
        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = sharedPreferencesToken.getString("access-token", null);


        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_CHAT + dto.getId()+"?size=15&page="+page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject objectInbox = new JSONObject(String.valueOf(array.getJSONObject(i)));
                            MessageDto messageDto = gson.fromJson(objectInbox.toString(), MessageDto.class);
                            list.add(messageDto);
                        }
                        if (page == 0) {
                            Log.e("member 1", members.toString());
                            adapter = new MessageAdapter(ChatActivity.this, list, members);
                            rcv_chat_list.setAdapter(adapter);
                            rcv_chat_list.scrollToPosition(list.size());
                        } else {
                            View v = rcv_chat_list.getChildAt(0);
                            int top = (v == null) ? 0 : v.getTop();
                            adapter.updateList(list);
                            rcv_chat_list.scrollToPosition(16);

                        }

                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.i("error", error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void updateMessageRealTime(MessageDto messageDto){
        this.adapter.updateMessage(messageDto);
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                rcv_chat_list.scrollToPosition(adapter.getItemCount());
                rcv_chat_list.getLayoutManager().smoothScrollToPosition(rcv_chat_list, new RecyclerView.State(),rcv_chat_list.getAdapter().getItemCount());
            }
        });
    }

    public void saveMap(Map<String,CroppedDrawable> map){
        this.members = map;
        Log.e("map : ", map.toString());
        Log.e("members : ", members.toString());
    }

    @Override
    public void onLoadEarlierMessages() {
        page++;
        updateList();
    }

    @Override
    public void SendingData(String s) {
        MessageDto messageDto = gson.fromJson(s,MessageDto.class);
        updateMessageRealTime(messageDto);
    }
}