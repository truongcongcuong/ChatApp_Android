package com.example.chatapp.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserCreateGroupAdapter;
import com.example.chatapp.adapter.SelectedUserCreateGroupAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.SendDataCreateRoomActivity;
import com.example.chatapp.dto.FriendDTO;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MemberCreateDto;
import com.example.chatapp.dto.MemberDto;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class AddMemberActivity extends AppCompatActivity implements SendDataCreateRoomActivity {
    private SearchView txt_add_member_find_user;
    private SimpleDateFormat sdfYMD;
    private InboxDto inboxDto;
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
    private List<UserProfileDto> searchResult;

    /*
    adpater những người đã được chọn để tạo nhóm
     */
    private SelectedUserCreateGroupAdapter selectedUserAdapter;
    private RecyclerView rcv_add_member_selected;
    private Button add_member_continue;
    private RelativeLayout bottomLayout;
    private Toolbar toolbar;
    /*
    dùng để set màu trở lại như cũ cho button tạo nhóm khi enable hoặc disable
     */
    private ColorStateList backgroundTintList;
    private Timer timer;
    private final long DELAY_SEARCH = 250;
    private int page = 0;
    private int pageSearch = 0;
    private final int size = 1;
    private Button btnLoadMore;

    private final BroadcastReceiver addMember = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                inboxDto.setRoom(newRoom);
                loadFriendList();
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
                loadFriendList();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

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
        timer = new Timer();
        searchResult = new ArrayList<>(0);
        txt_add_member_find_user = findViewById(R.id.txt_add_member_find_user);
        btnLoadMore = findViewById(R.id.add_member_btn_load_more);
        btnLoadMore.setVisibility(View.GONE);
        RecyclerView rcv_user_add_member_activity = findViewById(R.id.rcv_user_add_member_activity);

        rcv_add_member_selected = findViewById(R.id.rcv_add_member_selected);
        add_member_continue = findViewById(R.id.add_member_continue);
        bottomLayout = findViewById(R.id.add_member_bottom_layout);

        toolbar = findViewById(R.id.tlb_add_member_activity);
        toolbar.setTitle(getString(R.string.title_add_member));
        toolbar.setSubtitle(String.format("%s: %d", getString(R.string.add_member_selected), 0));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        sdfYMD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            inboxDto = (InboxDto) bundle.getSerializable("dto");

        add_member_continue.setEnabled(false);
        backgroundTintList = add_member_continue.getBackgroundTintList();

        txt_add_member_find_user.setQueryHint(getResources().getString(R.string.find_by_name_or_phone));
        txt_add_member_find_user.setIconifiedByDefault(false);
        txt_add_member_find_user.setFocusable(true);

        /*
        tìm icon close, icon search, và edit text nàm trên searchview
         */
        int searchIcon = txt_add_member_find_user.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        int closeIconId = txt_add_member_find_user.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        int editTextId = txt_add_member_find_user.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);

        /*
        xóa icon seach trên searchview
         */
        ImageView magImage = txt_add_member_find_user.findViewById(searchIcon);
        magImage.setVisibility(View.GONE);
        magImage.setImageDrawable(null);

        /*
        set padding cho edit text
         */
        EditText editText = txt_add_member_find_user.findViewById(editTextId);
        editText.setPadding(50, 0, 50, 0);

        /*
        set sự kiện khi click icon close trên searchview
         */
        ImageView closeIcon = txt_add_member_find_user.findViewById(closeIconId);
        closeIcon.setImageResource(R.drawable.ic_baseline_close_circle_24);
        closeIcon.setOnClickListener(v -> {
            page = 0;
            pageSearch = 0;
            editText.setText("");
            loadFriendList();
        });

        /*
        view nằm bên ngoài edit text của search view, set chiều cao của nó wrap vừa với edit text
         */
        int searchPlateId = txt_add_member_find_user.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = txt_add_member_find_user.findViewById(searchPlateId);
        ViewGroup.LayoutParams params = searchPlate.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        searchPlate.setLayoutParams(params);
        searchPlate.setPadding(0, 5, 0, 5);
        searchPlate.setBackgroundResource(R.drawable.search_view_background);

        rcv_user_add_member_activity.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new SearchUserCreateGroupAdapter(this, null, null);
        rcv_user_add_member_activity.setAdapter(adapter);

        txt_add_member_find_user.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                if (!newText.trim().isEmpty()) {
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    pageSearch = 0;
                                    search(newText);
                                }
                            },
                            DELAY_SEARCH
                    );
                } else {
                    page = 0;
                    pageSearch = 0;
                    timer.cancel();
                    loadFriendList();
                }
                return false;
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
        selectedUserAdapter = new SelectedUserCreateGroupAdapter(this, usersSelected);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rcv_add_member_selected.setLayoutManager(layoutManager);
        rcv_add_member_selected.setAdapter(selectedUserAdapter);

        add_member_continue.setOnClickListener(v -> {
            addMember();
        });

        loadFriendList();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addMember() {
        Set<MemberCreateDto> members = usersSelected.stream().map(x -> MemberCreateDto.builder()
                .userId(x.getId())
                .addTime(sdfYMD.format(new Date()))
                .addByUserId(user.getId())
                .build())
                .collect(Collectors.toSet());

        JSONArray array = new JSONArray();
        try {
            array = new JSONArray(gson.toJson(members));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, Constant.API_ROOM + "members/" + inboxDto.getRoom().getId(), array,
                response -> {
                    String res = response.toString();
                    Type listType = new TypeToken<Set<MemberDto>>() {
                    }.getType();
                    Set<MemberDto> memberCreate = gson.fromJson(res, listType);
                    if (!memberCreate.isEmpty()) {
                        RoomDTO room = inboxDto.getRoom();
                        room.setMembers(memberCreate);
                        inboxDto.setRoom(room);
                        Log.d("new room", room.toString());
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.add_member_success))
                            .setPositiveButton(getString(R.string.confirm_button), (dialog, id) -> {
                                dialog.cancel();
                                finish();
                            });
                    builder.setCancelable(false);
                    builder.create().show();
                }, error -> {
            String er;
            try {
                er = URLDecoder.decode(URLEncoder.encode(error.toString(), "iso8859-1"), "UTF-8");
                Log.d("///", er);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /*
    tìm kiếm user theo tên hoặc số điện thoại
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void search(String newText) {
        btnLoadMore.setOnClickListener(v -> {
            pageSearch++;
            search(newText);
        });
        List<String> exclude = inboxDto.getRoom().getMembers().stream().map(x -> x.getUser().getId())
                .collect(Collectors.toList());
        JSONArray jsonArray = new JSONArray(exclude);
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.POST,
                Constant.API_FRIEND_LIST + "/notIn" + "?query=" + newText
                        + "&page=" + pageSearch + "&size=" + size,
                jsonArray,
                response -> {
                    try {
                        if (response != null && response.length() > 0) {
                            JSONObject object = response.getJSONObject(0);
                            JSONArray content = (JSONArray) object.get("content");
                            boolean last = (boolean) object.get("last");
                            if (last)
                                btnLoadMore.setVisibility(View.GONE);
                            else
                                btnLoadMore.setVisibility(View.VISIBLE);
                            Type type = new TypeToken<List<FriendDTO>>() {
                            }.getType();
                            List<FriendDTO> list = gson.fromJson(content.toString(), type);
                            List<UserProfileDto> users = list.stream().map(FriendDTO::getFriend).collect(Collectors.toList());
                            if (pageSearch == 0) {
                                searchResult = users;
                            } else {
                                searchResult.addAll(users);
                            }
                            adapter.setList(searchResult);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        arrayRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(arrayRequest);
    }

    /*
    load tất cả bạn bè để chọn thành viên
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadFriendList() {
        btnLoadMore.setOnClickListener(v -> {
            page++;
            loadFriendList();
        });
        List<String> exclude = inboxDto.getRoom().getMembers().stream().map(x -> x.getUser().getId())
                .collect(Collectors.toList());
        JSONArray jsonArray = new JSONArray(exclude);
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.POST,
                Constant.API_FRIEND_LIST + "/notIn"
                        + "?size=" + size + "&page=" + page,
                jsonArray,
                response -> {
                    try {
                        if (response != null && response.length() > 0) {
                            JSONObject object = response.getJSONObject(0);
                            JSONArray content = (JSONArray) object.get("content");
                            boolean last = (boolean) object.get("last");
                            if (last)
                                btnLoadMore.setVisibility(View.GONE);
                            else
                                btnLoadMore.setVisibility(View.VISIBLE);
                            Type type = new TypeToken<List<FriendDTO>>() {
                            }.getType();
                            List<FriendDTO> list = gson.fromJson(content.toString(), type);
                            List<UserProfileDto> userList = list.stream().map(FriendDTO::getFriend).collect(Collectors.toList());
                            if (page == 0) {
                                userFriends = userList;
                            } else {
                                userFriends.addAll(userList);
                            }
                            adapter.setList(userFriends);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        arrayRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(arrayRequest);
    }

    /*
    nhận data từ adapter tìm kiếm gửi qua khi click chọn một người
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void addUserToGroup(Serializable serializable) {
        usersSelected.add((UserProfileDto) serializable);
        selectedUserAdapter.notifyDataSetChanged();

        rcv_add_member_selected.getLayoutManager().smoothScrollToPosition(rcv_add_member_selected,
                new RecyclerView.State(), selectedUserAdapter.getItemCount());
        toolbar.setSubtitle(String.format("%s: %d", getString(R.string.add_member_selected), usersSelected.size()));

        if (!usersSelected.isEmpty()) {
            bottomLayout.setVisibility(View.VISIBLE);
            add_member_continue.setBackgroundTintList(null);
            add_member_continue.setEnabled(true);
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

        toolbar.setSubtitle(String.format("%s: %d", getString(R.string.add_member_selected), usersSelected.size()));
        if (usersSelected.isEmpty()) {
            bottomLayout.setVisibility(View.GONE);
            toolbar.setSubtitle(String.format("%s: %d", getString(R.string.add_member_selected), 0));
            add_member_continue.setBackgroundTintList(backgroundTintList);
            add_member_continue.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (!txt_add_member_find_user.getQuery().toString().trim().isEmpty()
                || !usersSelected.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.add_member_cancel))
                    .setPositiveButton(getString(R.string.add_member_continue), (dialog, id) -> dialog.dismiss())
                    .setNegativeButton(getString(R.string.cancel_button), (dialog, id) -> {
                        dialog.cancel();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("dto", inboxDto);
                        setResult(Activity.RESULT_OK, resultIntent);
                        super.onBackPressed();
                        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                        super.finish();
                    });
            builder.create().show();
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("dto", inboxDto);
            setResult(Activity.RESULT_OK, resultIntent);
            super.onBackPressed();
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            super.finish();
        }
    }

    @Override
    public void finish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("dto", inboxDto);
        setResult(Activity.RESULT_OK, resultIntent);
        super.finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

}