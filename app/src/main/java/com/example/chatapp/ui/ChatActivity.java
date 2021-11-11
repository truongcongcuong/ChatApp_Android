package com.example.chatapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.cons.SendDataReplyMessage;
import com.example.chatapp.cons.SendingData;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MessageSendToServer;
import com.example.chatapp.dto.ReactionReceiver;
import com.example.chatapp.dto.ReadByReceiver;
import com.example.chatapp.dto.ReadBySend;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.enumvalue.MessageType;
import com.example.chatapp.enumvalue.OnlineStatus;
import com.example.chatapp.enumvalue.RoomType;
import com.example.chatapp.utils.FileUtil;
import com.example.chatapp.utils.MultiPartFileRequest;
import com.example.chatapp.utils.PathUtil;
import com.example.chatapp.utils.TimeAgo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Action;
import io.vertx.core.json.Json;
import lombok.SneakyThrows;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompCommand;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class ChatActivity extends AppCompatActivity implements SendingData, SendDataReplyMessage {

    private static final int PICK_IMAGE = 1;
    private static final int VIEW_ROOM_DETAIL = 2;
    private static final int REQUEST_PERMISSION = 3;
    private final int size = 20;
    private int page = 0;
    private int first = 0;
    private int last = first;
    private boolean isFirstTimeRun = true;

    private String access_token;
    private String replyMessageId = null;

    private TextView txt_chat_user_name;
    private TextView txt_chat_detail;
    private TextView txt_name_reply_chat_activity;
    private TextView txt_content_reply_chat_activity;

    private ImageView img_chat_user_avt;
    private RecyclerView rcv_chat_list;
    private EditText edt_chat_message_send;
    private InboxDto inboxDto;
    private MessageAdapter adapter;
    private Gson gson;
    private UserSummaryDTO user;
    private Button btnScrollToBottom;
    private StompClient stompClient;
    private LinearLayout layout_reply_chat_activity;

    private final BroadcastReceiver addMember = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                inboxDto.setRoom(newRoom);
                showImageAndDisplayName(inboxDto);
            }
        }
    };

    private final BroadcastReceiver deleteMember = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                inboxDto.setRoom(newRoom);
                System.out.println("newRoom after delete = " + newRoom);
                showImageAndDisplayName(inboxDto);
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                MessageDto newMessage = (MessageDto) bundle.getSerializable("dto");
                Log.d("-new mess in chat acti", newMessage.toString());

                if (newMessage.getSender() != null)
                    adapter.deleteOldReadTracking(newMessage.getSender().getId());
                updateMessageRealTime(newMessage);
            }
        }
    };

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("messages/new"));
        LocalBroadcastManager.getInstance(this).registerReceiver(addMember, new IntentFilter("room/members/add"));
        LocalBroadcastManager.getInstance(this).registerReceiver(deleteMember, new IntentFilter("room/members/delete"));

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        txt_chat_user_name = findViewById(R.id.txt_chat_user_name);
        txt_chat_detail = findViewById(R.id.txt_chat_detail);
        rcv_chat_list = findViewById(R.id.rcv_chat_list);
        edt_chat_message_send = findViewById(R.id.edt_chat_message_send);
        ImageButton ibt_chat_send_message = findViewById(R.id.ibt_chat_send_message);
        Toolbar tlb_chat = findViewById(R.id.tlb_chat_activity);
        img_chat_user_avt = findViewById(R.id.img_chat_user_avt);
        ImageButton ibt_chat_send_media = findViewById(R.id.ibt_chat_send_media);
        btnScrollToBottom = findViewById(R.id.btn_scroll_to_bottom);

        txt_name_reply_chat_activity = findViewById(R.id.txt_name_reply_chat_activity);
        txt_content_reply_chat_activity = findViewById(R.id.txt_content_reply_chat_activity);
        ImageView img_close_reply_chat_activity = findViewById(R.id.img_close_reply_chat_activity);
        layout_reply_chat_activity = findViewById(R.id.layout_reply_chat_activity);
        layout_reply_chat_activity.setVisibility(View.GONE);

        img_close_reply_chat_activity.setOnClickListener(v -> {
            replyMessageId = null;
            layout_reply_chat_activity.setVisibility(View.GONE);
        });

        gson = new Gson();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            inboxDto = (InboxDto) bundle.getSerializable("dto");

        Log.e("user ", inboxDto.toString());

        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();

        tlb_chat.setTitleTextColor(Color.WHITE);
        tlb_chat.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(tlb_chat);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        showImageAndDisplayName(inboxDto);

        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        Log.e("user-info", user.toString());

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        access_token = sharedPreferencesToken.getString("access-token", null);

        MyLinerLayoutManager linearLayoutManager = new MyLinerLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        rcv_chat_list.setLayoutManager(linearLayoutManager);

        // scroll event
        rcv_chat_list.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                first = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findFirstVisibleItemPosition();
                last = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findLastVisibleItemPosition();
