package com.example.chatapp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.FriendDeleteDto;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.dto.ViewProfileDto;
import com.example.chatapp.entity.FriendRequest;
import com.example.chatapp.enumvalue.FriendStatus;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView img_view_profile_activity;
    private MaterialButton btn_friend_status_view_profile_activity;
    private String userId;
    private Gson gson;
    private String token;
    private UserSummaryDTO currentUser;
    private TextView view_profile_txt_message;
    private ViewProfileDto viewProfileDto;
    private Menu menuOpts;

    private final BroadcastReceiver friendRequestReceived = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (currentUser.getId().equals(dto.getTo().getId())) {
                    setEventOnClickButton(FriendStatus.RECEIVED, dto.getFrom().getId());
                } else if (currentUser.getId().equals(dto.getFrom().getId())) {
                    setEventOnClickButton(FriendStatus.SENT, dto.getTo().getId());
                }
            }
        }
    };

    private final BroadcastReceiver friendRequestAccept = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (currentUser.getId().equals(dto.getFrom().getId())) {
                    setEventOnClickButton(FriendStatus.FRIEND, dto.getTo().getId());
                } else if (currentUser.getId().equals(dto.getTo().getId())) {
                    setEventOnClickButton(FriendStatus.FRIEND, dto.getFrom().getId());
                }
            }
        }
    };

    private final BroadcastReceiver friendRequestRecall = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (currentUser.getId().equals(dto.getFrom().getId())) {
                    setEventOnClickButton(FriendStatus.NONE, dto.getTo().getId());
                } else if (currentUser.getId().equals(dto.getTo().getId())) {
                    setEventOnClickButton(FriendStatus.NONE, dto.getFrom().getId());
                }
            }
        }
    };

    private final BroadcastReceiver friendRequestDelete = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (currentUser.getId().equals(dto.getFrom().getId())) {
                    setEventOnClickButton(FriendStatus.NONE, dto.getTo().getId());
                } else if (currentUser.getId().equals(dto.getTo().getId())) {
                    setEventOnClickButton(FriendStatus.NONE, dto.getFrom().getId());
                }
            }
        }
    };

    private final BroadcastReceiver friendDelete = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendDeleteDto dto = (FriendDeleteDto) bundle.getSerializable("dto");
                if (currentUser.getId().equals(dto.getUserId()) && userId.equals(dto.getFriendId())) {
                    setEventOnClickButton(FriendStatus.NONE, dto.getFriendId());
                } else if (currentUser.getId().equals(dto.getFriendId()) && userId.equals(dto.getUserId())) {
                    setEventOnClickButton(FriendStatus.NONE, dto.getUserId());
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestReceived, new IntentFilter("friendRequest/received"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestAccept, new IntentFilter("friendRequest/accept"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestRecall, new IntentFilter("friendRequest/recall"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestDelete, new IntentFilter("friendRequest/delete"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendDelete, new IntentFilter("friends/delete"));

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        RelativeLayout view_profile_top_layout = findViewById(R.id.view_profile_top_layout);
        Glide.with(this).load(R.drawable.bg_infor)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        view_profile_top_layout.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        img_view_profile_activity = findViewById(R.id.img_view_profile_activity);
        view_profile_txt_message = findViewById(R.id.view_profile_txt_message);
        Toolbar toolbar_view_profile_activity = findViewById(R.id.toolbar_view_profile_activity);
        btn_friend_status_view_profile_activity = findViewById(R.id.btn_friend_status_view_profile_activity);

        toolbar_view_profile_activity.setTitleTextColor(Color.WHITE);
        toolbar_view_profile_activity.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar_view_profile_activity);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            userId = bundle.getString("userId");

        MaterialButton btn_chat_view_profile_activity = findViewById(R.id.btn_chat_view_profile_activity);

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", Context.MODE_PRIVATE);
        currentUser = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        if (userId != null) {
            getProfile(userId);
            btn_chat_view_profile_activity.setOnClickListener(v -> {
                getInboxWith(userId);
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        super.finish();
    }

    /*
    tạo menu trên thanh toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_profile_activity, menu);

        final Activity activity = this;
        menu.findItem(R.id.menu_view_profile).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                View view = activity.findViewById(item.getItemId());

                final PopupMenu popupMenu = new PopupMenu(activity.getApplicationContext(), view);
                menuOpts = popupMenu.getMenu();
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_view_profile_fragment, menuOpts);

                menuOpts.getItem(0).setTitle(getString(R.string.view_information));
                menuOpts.getItem(1).setTitle(getString(R.string.remove_friend));
                menuOpts.getItem(2).setTitle(getString(R.string.report_user));
                if (viewProfileDto == null) {
                    menuOpts.getItem(1).setVisible(false);
                    menuOpts.getItem(1).setEnabled(false);
                } else {
                    if (!viewProfileDto.getFriendStatus().equals(FriendStatus.FRIEND)) {
                        menuOpts.getItem(1).setVisible(false);
                        menuOpts.getItem(1).setEnabled(false);
                    }
                }

                /*
                hiển thị icon trên popup menu
                 */
                try {
                    Field[] fields = popupMenu.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popupMenu);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                popupMenu.setOnMenuItemClickListener(item1 -> {
                    if (item1.getItemId() == R.id.view_profile_item_view_information) {
                        Intent intent = new Intent(activity, ViewInformationOfOtherUserActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("otherUserId", userId);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else if (item1.getItemId() == R.id.view_profile_item_remove_friend) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(getString(R.string.remove_friend_with_user, viewProfileDto.getDisplayName()))
                                .setPositiveButton(getString(R.string.cancel_button), (dialog, id) -> dialog.cancel())
                                .setNegativeButton(getString(R.string.confirm_button), (dialog, id) -> {
                                    deleteFriend(userId);
                                });
                        builder.create().show();
                    } else if (item1.getItemId() == R.id.view_profile_item_report_user) {
                        Intent intent = new Intent(ViewProfileActivity.this, ReportActivity.class);
                        Bundle bundle = new Bundle();
                        UserProfileDto profile = new UserProfileDto();
                        profile.setMeBLock(viewProfileDto.isMeBLock());
                        profile.setFriendStatus(viewProfileDto.getFriendStatus());
                        profile.setDisplayName(viewProfileDto.getDisplayName());
                        profile.setId(viewProfileDto.getId());
                        profile.setImageUrl(viewProfileDto.getImageUrl());
                        profile.setBlockMe(viewProfileDto.isBlockMe());
                        profile.setLastOnline(viewProfileDto.getLastOnline());
                        profile.setOnlineStatus(viewProfileDto.getOnlineStatus());
                        profile.setPhoneNumber(viewProfileDto.getPhoneNumber());
                        bundle.putSerializable("user", profile);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    return true;
                });
                popupMenu.show();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void getInboxWith(String anotherUserId) {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "/with/" + anotherUserId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        InboxDto dto = gson.fromJson(res, InboxDto.class);

                        Intent intent = new Intent(this, ChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", dto);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getProfile(String userId) {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_USER + "viewProfile/" + userId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        viewProfileDto = gson.fromJson(res, ViewProfileDto.class);
                        setEventOnClickButton(viewProfileDto.getFriendStatus(), viewProfileDto.getId());
                        setTitle(viewProfileDto.getDisplayName());
                        Glide.with(this).load(viewProfileDto.getImageUrl())
                                .placeholder(R.drawable.img_avatar_placeholer)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .centerCrop().circleCrop()
                                .into(img_view_profile_activity);
                        img_view_profile_activity.setOnClickListener(v -> {
                            Intent intent = new Intent(this, ViewImageActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("activityTitle", viewProfileDto.getDisplayName());
                            bundle.putString("activitySubTitle", getString(R.string.avatar));
                            bundle.putString("imageUrl", viewProfileDto.getImageUrl());
                            intent.putExtras(bundle);
                            startActivity(intent);
                        });

                    } catch (NumberFormatException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setEventOnClickButton(FriendStatus status, String userId) {
        if (menuOpts != null) {
            MenuItem item = menuOpts.getItem(1);
            if (item != null) {
                item.setVisible(false);
                item.setEnabled(false);
            }
        }
        if (status.equals(FriendStatus.SENT)) {

            viewProfileDto.setFriendStatus(FriendStatus.SENT);
            btn_friend_status_view_profile_activity.setText(getString(R.string.recall_button));
            btn_friend_status_view_profile_activity.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btn_friend_status_view_profile_activity.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_recall_24));
            view_profile_txt_message.setText(getString(R.string.friend_request_sent_to_user, viewProfileDto.getDisplayName()));
            btn_friend_status_view_profile_activity.setOnClickListener(v -> {
                deleteSentRequest(userId);
            });
        } else if (status.equals(FriendStatus.RECEIVED)) {

            viewProfileDto.setFriendStatus(FriendStatus.RECEIVED);
            btn_friend_status_view_profile_activity.setText(getString(R.string.accept));
            btn_friend_status_view_profile_activity.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btn_friend_status_view_profile_activity.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_round_accept_24));
            view_profile_txt_message.setText(getString(R.string.friend_request_received_from_user, viewProfileDto.getDisplayName()));
            btn_friend_status_view_profile_activity.setOnClickListener(v -> {
                acceptFriendRequest(userId);
            });
        } else if (status.equals(FriendStatus.FRIEND)) {

            viewProfileDto.setFriendStatus(FriendStatus.FRIEND);
            if (menuOpts != null) {
                MenuItem item = menuOpts.getItem(1);
                if (item != null) {
                    item.setVisible(true);
                    item.setEnabled(true);
                }
            }

            btn_friend_status_view_profile_activity.setText(getString(R.string.friend));
            btn_friend_status_view_profile_activity.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btn_friend_status_view_profile_activity.setIcon(null);
            view_profile_txt_message.setText(getString(R.string.relation_is_friend));
            btn_friend_status_view_profile_activity.setOnClickListener(null);
        } else if (status.equals(FriendStatus.NONE)) {

            viewProfileDto.setFriendStatus(FriendStatus.NONE);
            btn_friend_status_view_profile_activity.setText(getString(R.string.add_friend));
            btn_friend_status_view_profile_activity.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btn_friend_status_view_profile_activity.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_person_add_alt_24));
            view_profile_txt_message.setText(getString(R.string.relation_is_none, viewProfileDto.getDisplayName()));
            btn_friend_status_view_profile_activity.setOnClickListener(v -> {
                sendFriendRequest(userId);
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void deleteSentRequest(String userId) {
        if (userId != null) {
            StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_FRIEND_REQUEST + "/" + userId,
                    response -> {
                        setEventOnClickButton(FriendStatus.NONE, userId);
                    }, error -> {
                Log.e("error: ", error.toString());

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(this);
            DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(retryPolicy);
            queue.add(request);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void acceptFriendRequest(String userId) {
        if (userId != null) {
            StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_FRIEND_REQUEST + "/" + userId,
                    response -> {
                        setEventOnClickButton(FriendStatus.FRIEND, userId);
                    }, error -> {
                Log.e("error: ", error.toString());

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(this);
            DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(retryPolicy);
            queue.add(request);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendFriendRequest(String userId) {
        if (userId != null) {
            StringRequest request = new StringRequest(Request.Method.POST, Constant.API_FRIEND_REQUEST + "/" + userId,
                    response -> {
                        setEventOnClickButton(FriendStatus.SENT, userId);
                    },
                    error -> {

                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(retryPolicy);
            requestQueue.add(request);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void deleteFriend(String userId) {
        if (userId != null) {
            StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_FRIEND_LIST + "/" + userId,
                    response -> {
//                        setEventOnClickButton(FriendStatus.NONE, userId);
                    }, error -> {
                Log.e("error: ", error.toString());

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(this);
            DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(retryPolicy);
            queue.add(request);
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestReceived);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestAccept);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestRecall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestDelete);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendDelete);
        super.onDestroy();
    }

}
