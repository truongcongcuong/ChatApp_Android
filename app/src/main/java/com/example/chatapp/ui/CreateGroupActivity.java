package com.example.chatapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserCreateGroupAdapter;
import com.example.chatapp.adapter.SelectedUserCreateGroupAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.SendDataCreateRoomActivity;
import com.example.chatapp.dto.FriendDTO;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MemberCreateDto;
import com.example.chatapp.dto.RoomCreateDto;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.enumvalue.RoomType;
import com.example.chatapp.utils.MultiPartFileRequest;
import com.example.chatapp.utils.PathUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateGroupActivity extends AppCompatActivity implements SendDataCreateRoomActivity {
    private static final int PICK_IMAGE = 1;
    private EditText txt_create_group_name;
    private SearchView txt_create_room_find_user;
    private ImageView image_create_group;
    private ImageView image_create_group_delete;
    private RecyclerView lv_create_group_user;
    private SimpleDateFormat sdfYMD;
    /*
    adapter tim kiếm user trong danh sách bạn bè
     */
    private SearchUserCreateGroupAdapter adapter;
    private Gson gson;
    private UserSummaryDTO user;
    private String token;
    /*
    danh sách những người đã được chọn để tạo nhóm
     */
    private List<UserProfileDto> usersSelected;
    /*
    danh sách bạn bè
     */
    private List<UserProfileDto> userFriends;

    /*
    adpater những người đã được chọn để tạo nhóm
     */
    private SelectedUserCreateGroupAdapter selectedUserAdapter;
    private RecyclerView rcv_create_group_selected;
    private Button create_group_continue;
    private RelativeLayout bottomLayout;
    private Toolbar toolbar;
    /*
    dùng để set màu trở lại như cũ cho button tạo nhóm khi enable hoặc disable
     */
    private ColorStateList backgroundTintList;
    /*
    file hình ảnh đại diện cho nhóm
     */
    private File imageGroupFile;
    private ProgressDialog progress;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        toolbar = findViewById(R.id.tlb_create_group_activity);
        toolbar.setTitle(R.string.unname_group);
        toolbar.setSubtitle(R.string.no_member);
        setSupportActionBar(toolbar);

        sdfYMD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        image_create_group = findViewById(R.id.image_create_group_avt);
        image_create_group_delete = findViewById(R.id.create_group_selected_delete_image);
        txt_create_group_name = findViewById(R.id.txt_create_group_name);
        txt_create_room_find_user = findViewById(R.id.txt_create_group_find_user);
        lv_create_group_user = findViewById(R.id.rcv_create_group_user);

        rcv_create_group_selected = findViewById(R.id.rcv_create_group_selected);
        create_group_continue = findViewById(R.id.create_group_continute);
        bottomLayout = findViewById(R.id.create_group_bottom_layout);

        create_group_continue.setEnabled(false);
        backgroundTintList = create_group_continue.getBackgroundTintList();

        txt_create_room_find_user.setQueryHint(getResources().getString(R.string.find_by_name_or_phone));
        txt_create_room_find_user.setIconifiedByDefault(false);
        txt_create_room_find_user.setFocusable(true);

//        txt_create_group_name.requestFocus();
        image_create_group_delete.setVisibility(View.GONE);

        /*
        tìm icon close, icon search, và edit text nàm trên searchview
         */
        int searchIcon = txt_create_room_find_user.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        int closeIconId = txt_create_room_find_user.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        int editTextId = txt_create_room_find_user.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);

        /*
        xóa icon seach trên searchview
         */
        ImageView magImage = txt_create_room_find_user.findViewById(searchIcon);
        magImage.setVisibility(View.GONE);
        magImage.setImageDrawable(null);

        /*
        set padding cho edit text
         */
        EditText editText = txt_create_room_find_user.findViewById(editTextId);
        editText.setPadding(50, 0, 50, 0);

        /*
        set sự kiện khi click icon close trên searchview
         */
        View closeIcon = txt_create_room_find_user.findViewById(closeIconId);
        closeIcon.setOnClickListener(v -> {
            editText.setText("");
            adapter.notifyDataSetChanged();
        });

        /*
        view nằm bên ngoài edit text của search view, set chiều cao của nó wrap vừa với edit text
         */
        int searchPlateId = txt_create_room_find_user.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = txt_create_room_find_user.findViewById(searchPlateId);
        ViewGroup.LayoutParams params = searchPlate.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        searchPlate.setLayoutParams(params);
        searchPlate.setPadding(0, 5, 0, 5);
        searchPlate.setBackgroundResource(R.drawable.search_view_background);

        initImageCreate();

        lv_create_group_user.setLayoutManager(new LinearLayoutManager(CreateGroupActivity.this));
        this.adapter = new SearchUserCreateGroupAdapter(CreateGroupActivity.this, new ArrayList<>());
        lv_create_group_user.setAdapter(adapter);

        txt_create_room_find_user.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /*
            sự kiện bấm nút submit trên bàn phím khi tìm kiếm
             */
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return false;
            }

            /*
            sự kiện phím khi tìm kiếm
             */
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String newText) {
                /*
                nếu text không rỗng thì tìm, ngược lại xóa recyclerview
                 */
                if (!newText.isEmpty()) {
//                    search(newText);
                    List<UserProfileDto> find = userFriends.stream()
                            .filter(x -> x.getDisplayName().toLowerCase()
                                    .contains(txt_create_room_find_user.getQuery().toString().trim().toLowerCase()))
                            .collect(Collectors.toList());
                    adapter.setList(find);
                } else {
                    adapter.setList(userFriends);
                }
                return false;
            }
        });

        txt_create_group_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    toolbar.setTitle(s.toString().trim());
                    if (!usersSelected.isEmpty()) {
                    /*
                    khi tên khác rỗng và đã có danh sách thành viên thì enable cho button tạo nhóm
                     */
                        create_group_continue.setBackgroundTintList(null);
                        create_group_continue.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*
                khi tên rỗng thì disable button tạo nhóm
                 */
                if (txt_create_group_name.getText().toString().trim().isEmpty()) {
                    toolbar.setTitle(R.string.unname_group);
                    create_group_continue.setBackgroundTintList(backgroundTintList);
                    create_group_continue.setEnabled(false);
                }
            }
        });

        SharedPreferences sharedPreferencesUser = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);

        SharedPreferences sharedPreferencesToken = getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        gson = new Gson();
        user = gson.fromJson(userJson, UserSummaryDTO.class);

        /*
        layput bottom chỉ hiện khi người dùng chọn nhiều hơn một người để tạo nhóm
         */
        bottomLayout.setVisibility(View.GONE);
        usersSelected = new ArrayList<>();
        selectedUserAdapter = new SelectedUserCreateGroupAdapter(CreateGroupActivity.this, usersSelected);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(CreateGroupActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rcv_create_group_selected.setLayoutManager(layoutManager);
        rcv_create_group_selected.setAdapter(selectedUserAdapter);

        progress = new ProgressDialog(this);
        create_group_continue.setOnClickListener(v -> {
            progress.setMessage(getResources().getString(R.string.creating_room_message_dialog));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(false);
            progress.setCancelable(false);
            progress.show();
            try {
                createGroup();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        image_create_group.setOnClickListener(v -> {
            // xin quyền truy cập vào bộ sưu tập
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });

        image_create_group_delete.setOnClickListener(v -> {
            imageGroupFile = null;
            initImageCreate();
            image_create_group_delete.setVisibility(View.GONE);
        });

        loadFriendList();
    }

    private void initImageCreate() {
        Glide.with(this)
                .load(R.drawable.ic_camera_64)
                .placeholder(R.drawable.image_placeholer)
                .into(image_create_group);
        image_create_group.setBackground(null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!txt_create_group_name.getText().toString().trim().isEmpty()
                || !usersSelected.isEmpty() || imageGroupFile != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.confirm_abort_create_group)
                    .setPositiveButton(R.string.continue_create_group, (dialog, id) -> dialog.dismiss())
                    .setNegativeButton(R.string.abort_create_group, (dialog, id) -> {
                        dialog.cancel();
                        onBackPressed();
                    });
            builder.create().show();
        } else
            onBackPressed();
        return true;
    }

    /*
    tìm kiếm user theo tên hoặc số điện thoại
     */
    /*private void search(String newText) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_USER + "search",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        Type listType = new TypeToken<List<UserProfileDto>>() {
                        }.getType();
                        List<UserProfileDto> searchUserResult = new Gson().fromJson(res, listType);
                        Log.d("", searchUserResult.toString());

                        adapter.setList(searchUserResult);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("search friend error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("textToSearch", newText);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.getCache().clear();
        requestQueue.add(request);
    }*/

    /*
    load tất cả bạn bè để chọn thành viên
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadFriendList() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        Type listType = new TypeToken<List<FriendDTO>>() {
                        }.getType();
                        JSONObject jsonObject = new JSONObject(res);
                        List<FriendDTO> friendList = new Gson().fromJson(jsonObject.get("content").toString(), listType);
                        Log.d("", friendList.toString());

                        userFriends = friendList.stream().map(FriendDTO::getFriend).collect(Collectors.toList());
                        adapter.setList(userFriends);

                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("search friend error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.getCache().clear();
        requestQueue.add(request);
    }

    /*
    nhận data từ adapter tìm kiếm gửi qua khi click chọn một người
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void addUserToGroup(Serializable serializable) {
        usersSelected.add((UserProfileDto) serializable);
        selectedUserAdapter.notifyDataSetChanged();

        rcv_create_group_selected.getLayoutManager().smoothScrollToPosition(rcv_create_group_selected,
                new RecyclerView.State(), selectedUserAdapter.getItemCount());
        toolbar.setSubtitle(String.format("%d%s", usersSelected.size(), " thành viên."));

        if (!usersSelected.isEmpty()) {
            bottomLayout.setVisibility(View.VISIBLE);
            if (!txt_create_group_name.getText().toString().isEmpty()) {
                create_group_continue.setBackgroundTintList(null);
                create_group_continue.setEnabled(true);
            }
        }
    }

    /*
    nhận data từ adapter tìm kiếm và adapter selected gửi qua khi bỏ chọn một người
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void deleteUser(String idToDelete) {
        usersSelected.removeIf(x -> x.getId().equals(idToDelete));
        adapter.uncheckForUser(idToDelete);
        selectedUserAdapter.notifyDataSetChanged();

        toolbar.setSubtitle(String.format("%d%s", usersSelected.size(), " thành viên."));
        if (usersSelected.isEmpty()) {
            bottomLayout.setVisibility(View.GONE);
            toolbar.setSubtitle("Chưa có thành viên.");
            create_group_continue.setBackgroundTintList(backgroundTintList);
            create_group_continue.setEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*
        sau khi chọn ảnh đại diện của nhóm xong
         */
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getData() != null) {
                imageGroupFile = new File(PathUtil.getPath(this, data.getData()));
            }
            if (imageGroupFile != null) {
                Glide.with(this)
                        .load(imageGroupFile.getPath())
                        .centerCrop()
                        .circleCrop()
                        .placeholder(R.drawable.image_placeholer)
                        .into(image_create_group);
                image_create_group.setBackgroundResource(R.drawable.background_circle_image);
                image_create_group_delete.setVisibility(View.VISIBLE);
            } else {
                initImageCreate();
                image_create_group_delete.setVisibility(View.GONE);
            }
        } else {
            // chưa có hình ảnh nào được chọn
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    khi tạo nhóm, gửi ảnh đại diện của nhóm lên, đợi trả về link ảnh rồi mới set cho nhóm
    sau đó mới tạo nhóm
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createGroup() throws JSONException {
        List<File> files = new ArrayList<>();
        if (imageGroupFile != null) {
            files.add(imageGroupFile);
            MultiPartFileRequest<String> restApiMultiPartRequest =
                    new MultiPartFileRequest<String>(
                            Constant.API_FILE,
                            new HashMap<>(), // danh sách request param
                            files,
                            response -> {
                                String res = null;
                                try {
                                    res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                    Type listType = new TypeToken<List<String>>() {
                                    }.getType();
                                    List<String> urls = gson.fromJson(res, listType);
                                    createGroupAfterUploadImage(urls);
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> {
                                Log.i("upload file error", "error");
                            }) {

                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Authorization", "Bearer " + token);
                            return map;
                        }
                    };

            restApiMultiPartRequest.setRetryPolicy(new DefaultRetryPolicy(0, 1, 2));//10000
            Volley.newRequestQueue(this).add(restApiMultiPartRequest);
        } else {
            createGroupAfterUploadImage(new ArrayList<>());
        }
    }

    /*
    tạo nhóm sau khi đã có link ảnh đại diện
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createGroupAfterUploadImage(List<String> urls) throws JSONException {
        Set<MemberCreateDto> members = usersSelected.stream().map(x -> MemberCreateDto.builder()
                .userId(x.getId())
                .addTime(sdfYMD.format(new Date()))
                .addByUserId(user.getId())
                .build())
                .collect(Collectors.toSet());
        RoomCreateDto room = RoomCreateDto.builder()
                .name(txt_create_group_name.getText().toString().trim())
                .type(RoomType.GROUP)
                .createByUserId(user.getId())
                .members(members)
                .build();
        Log.d("----room create", room.toString());

        if (urls != null && !urls.isEmpty()) {
            room.setImageUrl(urls.get(0));
        }
        JSONObject object = new JSONObject(gson.toJson(room));
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constant.API_ROOM, object,
                response -> {
                    String res;
                    try {
                        res = URLDecoder.decode(URLEncoder.encode(response.toString(), "iso8859-1"), "UTF-8");
                        Log.d("///", res);

                        InboxDto inboxDto = gson.fromJson(res, InboxDto.class);

                        Intent intent = new Intent(this, ChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", inboxDto);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        progress.cancel();
                        startActivity(intent);
                        finish();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }, error -> {

        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(0, 1, 2));//10000
        Volley.newRequestQueue(this).add(request);
    }
}