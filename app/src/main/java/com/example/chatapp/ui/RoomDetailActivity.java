package com.example.chatapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapterVertical;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MenuItem;
import com.example.chatapp.dto.RoomDTO;
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
    private List<MenuItem> menuItems;
    private MenuButtonAdapterVertical menuAdapter;
    private ListView lv_menu_items;
    private InboxDto inboxDto;
    private ImageButton imageOfRoom;
    private ImageButton btn_change_image_of_room;
    private ImageButton btn_change_name_of_room;
    private TextView nameOfRoom;
    private TextView room_detail_create_at;
    private NestedScrollView scrollView;
    private Gson gson;
    private String token;
    private Toolbar toolbar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(1f)
                .build();

        gson = new Gson();

        Slidr.attach(this, config);

        toolbar = findViewById(R.id.tlb_chat_room_detail);
        lv_menu_items = findViewById(R.id.lv_room_detail_menu);
        imageOfRoom = findViewById(R.id.image_of_room_detail);
        nameOfRoom = findViewById(R.id.name_of_room_detail);
        room_detail_create_at = findViewById(R.id.room_detail_create_at);
        scrollView = findViewById(R.id.nested_scroll_room_detail);
        btn_change_image_of_room = findViewById(R.id.ibt_change_image_of_room);
        btn_change_name_of_room = findViewById(R.id.ibt_change_name_of_room);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setTitle("Tùy chọn");
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

        menuItems = new ArrayList<>();
        menuItems.add(MenuItem.builder()
                .key("viewRepo")
                .imageResource(R.drawable.ic_baseline_folder_open_24)
                .name("Kho lưu trữ")
                .build());

        if (inboxDto != null && inboxDto.getRoom().getType().equals(RoomType.ONE)) {
            Glide.with(RoomDetailActivity.this)
                    .load(inboxDto.getRoom().getTo().getImageUrl())
                    .centerCrop().circleCrop()
                    .placeholder(R.drawable.image_placeholer)
                    .into(imageOfRoom);
            nameOfRoom.setText(inboxDto.getRoom().getTo().getDisplayName());

            menuItems.add(MenuItem.builder()
                    .key("viewCommonGroup")
                    .imageResource(R.drawable.ic_baseline_groups_24)
                    .name("Xem nhóm chung")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("createRoomWithThisUser")
                    .imageResource(R.drawable.ic_baseline_group_create_24_black)
                    .name("Tạo nhóm với người này --đã xong")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("viewProfile")
                    .imageResource(R.drawable.ic_baseline_profile_circle_24)
                    .name("Xem trang cá nhân -- đã xong")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("block")
                    .imageResource(R.drawable.ic_baseline_block_24)
                    .name("Chặn tin nhắn")
                    .build());
        } else if (inboxDto != null && inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
            Glide.with(RoomDetailActivity.this)
                    .load(inboxDto.getRoom().getImageUrl())
                    .centerCrop().circleCrop()
                    .placeholder(R.drawable.image_placeholer)
                    .into(imageOfRoom);
            nameOfRoom.setText(inboxDto.getRoom().getName());
            room_detail_create_at.setText("Đã tạo: " + inboxDto.getRoom().getCreateAt());
            menuItems.add(MenuItem.builder()
                    .key("viewMembers")
                    .imageResource(R.drawable.ic_baseline_groups_24)
                    .name("Xem thành viên -- đã xong")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("addMember")
                    .imageResource(R.drawable.ic_baseline_group_create_24_black)
                    .name("Thêm thành viên --  đã xong")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("leaveRoom")
                    .imageResource(R.drawable.ic_baseline_leave_24)
                    .name("Rời khỏi nhóm -- đã xong")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("deleteGroup")
                    .imageResource(R.drawable.ic_baseline_delete_forever_24)
                    .name("Giải tán nhóm")
                    .build());
            btn_change_image_of_room.setPadding(3, 0, 3, 3);
            Glide.with(RoomDetailActivity.this)
                    .load(R.drawable.ic_baseline_camera_24)
                    .centerCrop()
                    .placeholder(R.drawable.image_placeholer)
                    .into(btn_change_image_of_room);

            // thay doi hinh anh cua nhom
            btn_change_image_of_room.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            });

            btn_change_name_of_room.setPadding(1, 1, 1, 1);
            Glide.with(RoomDetailActivity.this)
                    .load(R.drawable.ic_baseline_change_circle_24)
                    .centerCrop().circleCrop()
                    .placeholder(R.drawable.image_placeholer)
                    .into(btn_change_name_of_room);

            btn_change_name_of_room.setOnClickListener(v -> showRenameDialog());
        }

        menuItems.add(MenuItem.builder()
                .key("deleteInbox")
                .imageResource(R.drawable.ic_baseline_delete_forever_24)
                .name("Xóa cuộc trò chuyện --đã xong")
                .build());

        menuItems.add(MenuItem.builder()
                .key("report")
                .imageResource(R.drawable.ic_baseline_report_24)
                .name("Báo cáo")
                .build());
        for (int i = 0; i < 10; i++) {
            menuItems.add(MenuItem.builder()
                    .key("----------------")
                    .name("----------------")
                    .build());
        }

        menuAdapter = new MenuButtonAdapterVertical(RoomDetailActivity.this, R.layout.line_item_menu_button_vertical, menuItems);
        lv_menu_items.setAdapter(menuAdapter);
        lv_menu_items.setOnItemClickListener((parent, view, position, itemId) -> {
            MenuItem item = menuItems.get(position);
            if (item.getKey().equals("viewMembers")) {
                Intent intent = new Intent(this, MemberActivity.class);
                intent.putExtra("dto", inboxDto);
                startActivityForResult(intent, VIEW_MEMBER);
                overridePendingTransition(R.anim.enter, R.anim.exit);

            } else if (item.getKey().equals("addMember")) {
                Intent intent = new Intent(this, AddMemberActivity.class);
                intent.putExtra("dto", inboxDto);
                startActivityForResult(intent, ADD_MEMBER);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            } else if (item.getKey().equals("leaveRoom")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Rời khỏi nhóm " + inboxDto.getRoom().getName())
                        .setPositiveButton("Hủy", (dialog, id) -> dialog.cancel())
                        .setNegativeButton("Rời khỏi nhóm", (dialog, id) -> {
                            leaveGroup();
                        });
                builder.create().show();
            } else if (item.getKey().equals("deleteInbox")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Xóa lịch sử cuộc trò chuyện này?")
                        .setPositiveButton("Hủy", (dialog, id) -> dialog.cancel())
                        .setNegativeButton("Xóa", (dialog, id) -> {
                            deleteInbox();
                        });
                builder.create().show();
            } else if (item.getKey().equals("viewProfile")) {
                Intent intent = new Intent(this, ViewProfileActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("userId", inboxDto.getRoom().getTo().getId());
                intent.putExtras(bundle2);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (item.getKey().equals("createRoomWithThisUser")) {
                Intent intent = new Intent(this, CreateGroupActivity.class);
                Bundle bundle3 = new Bundle();
                Log.d("------", inboxDto.getRoom().getTo().toString());
                bundle3.putSerializable("user", inboxDto.getRoom().getTo());
                intent.putExtras(bundle3);
                startActivity(intent);
            }
        });
        setListViewHeightBasedOnChildren(lv_menu_items);
        scrollView.post(() -> scrollView.scrollTo(0, 0));
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
                    btn_ok.setEnabled(true);
                    btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                } else {
                    btn_ok.setEnabled(false);
                    btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark)));
                }
                if (!newName.getText().toString().equals(inboxDto.getRoom().getName())) {
                    btn_ok.setEnabled(true);
                    btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                } else {
                    btn_ok.setEnabled(false);
                    btn_ok.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark)));
                }
            }
        });

        title.setText("Đổi tên");

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
                                Type listType = new TypeToken<List<String>>() {
                                }.getType();
                                List<String> urls = new Gson().fromJson(res, listType);
                                for (String url : urls) {
                                    Log.d("", url);
                                }
                                if (!urls.isEmpty()) {
                                    room.setImageUrl(urls.get(0));
                                    inboxDto.setRoom(room);
                                    Glide.with(RoomDetailActivity.this)
                                            .load(inboxDto.getRoom().getImageUrl())
                                            .centerCrop().circleCrop()
                                            .placeholder(R.drawable.image_placeholer)
                                            .into(imageOfRoom);
                                }
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

}