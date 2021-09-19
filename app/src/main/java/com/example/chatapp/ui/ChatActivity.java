package com.example.chatapp.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.cons.SendData;
import com.example.chatapp.cons.WebsocketClient;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MessageSendToServer;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.ReadByReceiver;
import com.example.chatapp.dto.ReadBySend;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.Json;
import ua.naiksoftware.stomp.dto.StompMessage;

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
    List<MessageDto> list = new ArrayList<>();

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

        Log.e("user ", dto.toString());

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

        Glide.with(this).load(url).placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(img_chat_user_avt);

        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        Log.e("user-info", user.toString());
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

        WebsocketClient.getInstance().getStompClient()
                .topic("/users/queue/messages")
                .subscribe(x -> {
                    Log.i(">>>receiver", x.getPayload());
                    MessageDto messageDto = gson.fromJson(x.getPayload(), MessageDto.class);
                    updateMessageRealTime(messageDto);
                }, throwable -> {
                    Log.i(">>>receiver error", throwable.getMessage());
                });

        WebsocketClient.getInstance().getStompClient()
                .topic("/users/queue/read")
                .subscribe(x -> {
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {
                            updateReadMessage(x);
                        }
                    });
                }, throwable -> {
                    Log.i(">>>read tracking error", throwable.getMessage());
                });

        ibt_chat_send_message.setOnClickListener(v -> {
            String message = edt_chat_message_send.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                Log.e("test send pt : ", "suscc");
                sendMessage(message);
                edt_chat_message_send.setText("");
            } else Log.e("test send pt : ", "failed");
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateReadMessage(StompMessage stompMessage) {
        ReadByReceiver readbyReceiver = gson.fromJson(stompMessage.getPayload(), ReadByReceiver.class);
        Log.e("readbyreceiver", readbyReceiver.toString());
        for (MessageDto m : list) {
            if (m.getId().equals(readbyReceiver.getMessageId())) {
                List<ReadByDto> readbyes = m.getReadbyes();
                if (readbyes == null)
                    readbyes = new ArrayList<>();
                ReadByDto readByDto = new ReadByDto();
                readByDto.setReadAt(readbyReceiver.getReadAt());
                readByDto.setReadByUser(readbyReceiver.getReadByUser());
                if (!readbyes.contains(readByDto))
                    readbyes.add(readByDto);
                Log.i("message readed", m.toString());
            } else {
                m.setReadbyes(new ArrayList<>());
            }
            if (m.getId().equals(readbyReceiver.getOldMessageId()) &&
                    !m.getId().equals(readbyReceiver.getMessageId())) {
                Log.i("old message read", m.toString());
                List<ReadByDto> readbyes = m.getReadbyes();
                if (readbyes != null) {
                    readbyes.removeIf(x -> x.getReadByUser().getId().equals(readbyReceiver.getReadByUser().getId()));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void sendMessage(String message) {
        MessageSendToServer messageSendToServer = new MessageSendToServer();
        messageSendToServer.setContent(message);
        messageSendToServer.setRoomId(dto.getRoom().getId());
        messageSendToServer.setType("TEXT");
        Log.e("send : ", Json.encode(messageSendToServer));
        WebsocketClient.getInstance().getStompClient()
                .send("/app/chat", Json.encode(messageSendToServer))
                .subscribe(() -> {

                });
    }

    private void sendReadMessageNotification() {
        if (list.size() != 0) {
            MessageDto lastMessage = list.get(list.size() - 1);
            ReadBySend readBySend = ReadBySend.builder()
                    .messageId(lastMessage.getId())
                    .readAt(new Date())
                    .roomId(lastMessage.getRoomId())
                    .userId(user.getId())
                    .build();

            WebsocketClient.getInstance()
                    .getStompClient()
                    .send("/app/read", Json.encode(readBySend))
                    .subscribe(() -> {

                    });
        }
    }

    private void loadMoreData() {
        page++;
        adapter.setLoadEarlierMsgs(true);
        updateList();
    }

    private void updateList() {
        list = new ArrayList<>();
        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = sharedPreferencesToken.getString("access-token", null);
        Log.e("url : ", Constant.API_CHAT + dto.getId());
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_CHAT + dto.getId() + "?size=15&page=" + page,
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
                            adapter = new MessageAdapter(ChatActivity.this, list);
                            rcv_chat_list.setAdapter(adapter);
                            rcv_chat_list.scrollToPosition(list.size());
                        } else {
                            View v = rcv_chat_list.getChildAt(0);
                            int top = (v == null) ? 0 : v.getTop();
                            adapter.updateList(list);
                            rcv_chat_list.scrollToPosition(16);

                        }
                        sendReadMessageNotification();
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

    private void updateMessageRealTime(MessageDto messageDto) {
        this.adapter.updateMessage(messageDto);
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                rcv_chat_list.scrollToPosition(adapter.getItemCount());
                rcv_chat_list.getLayoutManager().smoothScrollToPosition(rcv_chat_list, new RecyclerView.State(), rcv_chat_list.getAdapter().getItemCount());
            }
        });
        sendReadMessageNotification();
    }

    @Override
    public void onLoadEarlierMessages() {
        page++;
        updateList();
    }

    @Override
    public void SendingData(String s) {
        MessageDto messageDto = gson.fromJson(s, MessageDto.class);
        Log.e("da nhan nhe baby : ", messageDto.toString());
        updateMessageRealTime(messageDto);
    }
}