//                if (!recyclerView.canScrollVertically(-1)) {
//                    loadMoreData();
//                }
                visibleOrGoneButtonScrollToBottom();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                first = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findFirstVisibleItemPosition();
                last = ((LinearLayoutManager) rcv_chat_list.getLayoutManager()).findLastVisibleItemPosition();

//                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
//                     scroll to bottom
//                }
                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadMoreData();
                }
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                     scrolling
//                }
                visibleOrGoneButtonScrollToBottom();
            }
        });

        btnScrollToBottom.setOnClickListener(v -> {
            rcv_chat_list.getLayoutManager().smoothScrollToPosition(rcv_chat_list, new RecyclerView.State(), rcv_chat_list.getAdapter().getItemCount());
            btnScrollToBottom.setVisibility(View.GONE);
        });

        ibt_chat_send_media.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_needed_title))
                            .setMessage(getString(R.string.permission_needed_message))
                            .setPositiveButton(getString(R.string.confirm_button), (dialog, which) ->
                                    ActivityCompat.requestPermissions(ChatActivity.this,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            REQUEST_PERMISSION))
                            .setNegativeButton(getString(R.string.cancel_button), (dialog, which) -> dialog.cancel()).create().show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                }
            } else {
                openFileChoose();
            }
        });

//        stompClient = WebsocketClient.getInstance().getStompClient();
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Constant.WEB_SOCKET);
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("userId", user.getId()));
        headers.add(new StompHeader("access_token", access_token));

        Log.i("userId", user.getId());
        Log.i("access_token", access_token);

        stompClient.connect(headers);

