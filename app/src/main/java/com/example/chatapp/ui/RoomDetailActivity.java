package com.example.chatapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapterVertical;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dialog.ViewCommonGroupDialog;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MyMedia;
import com.example.chatapp.dto.MyMenuItem;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.enumvalue.RoomType;
import com.example.chatapp.ui.main.MainActivity;
import com.example.chatapp.utils.MultiPartFileRequest;
import com.example.chatapp.utils.PathUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

public class RoomDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int ADD_MEMBER = 2;
    private static final int VIEW_MEMBER = 3;
    private static final int REQUEST_PERMISSION = 123;
    private List<MyMenuItem> myMenuItems;
    private InboxDto inboxDto;
    private ImageButton imageOfRoom;
    private TextView nameOfRoom;
    private NestedScrollView scrollView;
    private String token;
    private ImageButton btn_change_image_of_room;
    private ImageButton btn_change_name_of_room;
    private UserSummaryDTO currentUser;
    private ListView lv_menu_items;

    private final BroadcastReceiver addMember = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                inboxDto.setRoom(newRoom);
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
                showImageAndNameOfRoom(context, inboxDto);
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
                showImageAndNameOfRoom(context, inboxDto);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        LocalBroadcastManager.getInstance(this).registerReceiver(addMember, new IntentFilter("room/members/add"));
        LocalBroadcastManager.getInstance(this).registerReceiver(deleteMember, new IntentFilter("room/members/delete"));
        LocalBroadcastManager.getInstance(this).registerReceiver(renameRoom, new IntentFilter("room/rename"));
        LocalBroadcastManager.getInstance(this).registerReceiver(changeImageRoom, new IntentFilter("room/changeImage"));

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Gson gson = new Gson();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.tlb_chat_room_detail);
        lv_menu_items = findViewById(R.id.lv_room_detail_menu);
        imageOfRoom = findViewById(R.id.image_of_room_detail);
        nameOfRoom = findViewById(R.id.name_of_room_detail);
        scrollView = findViewById(R.id.nested_scroll_room_detail);
        btn_change_image_of_room = findViewById(R.id.ibt_change_image_of_room);
        btn_change_name_of_room = findViewById(R.id.ibt_change_name_of_room);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setTitle(getString(R.string.options));
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            inboxDto = (InboxDto) bundle.getSerializable("dto");

        SharedPreferences sharedPreferencesToken = RoomDetailActivity.this.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        SharedPreferences sharedPreferencesUser = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        currentUser = gson.fromJson(userJson, UserSummaryDTO.class);

        myMenuItems = new ArrayList<>();
        setMenuItemList();

        // thay doi hinh anh cua nhom
        btn_change_image_of_room.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_needed_title))
                            .setMessage(getString(R.string.permission_needed_message))
                            .setPositiveButton(getString(R.string.confirm_button), (dialog, which) ->
                                    ActivityCompat.requestPermissions(RoomDetailActivity.this,
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

        btn_change_name_of_room.setOnClickListener(v -> showRenameDialog());

        MenuButtonAdapterVertical menuAdapter = new MenuButtonAdapterVertical(RoomDetailActivity.this, R.layout.line_item_menu_button_vertical, myMenuItems);
        lv_menu_items.setAdapter(menuAdapter);
        lv_menu_items.setOnItemClickListener((parent, view, position, itemId) -> {
            MyMenuItem item = myMenuItems.get(position);
            switch (item.getKey()) {
                case "viewMembers": {
                    Intent intent = new Intent(this, MemberActivity.class);
                    intent.putExtra("dto", inboxDto);
                    startActivityForResult(intent, VIEW_MEMBER);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    break;
                }
                case "addMember": {
                    Intent intent = new Intent(this, AddMemberActivity.class);
                    intent.putExtra("dto", inboxDto);
                    startActivityForResult(intent, ADD_MEMBER);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    break;
                }
                case "leaveRoom": {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.leave_group_confirm, inboxDto.getRoom().getName()))
                            .setPositiveButton(getString(R.string.cancel_button), (dialog, id) -> dialog.cancel())
                            .setNegativeButton(getString(R.string.confirm_button), (dialog, id) -> {
                                leaveGroup();
                            });
                    builder.create().show();
                    break;
                }
                case "deleteInbox": {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.delete_chat_history_confirm))
                            .setPositiveButton(getString(R.string.cancel_button), (dialog, id) -> dialog.cancel())
                            .setNegativeButton(getString(R.string.confirm_button), (dialog, id) -> {
                                deleteInbox();
                            });
                    builder.create().show();
                    break;
                }
                case "viewProfile": {
                    Intent intent = new Intent(this, ViewProfileActivity.class);
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("userId", inboxDto.getRoom().getTo().getId());
                    intent.putExtras(bundle2);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                }
                case "createRoomWithThisUser": {
                    Intent intent = new Intent(this, CreateGroupActivity.class);
                    Bundle bundle3 = new Bundle();
                    Log.d("------", inboxDto.getRoom().getTo().toString());
                    bundle3.putSerializable("user", inboxDto.getRoom().getTo());
                    intent.putExtras(bundle3);
                    startActivity(intent);
                    break;
                }
                case "viewCommonGroup":
                    ViewCommonGroupDialog viewCommonGroupDialog = new ViewCommonGroupDialog(this, inboxDto.getRoom().getTo());
                    viewCommonGroupDialog.show();
                    break;
                case "block":
                    AlertDialog.Builder blockBuilder = new AlertDialog.Builder(this);
                    blockBuilder.setMessage(getString(R.string.block_message_confirm, inboxDto.getRoom().getTo().getDisplayName()))
                            .setPositiveButton(getString(R.string.cancel_button), (dialog, id) -> dialog.cancel())
                            .setNegativeButton(getString(R.string.confirm_button), (dialog, id) -> {
                                block(inboxDto.getRoom().getTo().getId());
                            });
                    blockBuilder.create().show();
                    break;
                case "unBlock":
                    AlertDialog.Builder unBlockBuilder = new AlertDialog.Builder(this);
                    unBlockBuilder.setMessage(getString(R.string.un_block_message_confirm, inboxDto.getRoom().getTo().getDisplayName()))
                            .setPositiveButton(getString(R.string.cancel_button), (dialog, id) -> dialog.cancel())
                            .setNegativeButton(getString(R.string.confirm_button), (dialog, id) -> {
                                unBlock(inboxDto.getRoom().getTo().getId());
                            });
                    unBlockBuilder.create().show();
                    break;
            }
        });
        setListViewHeightBasedOnChildren(lv_menu_items);
        scrollView.post(() -> scrollView.scrollTo(0, 0));
    }

    private void setMenuItemList() {
        try {
            myMenuItems.clear();
        } catch (Exception e) {
            myMenuItems = new ArrayList<>();
        }
        myMenuItems.add(MyMenuItem.builder()
                .key("viewStoredMedia")
                .imageResource(R.drawable.ic_baseline_folder_open_24)
                .name(getString(R.string.stored_media))
                .build());

        final Context context = getApplication().getApplicationContext();
        showImageAndNameOfRoom(context, inboxDto);
        if (inboxDto != null && inboxDto.getRoom().getType().equals(RoomType.ONE)) {
            myMenuItems.add(MyMenuItem.builder()
                    .key("viewCommonGroup")
                    .imageResource(R.drawable.ic_baseline_groups_24)
                    .name(getString(R.string.view_groups_in_common))
                    .build());
            myMenuItems.add(MyMenuItem.builder()
                    .key("createRoomWithThisUser")
                    .imageResource(R.drawable.ic_baseline_group_create_24_black)
                    .name(getString(R.string.create_group_with_this_user))
                    .build());
            myMenuItems.add(MyMenuItem.builder()
                    .key("viewProfile")
                    .imageResource(R.drawable.ic_baseline_profile_circle_24)
                    .name(getString(R.string.view_profile))
                    .build());
            if (inboxDto.getRoom().getTo().isMeBLock()) {
                myMenuItems.add(MyMenuItem.builder()
                        .key("unBlock")
                        .imageResource(R.drawable.ic_baseline_block_24)
                        .name(getString(R.string.un_block_messages))
                        .build());
            } else {
                myMenuItems.add(MyMenuItem.builder()
                        .key("block")
                        .imageResource(R.drawable.ic_baseline_block_24)
                        .name(getString(R.string.block_messages))
                        .build());
            }
        } else if (inboxDto != null && inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
            TextView room_detail_create_at = findViewById(R.id.room_detail_create_at);
            room_detail_create_at.setVisibility(View.VISIBLE);
            room_detail_create_at.setText(String.format("%s: %s", getString(R.string.created), inboxDto.getRoom().getCreateAt()));
            myMenuItems.add(MyMenuItem.builder()
                    .key("viewMembers")
                    .imageResource(R.drawable.ic_baseline_groups_24)
                    .name(getString(R.string.view_members))
                    .build());
            myMenuItems.add(MyMenuItem.builder()
                    .key("addMember")
                    .imageResource(R.drawable.ic_baseline_group_create_24_black)
                    .name(getString(R.string.title_add_member))
                    .build());
            myMenuItems.add(MyMenuItem.builder()
                    .key("leaveRoom")
                    .imageResource(R.drawable.ic_baseline_leave_24)
                    .name(getString(R.string.leave_group))
                    .build());
            if (currentUser.getId().equals(inboxDto.getRoom().getCreateByUserId())) {
                myMenuItems.add(MyMenuItem.builder()
                        .key("deleteGroup")
                        .imageResource(R.drawable.ic_baseline_delete_forever_24)
                        .name(getString(R.string.delete_group))
                        .build());
            }
            btn_change_image_of_room.setVisibility(View.VISIBLE);
            btn_change_image_of_room.setPadding(3, 0, 3, 3);
            if (isValidContextForGlide(context)) {
                Glide.with(context)
                        .load(R.drawable.ic_baseline_camera_24)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(btn_change_image_of_room);
            }

            btn_change_name_of_room.setPadding(1, 1, 1, 1);
            if (isValidContextForGlide(context)) {
                Glide.with(context)
                        .load(R.drawable.ic_baseline_change_circle_24)
                        .centerCrop().circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(btn_change_name_of_room);
            }
        }

        myMenuItems.add(MyMenuItem.builder()
                .key("deleteInbox")
                .imageResource(R.drawable.ic_baseline_delete_forever_24)
                .name(getString(R.string.delete_chat_history))
                .build());

        myMenuItems.add(MyMenuItem.builder()
                .key("report")
                .imageResource(R.drawable.ic_baseline_report_24)
                .name(getString(R.string.report))
                .build());
        for (int i = 0; i < 10; i++) {
            myMenuItems.add(MyMenuItem.builder()
                    .key("----------------")
                    .name("----------------")
                    .build());
        }
        MenuButtonAdapterVertical menuAdapter = new MenuButtonAdapterVertical(RoomDetailActivity.this, R.layout.line_item_menu_button_vertical, myMenuItems);
        lv_menu_items.setAdapter(menuAdapter);
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

    private void showImageAndNameOfRoom(Context context, InboxDto inboxDto) {
        if (inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
            if (isValidContextForGlide(context)) {
                Glide.with(context)
                        .load(inboxDto.getRoom().getImageUrl())
                        .centerCrop().circleCrop()
                        .placeholder(R.drawable.img_avatar_placeholer)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageOfRoom);
            }
            imageOfRoom.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewImageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("activityTitle", inboxDto.getRoom().getName());
                bundle.putString("activitySubTitle", getString(R.string.avatar));
                bundle.putString("imageUrl", inboxDto.getRoom().getImageUrl());
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            nameOfRoom.setText(inboxDto.getRoom().getName());
        } else if (inboxDto.getRoom().getType().equals(RoomType.ONE)) {
            if (isValidContextForGlide(context)) {
                Glide.with(context)
                        .load(inboxDto.getRoom().getTo().getImageUrl())
                        .centerCrop().circleCrop()
                        .placeholder(R.drawable.img_avatar_placeholer)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageOfRoom);
            }
            imageOfRoom.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewImageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("activityTitle", inboxDto.getRoom().getTo().getDisplayName());
                bundle.putString("activitySubTitle", getString(R.string.avatar));
                bundle.putString("imageUrl", inboxDto.getRoom().getTo().getImageUrl());
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            nameOfRoom.setText(inboxDto.getRoom().getTo().getDisplayName());
        }
    }

    private void openFileChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
    }

    private void deleteInbox() {
        StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_INBOX + "/" + inboxDto.getId(),
                response -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    super.finish();
                },
                error -> Log.i("delete inbox error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(RoomDetailActivity.this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    /*
    sự kiện nhấn icon back trên toolbar
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showRenameDialog() {
        final Dialog dialog = new Dialog(RoomDetailActivity.this);
        dialog.setContentView(R.layout.layout_rename_room_dialog);

        TextView title = dialog.findViewById(R.id.txt_rename_dialog_title);
        EditText newName = dialog.findViewById(R.id.txt_rename_new_name);
        Button btn_cancel = dialog.findViewById(R.id.btn_rename_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_rename_ok);

        btn_ok.setEnabled(false);
        btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark)));

        newName.setText(inboxDto.getRoom().getName());

        newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void afterTextChanged(Editable s) {
                if (!newName.getText().toString().isEmpty()) {
                    if (!newName.getText().toString().equals(inboxDto.getRoom().getName())) {
                        btn_ok.setEnabled(true);
                        btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    } else {
                        btn_ok.setEnabled(false);
                        btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark)));
                    }
                } else {
                    btn_ok.setEnabled(false);
                    btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark)));
                }
            }
        });

        title.setText(getString(R.string.rename_group_title));

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setAttributes(layoutParams);
        dialog.show();

        btn_cancel.setOnClickListener(v1 -> dialog.cancel());
        btn_ok.setOnClickListener(v1 -> rename(inboxDto, dialog, newName.getText().toString().trim()));
    }

    private void leaveGroup() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_ROOM + "leave/" + inboxDto.getRoom().getId(),
                response -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    super.finish();
                },
                error -> Log.i("leave error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(RoomDetailActivity.this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    private void rename(InboxDto ibdto, Dialog dialog, String newName) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_ROOM + "rename/" + inboxDto.getRoom().getId(),
                response -> {
                    nameOfRoom.setText(newName);
                    RoomDTO room = inboxDto.getRoom();
                    room.setName(newName);
                    inboxDto.setRoom(room);
                    dialog.dismiss();
                },
                error -> Log.i("rename error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("name", newName);

                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(RoomDetailActivity.this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("dto", inboxDto);
        setResult(Activity.RESULT_OK, resultIntent);
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        super.finish();
    }

    @Override
    public void finish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("dto", inboxDto);
        setResult(Activity.RESULT_OK, resultIntent);
        super.finish();
    }

    // set dynamic height for list view
    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @SneakyThrows
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            List<File> files = new ArrayList<>();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    File file = new File(PathUtil.getPath(RoomDetailActivity.this, imageUri));
                    files.add(file);
                }
            } else {
                File file = new File(PathUtil.getPath(RoomDetailActivity.this, data.getData()));
                files.add(file);
            }
            uploadMultiFiles(files);
        } else {
            // chưa có hình ảnh nào được chọn
        }
        if ((requestCode == ADD_MEMBER || requestCode == VIEW_MEMBER)
                && (resultCode == Activity.RESULT_OK && data != null)) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                InboxDto inboxDto = (InboxDto) bundle.getSerializable("dto");
                this.inboxDto = inboxDto;
                Log.d("inboxdto", inboxDto.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadMultiFiles(List<File> files) {
        RoomDTO room = inboxDto.getRoom();
        MultiPartFileRequest<String> restApiMultiPartRequest =
                new MultiPartFileRequest<String>(Request.Method.POST, Constant.API_ROOM + "changeImage/" + inboxDto.getRoom().getId(),
                        new HashMap<>(), // danh sách request param
                        files,
                        response -> {
                            try {
                                String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                Type listType = new TypeToken<MyMedia>() {
                                }.getType();
                                MyMedia media = new Gson().fromJson(res, listType);
                                room.setImageUrl(media.getUrl());
                                inboxDto.setRoom(room);
                                Glide.with(RoomDetailActivity.this)
                                        .load(inboxDto.getRoom().getImageUrl())
                                        .centerCrop().circleCrop()
                                        .placeholder(R.drawable.img_avatar_placeholer)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(imageOfRoom);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            Log.i("upload error", "error");
                        }) {

                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + token);
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

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(addMember);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deleteMember);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(renameRoom);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(changeImageRoom);
        super.onDestroy();
    }

    private void block(String userIdToBlock) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_BLOCK + "/" + userIdToBlock,
                response -> {
                    RoomDTO room = inboxDto.getRoom();
                    UserProfileDto to = room.getTo();
                    to.setMeBLock(true);
                    room.setTo(to);
                    inboxDto.setRoom(room);
                    setMenuItemList();
                },
                error -> Log.i("block error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(RoomDetailActivity.this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    private void unBlock(String userIdToBlock) {
        StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_BLOCK + "/" + userIdToBlock,
                response -> {
                    RoomDTO room = inboxDto.getRoom();
                    UserProfileDto to = room.getTo();
                    to.setMeBLock(false);
                    room.setTo(to);
                    inboxDto.setRoom(room);
                    setMenuItemList();
                },
                error -> Log.i("block error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(RoomDetailActivity.this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

}