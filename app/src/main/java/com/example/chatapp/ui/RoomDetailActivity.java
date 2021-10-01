package com.example.chatapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MenuItem;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.enumvalue.RoomType;
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
    private List<MenuItem> menuItems;
    private MenuButtonAdapter menuAdapter;
    private ListView lv_menu_items;
    private ImageButton btnBack;
    private TextView title;
    private InboxDto inboxDto;
    private ImageButton imageOfRoom;
    private ImageButton btn_change_image_of_room;
    private ImageButton btn_change_name_of_room;
    private TextView nameOfRoom;
    private NestedScrollView scrollView;
    private Gson gson;
    private String token;

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
                .edgeSize(0.5f)
                .build();

        gson = new Gson();

        Slidr.attach(this, config);

        lv_menu_items = findViewById(R.id.lv_room_detail_menu);
        btnBack = findViewById(R.id.ibt_room_detail_back);
        title = findViewById(R.id.txt_room_detail_title);
        imageOfRoom = findViewById(R.id.image_of_room_detail);
        nameOfRoom = findViewById(R.id.name_of_room_detail);
        scrollView = findViewById(R.id.myscroll);
        btn_change_image_of_room = findViewById(R.id.ibt_change_image_of_room);
        btn_change_name_of_room = findViewById(R.id.ibt_change_name_of_room);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            inboxDto = (InboxDto) bundle.getSerializable("dto");

        SharedPreferences sharedPreferencesToken = RoomDetailActivity.this.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        menuItems = new ArrayList<>();
        menuItems.add(MenuItem.builder()
                .key("viewRepo")
                .imageResource(R.drawable.ic_reaction_love)
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
                    .imageResource(R.drawable.ic_reaction_love)
                    .name("Xem nhóm chung")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("block")
                    .imageResource(R.drawable.ic_reaction_love)
                    .name("Chặn tin nhắn")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("viewProfile")
                    .imageResource(R.drawable.ic_reaction_love)
                    .name("Xem trang cá nhân")
                    .build());
        }
        if (inboxDto != null && inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
            Glide.with(RoomDetailActivity.this)
                    .load(inboxDto.getRoom().getImageUrl())
                    .centerCrop().circleCrop()
                    .placeholder(R.drawable.image_placeholer)
                    .into(imageOfRoom);
            nameOfRoom.setText(inboxDto.getRoom().getName());
            menuItems.add(MenuItem.builder()
                    .key("viewMembers")
                    .imageResource(R.drawable.ic_reaction_haha)
                    .name("Xem thành viên")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("addMember")
                    .imageResource(R.drawable.ic_reaction_love)
                    .name("Thêm thành viên")
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("leaveRoom")
                    .imageResource(R.drawable.ic_reaction_love)
                    .name("Rời khỏi nhóm")
                    .build());
            btn_change_image_of_room.setPadding(3, 0, 3, 3);
            Glide.with(RoomDetailActivity.this)
                    .load(R.drawable.ic_camera)
                    .centerCrop().circleCrop()
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
                    .load(R.drawable.ic_edit)
                    .centerCrop().circleCrop()
                    .placeholder(R.drawable.image_placeholer)
                    .into(btn_change_name_of_room);

            btn_change_name_of_room.setOnClickListener(v -> showRenameDialog());
        }

        for (int i = 0; i < 3; i++) {
            menuItems.add(MenuItem.builder()
                    .key("delete")
                    .imageResource(R.drawable.ic_reaction_sad)
                    .name("Xóa cuộc trò chuyện")
                    .build());

            menuItems.add(MenuItem.builder()
                    .key("report")
                    .imageResource(R.drawable.ic_reaction_love)
                    .name("Báo cáo")
                    .build());
        }

        title.setText("Tùy chọn");
        btnBack.setOnClickListener(v -> onBackPressed());

        menuAdapter = new MenuButtonAdapter(RoomDetailActivity.this, R.layout.line_item_menu_button, menuItems);
        lv_menu_items.setAdapter(menuAdapter);
        lv_menu_items.setOnItemClickListener((parent, view, position, itemId) -> {
            MenuItem item = menuItems.get(position);
            if (item.getKey().equals("viewMembers")) {
                Intent intent = new Intent(RoomDetailActivity.this, MemberActivity.class);
                intent.putExtra("dto", inboxDto);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        setListViewHeightBasedOnChildren(lv_menu_items);
        scrollView.post(() -> scrollView.scrollTo(0, 0));
    }

    private void showRenameDialog() {
        final Dialog dialog = new Dialog(RoomDetailActivity.this);
        dialog.setContentView(R.layout.rename_room_dialog);

        TextView title = dialog.findViewById(R.id.txt_rename_dialog_title);
        EditText newName = dialog.findViewById(R.id.txt_rename_new_name);
        Button btn_cancel = dialog.findViewById(R.id.btn_rename_cancel);
        Button btn_ok = dialog.findViewById(R.id.btn_rename_ok);

        btn_ok.setEnabled(false);

        newName.setText(inboxDto.getRoom().getName());

        title.setText("Đổi tên");
        newName.setOnKeyListener((v1, keyCode, event) -> {
            btn_ok.setEnabled(!newName.getText().toString().isEmpty());
            btn_ok.setEnabled(!newName.getText().toString().equals(inboxDto.getRoom().getName()));
            return false;
        });

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_readby_dialog);
        dialog.show();

        btn_cancel.setOnClickListener(v1 -> dialog.dismiss());
        btn_ok.setOnClickListener(v1 -> rename(inboxDto, dialog, newName.getText().toString().trim()));
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadMultiFiles(List<File> files) {
        RoomDTO room = inboxDto.getRoom();
        MultiPartFileRequest<String> restApiMultiPartRequest =
                new MultiPartFileRequest<String>(Constant.API_ROOM + "changeImage/" + room.getId(),
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