//        stompClient
//                .topic("/users/queue/messages")
//                .subscribe(x -> {
//                    Log.i("chat activ subcri mess", x.getPayload());
//                    MessageDto messageDto = gson.fromJson(x.getPayload(), MessageDto.class);
//                    if (messageDto.getSender() != null)
//                        adapter.deleteOldReadTracking(messageDto.getSender().getId());
//                    updateMessageRealTime(messageDto);
//                }, throwable -> {
//                    Log.i("chat activ subcri erro", throwable.getMessage());
//                });

        stompClient
                .topic("/users/queue/read")
                .subscribe(x -> {
                    ReadByReceiver readByReceiver = gson.fromJson(x.getPayload(), ReadByReceiver.class);
                    adapter.updateReadToMessage(readByReceiver);
                }, throwable -> {
                    Log.i("chat activ subc read er", throwable.getMessage());
                });

        stompClient
                .topic("/users/queue/reaction")
                .subscribe(x -> {
                    ReactionReceiver receiver = gson.fromJson(x.getPayload(), ReactionReceiver.class);
                    adapter.updateReactionToMessage(receiver);
                }, throwable -> {
                    Log.i("chat activ react error", throwable.getMessage());
                });

        stompClient
                .topic("/users/queue/messages/delete")
                .subscribe(x -> {
                    Log.d("--deleted mes", x.getPayload());
                    MessageDto deletedMessage = gson.fromJson(x.getPayload(), MessageDto.class);
                    adapter.updateDeletedMessage(deletedMessage);
                }, throwable -> {
                    Log.i("chat activ react error", throwable.getMessage());
                });

        /*
        sự kiện focus trên edittext
         */
        edt_chat_message_send.setOnFocusChangeListener((v, hasFocus) -> {
            /*
            không focus thì ẩn bàn phím
             */
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edt_chat_message_send.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        ibt_chat_send_message.setOnClickListener(v -> {
            String message = edt_chat_message_send.getText().toString().trim();
            if (!message.isEmpty()) {
                checkInboxExistsBeforeSendMessage(message, MessageType.TEXT);
                edt_chat_message_send.getText().clear();
                edt_chat_message_send.setText("");
                replyMessageId = null;
                layout_reply_chat_activity.setVisibility(View.GONE);
                edt_chat_message_send.postDelayed(() -> edt_chat_message_send.requestFocus(), 200);
            } else {
                edt_chat_message_send.requestFocus();
                /*
                mở bàn phím
                 */
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edt_chat_message_send, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        adapter = new MessageAdapter(ChatActivity.this, new ArrayList<>());
        rcv_chat_list.setAdapter(adapter);
        updateList();
    }

    private void showImageAndDisplayName(InboxDto inboxDto) {
        String displayName = "";
        String url = "";
        String detail = "";
        if (inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
            displayName = inboxDto.getRoom().getName();
            url = inboxDto.getRoom().getImageUrl();
            if (inboxDto.getRoom().getMembers() != null)
                detail = String.format("%d %s", inboxDto.getRoom().getMembers().size(), getString(R.string.members));
            else
                detail = String.format("%d %s", 0, getString(R.string.members));
        } else {
            displayName = inboxDto.getRoom().getTo().getDisplayName();
            url = inboxDto.getRoom().getTo().getImageUrl();
            if (inboxDto.getRoom().getTo().getOnlineStatus().equals(OnlineStatus.ONLINE))
                detail = getString(R.string.present_online);
            else {
                try {
                    detail = String.format("%s %s", getString(R.string.online), TimeAgo.getTime(inboxDto.getRoom().getTo().getLastOnline()));
                } catch (ParseException e) {
                    detail = "";
                }
            }
        }
        txt_chat_user_name.setText(displayName);
        txt_chat_detail.setText(detail);
        final Context context = getApplication().getApplicationContext();

        if (isValidContextForGlide(context)) {
            Glide.with(context).load(url).placeholder(R.drawable.image_placeholer)
                    .centerCrop().circleCrop().into(img_chat_user_avt);
        }
    }

    public boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    // nếu cuộn quá size/3 item thì hiện nút bấm để xuống cuối
    private void visibleOrGoneButtonScrollToBottom() {
        if (rcv_chat_list.getAdapter() != null && (rcv_chat_list.getAdapter().getItemCount() - last) > (size / 3)) {
            btnScrollToBottom.setVisibility(View.VISIBLE);
        } else
            btnScrollToBottom.setVisibility(View.GONE);
    }

    @SuppressLint("CheckResult")
    private void checkInboxExistsBeforeSendMessage(String message, MessageType messageType) {
        if (inboxDto.getId() == null) {
            createInboxIfNotExistsAndSendMessage(inboxDto.getRoom().getTo().getId(), message, messageType);
        } else {
            sendMessage(message, messageType);
        }
    }

    /*
    sự kiện nhấn icon back trên toolbar
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void sendMessage(String message, MessageType type) {
        MessageSendToServer messageSendToServer = new MessageSendToServer();
        messageSendToServer.setContent(message);
        messageSendToServer.setRoomId(inboxDto.getRoom().getId());
        messageSendToServer.setReplyId(replyMessageId);
        messageSendToServer.setType(type);
        Log.e("send : ", Json.encode(messageSendToServer));

        stompClient
                .send("/app/chat", Json.encode(messageSendToServer))
                .subscribe(() -> {

                });
    }

    /*
    hai người chưa có room chung nên phải tạo room và inbox trước khi gửi tin nhắn đầu tiên
     */
    private void createInboxIfNotExistsAndSendMessage(String toUserId, String message, MessageType messageType) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_INBOX + "/with/" + toUserId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        inboxDto = gson.fromJson(res, InboxDto.class);
                        sendMessage(message, messageType);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("chat activ send mes err", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + access_token);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @SuppressLint("CheckResult")
    private void sendReadMessageNotification() {
        Log.d("--------", "send read mess");
        MessageDto lastMessage = adapter.getLastMessage();
        if (lastMessage != null) {
            ReadBySend readBySend = ReadBySend.builder()
                    .messageId(lastMessage.getId())
                    .readAt(new Date())
                    .roomId(lastMessage.getRoomId())
                    .userId(user.getId())
                    .build();

            stompClient
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
        Log.e("url : ", Constant.API_CHAT + inboxDto.getId());
        if (inboxDto != null && inboxDto.getId() != null) {
            StringRequest request = new StringRequest(Request.Method.GET, Constant.API_CHAT + inboxDto.getId() + "?size=" + size + "&page=" + page,
                    response -> {
                        try {
                            String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                            JSONObject object = new JSONObject(res);
                            JSONArray array = (JSONArray) object.get("content");

                            Type listType = new TypeToken<List<MessageDto>>() {
                            }.getType();
                            List<MessageDto> list = gson.fromJson(array.toString(), listType);
                            if (!list.isEmpty()) {
                                adapter.updateList(list);
                                if (isFirstTimeRun) {
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(1500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        sendReadMessageNotification();
                                    }).start();
                                    isFirstTimeRun = false;
                                }
                                if (page == 0) {
                                    rcv_chat_list.scrollToPosition(list.size() - 1);
                                } else {
                                    rcv_chat_list.scrollToPosition(size + (last - first) - 1);
                                }
                            }
                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.i("chat activ get mess er", error.toString())) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + access_token);
                    return map;
                }
            };

            DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(retryPolicy);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateMessageRealTime(MessageDto messageDto) {
        if (messageDto.getRoomId().equals(inboxDto.getRoom().getId())) {
            this.adapter.updateMessage(messageDto);
            ChatActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    rcv_chat_list.getLayoutManager()
                            .smoothScrollToPosition(rcv_chat_list,
                                    new RecyclerView.State(),
                                    rcv_chat_list.getAdapter().getItemCount());
                }
            });
            new Thread(() -> {
                if (messageDto.getSender() != null && !messageDto.getSender().getId().equals(user.getId())) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendReadMessageNotification();
                }
            }).start();
        }
    }

    @Override
    public void sendString(String s) {
//        MessageDto messageDto = gson.fromJson(s, MessageDto.class);
//        updateMessageRealTime(messageDto);
    }

    @SneakyThrows
    @Override
    // xử lý khi chọn hình ảnh để gửi
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            List<File> files = new ArrayList<>();
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
            Log.d("--file da chon", files.toString());
            checkInboxExistsAndSendFileMessage(files);
        } else {
            // chưa có hình ảnh nào được chọn
        }
