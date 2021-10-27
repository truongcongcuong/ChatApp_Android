package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.SearchUserAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.UserProfileDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AddFriendActivity extends AppCompatActivity {
    private Timer timer;
    private String token;
    private RecyclerView recyclerView;
    private SearchUserAdapter searchUserAdapter;
    private List<UserProfileDto> searchUserResult;
    private LinearLayout add_friend_layout_search;
    private TextView add_friend_txt_notify;
    private final int DELAY_SEARCH = 250;
    private static String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        timer = new Timer();
        searchUserResult = Collections.emptyList();
        searchUserAdapter = new SearchUserAdapter(this, searchUserResult);

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        message = getString(R.string.find_friend_via_phone_number);
        SearchView searchView = findViewById(R.id.add_friend_search_view);
        recyclerView = findViewById(R.id.add_friend_recyclerview);
        recyclerView.setVisibility(View.GONE);
        add_friend_layout_search = findViewById(R.id.add_friend_layout_search);
        add_friend_layout_search.setVisibility(View.VISIBLE);
        add_friend_txt_notify = findViewById(R.id.add_friend_txt_notify);
        add_friend_txt_notify.setText(message);
        Toolbar toolbar = findViewById(R.id.add_friend_toolbar);
        toolbar.setTitle(R.string.add_friend_title);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        
        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        searchView.setQueryHint(message);
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);

        /*
        tìm icon close, icon search, và edit text nàm trên searchview
         */
        int searchIcon = searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        int closeIconId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        int editTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);

        /*
        xóa icon seach trên searchview
         */
        ImageView magImage = searchView.findViewById(searchIcon);
        magImage.setVisibility(View.GONE);
        magImage.setImageDrawable(null);

        /*
        set padding cho edit text
         */
        EditText editText = searchView.findViewById(editTextId);
        editText.setPadding(50, 0, 50, 0);

        /*
        set sự kiện khi click icon close trên searchview
         */
        ImageView closeIcon = searchView.findViewById(closeIconId);
        closeIcon.setImageResource(R.drawable.ic_baseline_close_circle_24);
        closeIcon.setOnClickListener(v -> {
            try {
                searchUserResult.clear();
                searchUserAdapter.setList(searchUserResult);
            } catch (Exception e) {
                searchUserAdapter = new SearchUserAdapter(this, null);
            }
            editText.setText("");
            recyclerView.setVisibility(View.GONE);
            add_friend_layout_search.setVisibility(View.VISIBLE);
            add_friend_txt_notify.setText(message);
        });

        /*
        view nằm bên ngoài edit text của search view, set chiều cao của nó wrap vừa với edit text
         */
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        ViewGroup.LayoutParams params = searchPlate.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        searchPlate.setPadding(0, 5, 0, 5);
        searchPlate.setLayoutParams(params);
        searchPlate.setBackgroundResource(R.drawable.search_view_background);

        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
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
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    searchUserWithPhoneNumber(newText);
                                }
                            },
                            DELAY_SEARCH
                    );
                } else {
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        searchUserResult.clear();
                                        searchUserAdapter.setList(searchUserResult);
                                        add_friend_layout_search.setVisibility(View.VISIBLE);
                                        add_friend_txt_notify.setText(message);
                                    } catch (Exception e) {
                                        searchUserAdapter = new SearchUserAdapter(AddFriendActivity.this, null);
                                    }
                                }
                            },
                            0
                    );
                }
                return false;
            }
        });

    }

    private void visibleOrGoneSearchLayout() {
        if (searchUserResult.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            add_friend_layout_search.setVisibility(View.VISIBLE);
            add_friend_txt_notify.setText(getString(R.string.no_result));
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            add_friend_layout_search.setVisibility(View.GONE);
        }
    }

    private void searchUserWithPhoneNumber(String phoneNumber) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_USER + "searchPhone",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        Type listType = new TypeToken<List<UserProfileDto>>() {
                        }.getType();
                        searchUserResult = new Gson().fromJson(res, listType);
                        Log.d("", searchUserResult.toString());

                        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                        if (recyclerView.getLayoutManager() == null)
                            recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setVisibility(View.VISIBLE);
                        add_friend_layout_search.setVisibility(View.GONE);
                        recyclerView.setAdapter(searchUserAdapter);
                        searchUserAdapter.setList(searchUserResult);
                        visibleOrGoneSearchLayout();

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
                map.put("textToSearch", phoneNumber);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.getCache().clear();
        requestQueue.add(request);
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
    }

}