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
import com.example.chatapp.adapter.LineItemPictureBeforeSendAdapter;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.cons.SendDataReplyMessage;
import com.example.chatapp.cons.SendingData;
import com.example.chatapp.cons.WebSocketClient;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MessageSendToServer;
import com.example.chatapp.dto.MyMedia;
import com.example.chatapp.dto.ReactionReceiver;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.ReadByReceiver;
import com.example.chatapp.dto.ReadBySend;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.enumvalue.MessageType;
import com.example.chatapp.enumvalue.OnlineStatus;
import com.example.chatapp.enumvalue.RoomType;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.vertx.core.json.Json;
import lombok.SneakyThrows;

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
    private LinearLayout layout_reply_chat_activity;
    private List<File> fileList;
    private LineItemPictureBeforeSendAdapter adapterBeforeSend;
    private Timer timer;
    public static final int DELAY = 1500;

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

    private final BroadcastReceiver renameRoom = new BroadcastReceiver() {
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

    private final BroadcastReceiver changeImageRoom = new BroadcastReceiver() {
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

    private final BroadcastReceiver newMessage = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                MessageDto newMessage = (MessageDto) bundle.getSerializable("dto");
                if (newMessage.getSender() != null)
                    adapter.deleteOldReadTracking(newMessage.getSender().getId());
                updateMessageRealTime(newMessage);
            }
        }
    };

    private final BroadcastReceiver readMessage = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ReadByReceiver readByReceiver = (ReadByReceiver) bundle.getSerializable("dto");
                adapter.updateReadToMessage(readByReceiver);
            }
        }
    };

    private final BroadcastReceiver reactionMessage = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ReactionReceiver readByReceiver = (ReactionReceiver) bundle.getSerializable("dto");
                adapter.updateReactionToMessage(readByReceiver);
            }
        }
    };

    private final BroadcastReceiver deleteMessage = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                MessageDto deletedMessage = (MessageDto) bundle.getSerializable("dto");
                adapter.updateDeletedMessage(deletedMessage);
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
        LocalBroadcastManager.getInstance(this).registerReceiver(addMember, new IntentFilter("room/members/add"));
        LocalBroadcastManager.getInstance(this).registerReceiver(deleteMember, new IntentFilter("room/members/delete"));
        LocalBroadcastManager.getInstance(this).registerReceiver(renameRoom, new IntentFilter("room/rename"));
        LocalBroadcastManager.getInstance(this).registerReceiver(changeImageRoom, new IntentFilter("room/changeImage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(newMessage, new IntentFilter("messages/new"));
        LocalBroadcastManager.getInstance(this).registerReceiver(readMessage, new IntentFilter("messages/read"));
        LocalBroadcastManager.getInstance(this).registerReceiver(reactionMessage, new IntentFilter("messages/reaction"));
        LocalBroadcastManager.getInstance(this).registerReceiver(deleteMessage, new IntentFilter("messages/delete"));

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        timer = new Timer();

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
            if (fileList != null && !fileList.isEmpty()) {
                ArrayList<File> copyFileList = new ArrayList<>(fileList);
                try {
                    fileList.clear();
                    adapterBeforeSend.notifyDataSetChanged();
                } catch (Exception e) {
                    fileList = new ArrayList<>();
                    adapterBeforeSend.setList(fileList);
                }
                checkInboxExistsBeforeSendMessage(message, copyFileList);
                edt_chat_message_send.getText().clear();
                edt_chat_message_send.setText("");
                layout_reply_chat_activity.setVisibility(View.GONE);
                edt_chat_message_send.postDelayed(() -> edt_chat_message_send.requestFocus(), 200);
            } else {
                if (message.isEmpty()) {
                    edt_chat_message_send.requestFocus();
//                    mở bàn phím
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(edt_chat_message_send, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    checkInboxExistsBeforeSendMessage(message, fileList);
                    edt_chat_message_send.getText().clear();
                    edt_chat_message_send.setText("");
                    layout_reply_chat_activity.setVisibility(View.GONE);
                    edt_chat_message_send.postDelayed(() -> edt_chat_message_send.requestFocus(), 200);
                }
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
            Glide.with(context).load(url).placeholder(R.drawable.img_avatar_placeholer)
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("CheckResult")
    private void checkInboxExistsBeforeSendMessage(String message, List<File> files) {
        if (inboxDto.getId() == null) {
            checkInboxExistsAndSendMessage(message, files);
        } else {
            uploadAndSend(message, files);
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

    private void sendMessage(String message, List<MyMedia> media) {
        MessageSendToServer messageSendToServer = new MessageSendToServer();
        if (message.trim().isEmpty())
            messageSendToServer.setContent(null);
        else
            messageSendToServer.setContent(message);
        messageSendToServer.setRoomId(inboxDto.getRoom().getId());
        messageSendToServer.setReplyId(replyMessageId);
        if (media == null || media.isEmpty())
            messageSendToServer.setType(MessageType.TEXT);
        else {
            messageSendToServer.setMedia(media);
            messageSendToServer.setType(MessageType.MEDIA);
        }
        Log.e("send : ", Json.encode(messageSendToServer));

        WebSocketClient.getInstance().getStompClient()
                .send("/app/chat", Json.encode(messageSendToServer))
                .subscribe(() -> {

                });
        replyMessageId = null;
    }

    /*
    hai người chưa có room chung nên phải tạo room và inbox trước khi gửi tin nhắn đầu tiên
     */
    /*@RequiresApi(api = Build.VERSION_CODES.N)
    private void createInboxIfNotExistsAndSendMessage(String toUserId, String message, List<File> files) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_INBOX + "/with/" + toUserId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        inboxDto = gson.fromJson(res, InboxDto.class);
                        uploadAndSend(message, files);
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
    }*/

    @SuppressLint("CheckResult")
    private void sendReadMessageNotification() {
        Log.d("--------", "send read mess");
        MessageDto lastMessage = adapter.getLastMessage();
        System.out.println("lastMessage = " + lastMessage);
        boolean find = false;
        if (lastMessage != null) {
            Set<ReadByDto> readTracking = lastMessage.getReadbyes();
            for (ReadByDto read : readTracking) {
                if (read.getReadByUser() != null &&
                        user.getId().equals(read.getReadByUser().getId())) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                ReadBySend readBySend = ReadBySend.builder()
                        .messageId(lastMessage.getId())
                        .readAt(new Date())
                        .roomId(lastMessage.getRoomId())
                        .userId(user.getId())
                        .build();

                WebSocketClient.getInstance().getStompClient()
                        .send("/app/read", Json.encode(readBySend))
                        .subscribe(() -> {

                        });
            }
        }
    }

    private void loadMoreData() {
        updateList();
    }

    private void updateList() {
        Log.e("url : ", Constant.API_CHAT + inboxDto.getId());
        if (inboxDto != null && inboxDto.getId() != null) {
            StringRequest request = new StringRequest(Request.Method.GET, Constant.API_CHAT + inboxDto.getId() + "?size=" + size + "&page=" + page,
                    response -> {
                        try {
                            page++;
                            String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                            JSONObject object = new JSONObject(res);
                            JSONArray array = (JSONArray) object.get("content");

                            Type listType = new TypeToken<List<MessageDto>>() {
                            }.getType();
                            List<MessageDto> list = gson.fromJson(array.toString(), listType);
                            if (!list.isEmpty()) {
                                adapter.updateList(list);
                                if (isFirstTimeRun) {
                                    timer.cancel();
                                    timer = new Timer();
                                    timer.schedule(
                                            new TimerTask() {
                                                @Override
                                                public void run() {
                                                    sendReadMessageNotification();
                                                }
                                            },
                                            DELAY
                                    );
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
            timer.cancel();
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            sendReadMessageNotification();
                        }
                    },
                    DELAY
            );
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
            fileList = files;
            RecyclerView recyclerView = findViewById(R.id.rcv_picture_before_send);
            adapterBeforeSend = new LineItemPictureBeforeSendAdapter(this, fileList);
            LinearLayoutManager beforeSendLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
            recyclerView.setAdapter(adapterBeforeSend);
            recyclerView.setLayoutManager(beforeSendLayoutManager);
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkInboxExistsAndSendMessage(String message, List<File> files) {
        Log.d("--", inboxDto.toString());
        if (inboxDto.getId() == null) {
            if (inboxDto.getRoom().getType().equals(RoomType.ONE)) {
                StringRequest request = new StringRequest(Request.Method.POST, Constant.API_INBOX + "/with/" + inboxDto.getRoom().getTo().getId(),
                        response -> {
                            try {
                                Log.d("--", "sad");
                                String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                inboxDto = gson.fromJson(res, InboxDto.class);
                                uploadAndSend(message, files);
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
                uploadAndSend(message, files);
            }
        } else {
            uploadAndSend(message, files);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void uploadAndSend(String message, List<File> files) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("roomId", inboxDto.getRoom().getId());
        Log.d("--", "send mes");
        if (files != null && !files.isEmpty()) {
            MultiPartFileRequest<String> restApiMultiPartRequest =
                    new MultiPartFileRequest<String>(Request.Method.POST, Constant.API_FILE,
                            params, // danh sách request param
                            files,
                            response -> {
                                Log.d("--", "respone");
                                try {
                                    Log.d("--", "try");
                                    String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                    Type listType = new TypeToken<List<MyMedia>>() {
                                    }.getType();
                                    List<MyMedia> media = new Gson().fromJson(res, listType);
                                    sendMessage(message, media);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> {
                                try {
                                    fileList.clear();
                                    adapterBeforeSend.notifyDataSetChanged();
                                } catch (Exception e) {
                                    fileList = new ArrayList<>();
                                    adapterBeforeSend.setList(fileList);
                                }
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
        } else {
            sendMessage(message, null);
        }

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
        super.finish();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(addMember);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deleteMember);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(renameRoom);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(changeImageRoom);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newMessage);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(readMessage);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reactionMessage);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deleteMessage);
        super.onDestroy();
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