//        if (requestCode == VIEW_ROOM_DETAIL && resultCode == Activity.RESULT_OK && data != null) {
//            Bundle bundle = data.getExtras();
//            if (bundle != null) {
//                InboxDto inboxDto = (InboxDto) bundle.getSerializable("dto");
//                this.inboxDto = inboxDto;
//                showImageAndDisplayName(inboxDto);
//            }
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    gửi tin nhắn file, hình ảnh, video
     */
    private void checkInboxExistsAndSendFileMessage(List<File> files) {
        Log.d("--", inboxDto.toString());
        if (inboxDto.getId() != null) {
            if (inboxDto.getRoom().getType().equals(RoomType.ONE)) {
                StringRequest request = new StringRequest(Request.Method.POST, Constant.API_INBOX + "/with/" + inboxDto.getRoom().getTo().getId(),
                        response -> {
                            try {
                                Log.d("--", "sad");
                                String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                inboxDto = gson.fromJson(res, InboxDto.class);
                                sendFileMessage(files);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.i("chat activ send mes err", error.toString())) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + access_token);
                        return map;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                request.setRetryPolicy(retryPolicy);
                requestQueue.add(request);
            } else if (inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
                sendFileMessage(files);
            }
        }
    }

    private void sendFileMessage(List<File> files) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("roomId", inboxDto.getRoom().getId());
        Log.d("--", "send mes");
        MultiPartFileRequest<String> restApiMultiPartRequest =
                new MultiPartFileRequest<String>(Request.Method.POST, Constant.API_FILE,
                        params, // danh sách request param
                        files,
                        response -> {
                            Log.d("--", "respone");
                            try {
                                Log.d("--", "try");
                                String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                Type listType = new TypeToken<List<String>>() {
                                }.getType();
                                List<String> urls = new Gson().fromJson(res, listType);
                                for (String url : urls) {
                                    sendMessage(url, FileUtil.getMessageType(url));
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            Log.i("upload error", error.toString());
                        }) {

                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + access_token);
                        return map;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        return params;
                    }
                };

        restApiMultiPartRequest.setRetryPolicy(new DefaultRetryPolicy(0, 1, 2));//10000
        Volley.newRequestQueue(this).add(restApiMultiPartRequest);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("dto", inboxDto);
        setResult(Activity.RESULT_OK, resultIntent);
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    /*
    unsubscribe websocket khi đóng activity
     */
    @SuppressLint("CheckResult")
    @Override
    public void finish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("dto", inboxDto);
        setResult(Activity.RESULT_OK, resultIntent);
        stompClient
                .send(new StompMessage(StompCommand.UNSUBSCRIBE,
                        Collections.singletonList(new StompHeader(StompHeader.ID, stompClient.getTopicId("/users/queue/read"))),
                        null))
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("unsubscribe read", "ok");
                    }
                });

        stompClient
                .send(new StompMessage(StompCommand.UNSUBSCRIBE,
                        Collections.singletonList(new StompHeader(StompHeader.ID, stompClient.getTopicId("/users/queue/messages"))),
                        null))
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("unsubscribe message", "ok");
                    }
                });
        stompClient
                .send(new StompMessage(StompCommand.UNSUBSCRIBE,
                        Collections.singletonList(new StompHeader(StompHeader.ID, stompClient.getTopicId("/users/queue/reaction"))),
                        null))
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("unsubscribe reaction", "ok");
                    }
                });
        super.finish();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void reply(MessageDto messageDto) {
        layout_reply_chat_activity.setVisibility(View.VISIBLE);
        txt_content_reply_chat_activity.setText(messageDto.getContent());
        txt_name_reply_chat_activity.setText(String.format("%s %s", getString(R.string.reply), messageDto.getSender().getDisplayName()));
        replyMessageId = messageDto.getId();
        edt_chat_message_send.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edt_chat_message_send, InputMethodManager.SHOW_IMPLICIT);
    }

    static class MyLinerLayoutManager extends LinearLayoutManager {

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public MyLinerLayoutManager(Context context) {
            super(context);
        }

        public MyLinerLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public MyLinerLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }
    }

    /*
    tạo menu trên thanh toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.room_detail_chat_activity);

        menuItem.setOnMenuItemClickListener(item -> {
            Log.d("", inboxDto.toString());
            Intent intent = new Intent(ChatActivity.this, RoomDetailActivity.class);
            intent.putExtra("dto", inboxDto);
            startActivityForResult(intent, VIEW_ROOM_DETAIL);
            overridePendingTransition(R.anim.enter, R.anim.exit);
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChoose();
            } else {
                Toast.makeText(this, getString(R.string.allow_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFileChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
    }

}