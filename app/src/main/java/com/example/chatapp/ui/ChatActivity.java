package com.example.chatapp.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.example.chatapp.dto.ReactionReceiver;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.ReadByReceiver;
import com.example.chatapp.dto.ReadBySend;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.Reaction;
import com.example.chatapp.utils.PathUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.Json;
import lombok.SneakyThrows;
import ua.naiksoftware.stomp.dto.StompMessage;

public class ChatActivity extends AppCompatActivity implements SendData {

    private ImageView img_chat_user_avt;
    private TextView txt_chat_user_name;
    private RecyclerView rcv_chat_list;
    private EditText edt_chat_message_send;
    private ImageButton ibt_chat_send_message;
    private ImageButton ibt_chat_back;
    private ImageButton ibt_chat_send_media;
    private Toolbar tlb_chat;
    private String displayName, url;
    private InboxDto dto;
    private GetNewAccessToken getNewAccessToken;
    private MessageAdapter adapter;
    private Gson gson;
    private int page = 0;
    private int size = 20;
    private int first = 0;
    private int last = first;
    private UserSummaryDTO user;
    private LinearLayoutManager linearLayoutManager;
    private String access_token;
    private String token;
    private List<MessageDto> list;
    private Button btnScrollToBottom;
    private static final int PICK_IMAGE = 1;
    private SharedPreferences sharedPreferencesToken;

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.M)
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
        ibt_chat_send_media = findViewById(R.id.ibt_chat_send_media);
        btnScrollToBottom = findViewById(R.id.btn_scroll_to_bottom);

        list = new ArrayList<>();
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

        linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        rcv_chat_list.setLayoutManager(linearLayoutManager);

        // scroll event
        rcv_chat_list.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                first = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findFirstVisibleItemPosition();
                last = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findLastVisibleItemPosition();
                if (!recyclerView.canScrollVertically(-1)) {
                    loadMoreData();
                }
                if ((rcv_chat_list.getAdapter().getItemCount() - last) > (size / 2)) {
                    btnScrollToBottom.setVisibility(View.VISIBLE);
                } else
                    btnScrollToBottom.setVisibility(View.GONE);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                first = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findFirstVisibleItemPosition();
                last = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findLastVisibleItemPosition();

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // scroll to bottom
                }
                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadMoreData();
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // scrolling
                }
                if ((rcv_chat_list.getAdapter().getItemCount() - last) > (size / 2)) {
                    btnScrollToBottom.setVisibility(View.VISIBLE);
                } else
                    btnScrollToBottom.setVisibility(View.GONE);
            }
        });

        ibt_chat_back.setOnClickListener(v -> {
            finish();
        });

        btnScrollToBottom.setOnClickListener(v -> {
            rcv_chat_list.getLayoutManager().smoothScrollToPosition(rcv_chat_list, new RecyclerView.State(), rcv_chat_list.getAdapter().getItemCount());
            btnScrollToBottom.setVisibility(View.GONE);
        });

        ibt_chat_send_media.setOnClickListener(v -> {
            // xin quyền truy cập vào bộ sưu tập
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });

        WebsocketClient.getInstance().getStompClient()
                .topic("/users/queue/messages")
                .subscribe(x -> {
                    Log.i("chat activ subcri mess", x.getPayload());
                    @SuppressLint("CheckResult")
                    MessageDto messageDto = gson.fromJson(x.getPayload(), MessageDto.class);
                    updateMessageRealTime(messageDto);
                }, throwable -> {
                    Log.i("chat activ subcri erro", throwable.getMessage());
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
                    Log.i("chat activ subc read er", throwable.getMessage());
                });

        WebsocketClient.getInstance().getStompClient()
                .topic("/users/queue/reaction")
                .subscribe(x -> {
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {
                            updateReactionMessage(x);
                        }
                    });
                }, throwable -> {
                    Log.i("chat activ react error", throwable.getMessage());
                });

        ibt_chat_send_message.setOnClickListener(v -> {
            String message = edt_chat_message_send.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
                edt_chat_message_send.setText("");
            }
        });

        adapter = new MessageAdapter(ChatActivity.this, list);
        rcv_chat_list.setHasFixedSize(true);
        rcv_chat_list.setAdapter(adapter);
        updateList();
    }

    private void updateReactionMessage(StompMessage x) {
        Log.i("chat activ subcri react", x.getPayload());
        ReactionReceiver receiver = gson.fromJson(x.getPayload(), ReactionReceiver.class);
        for (MessageDto m : list) {
            if (m.getId().equals(receiver.getMessageId())) {
                List<Reaction> reactions = m.getReactions();
                if (reactions == null)
                    reactions = new ArrayList<>();
                Reaction reaction = new Reaction();
                reaction.setType(receiver.getType());
                reaction.setReactByUserId(receiver.getReactByUser().getId());
                reactions.add(reaction);

                m.setReactions(reactions);
            }
        }
        adapter.notifyDataSetChanged();
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

                m.setReadbyes(readbyes);
                Log.i("message readed", m.toString());
            }
            if (m.getId().equals(readbyReceiver.getOldMessageId()) &&
                    !m.getId().equals(readbyReceiver.getMessageId())) {
                Log.i("old message read", m.toString());
                List<ReadByDto> readbyes = m.getReadbyes();
                if (readbyes != null) {
                    readbyes.removeIf(x -> x.getReadByUser().getId().equals(readbyReceiver.getReadByUser().getId()));
                }

                m.setReadbyes(readbyes);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("CheckResult")
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

    @SuppressLint("CheckResult")
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
        updateList();
    }

    private void updateList() {
        list = new ArrayList<>();
        sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        Log.e("url : ", Constant.API_CHAT + dto.getId());
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_CHAT + dto.getId() + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");

                        Type listType = new TypeToken<List<MessageDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        if (!list.isEmpty()) {
                            if (page == 0) {
//                            adapter = new MessageAdapter(ChatActivity.this, list);
//                            rcv_chat_list.setHasFixedSize(true);
//                            rcv_chat_list.setAdapter(adapter);
                                adapter.updateList(list);
                                rcv_chat_list.scrollToPosition(list.size() - 1);
                            } else {
                                adapter.updateList(list);
                                rcv_chat_list.scrollToPosition(size + (last - first) - 1);
                            }
                        }
//                        adapter.notifyDataSetChanged();
//                        sendReadMessageNotification();
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("chat activ get mess er", error.toString())) {
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
                rcv_chat_list.getLayoutManager()
                        .smoothScrollToPosition(rcv_chat_list,
                                new RecyclerView.State(),
                                rcv_chat_list.getAdapter().getItemCount());
            }
        });
        sendReadMessageNotification();
    }

    @Override
    public void SendingData(String s) {
//        MessageDto messageDto = gson.fromJson(s, MessageDto.class);
//        updateMessageRealTime(messageDto);
    }

    @SneakyThrows
    @Override
    // xử lý khi chọn hình ảnh để gửi
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        List<File> files = new ArrayList<>();
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    File file = new File(PathUtil.getPath(ChatActivity.this, imageUri));
                    files.add(file);
                }
            } else {
                File file = new File(PathUtil.getPath(ChatActivity.this, data.getData()));
                files.add(file);
            }
            Toast.makeText(this, "file đã chọn " + files.toString(), Toast.LENGTH_SHORT).show();
        } else {
            // chưa có hình ảnh nào được chọn
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}