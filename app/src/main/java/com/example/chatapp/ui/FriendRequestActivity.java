package com.example.chatapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.SendDataFriendRequest;
import com.example.chatapp.cons.ZoomOutPageTransformer;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.FriendRequest;
import com.example.chatapp.ui.main.frag.FriendRequestReceivedFragment;
import com.example.chatapp.ui.main.frag.FriendRequestSentFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class FriendRequestActivity extends AppCompatActivity implements SendDataFriendRequest {
    private TabLayout tabLayout_friend_request;
    private FriendRequestReceivedFragment friendRequestReceivedFragment;
    private FriendRequestSentFragment friendRequestSentFragment;
    private final int NUM_PAGES = 2;
    private final int POSITION_OF_RECEIVED = 0;
    private final int POSITION_OF_SENT = 1;
    private String token;
    private int[] counts = new int[NUM_PAGES];
    private String[] title = new String[NUM_PAGES];
    private UserSummaryDTO currentUser;

    private final BroadcastReceiver friendRequestReceived = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (currentUser.getId().equals(dto.getTo().getId())) {
                    friendRequestReceivedFragment.receivedFriendRequest(dto);
                    counts[POSITION_OF_RECEIVED] = counts[POSITION_OF_RECEIVED] + 1;
                    updateCountSearchResult(POSITION_OF_RECEIVED, counts[POSITION_OF_RECEIVED]);
                } else if (currentUser.getId().equals(dto.getFrom().getId())) {
                    friendRequestSentFragment.sentFriendRequest(dto);
                    counts[POSITION_OF_SENT] = counts[POSITION_OF_SENT] + 1;
                    updateCountSearchResult(POSITION_OF_SENT, counts[POSITION_OF_SENT]);
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
                    friendRequestSentFragment.acceptFriendRequest(dto);
                    if (counts[POSITION_OF_SENT] > 0)
                        counts[POSITION_OF_SENT] = counts[POSITION_OF_SENT] - 1;
                    updateCountSearchResult(POSITION_OF_SENT, counts[POSITION_OF_SENT]);
                } else if (currentUser.getId().equals(dto.getTo().getId())) {
                    friendRequestReceivedFragment.removeAcceptedFriendRequest(dto);
                    if (counts[POSITION_OF_RECEIVED] > 0)
                        counts[POSITION_OF_RECEIVED] = counts[POSITION_OF_RECEIVED] - 1;
                    updateCountSearchResult(POSITION_OF_RECEIVED, counts[POSITION_OF_RECEIVED]);
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
                    friendRequestSentFragment.deleteFriendRequest(dto);
                    if (counts[POSITION_OF_SENT] > 0)
                        counts[POSITION_OF_SENT] = counts[POSITION_OF_SENT] - 1;
                    updateCountSearchResult(POSITION_OF_SENT, counts[POSITION_OF_SENT]);
                } else if (currentUser.getId().equals(dto.getTo().getId())) {
                    friendRequestReceivedFragment.recallFriendRequest(dto);
                    if (counts[POSITION_OF_RECEIVED] > 0)
                        counts[POSITION_OF_RECEIVED] = counts[POSITION_OF_RECEIVED] - 1;
                    updateCountSearchResult(POSITION_OF_RECEIVED, counts[POSITION_OF_RECEIVED]);
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
                    friendRequestSentFragment.deleteFriendRequest(dto);
                    if (counts[POSITION_OF_SENT] > 0)
                        counts[POSITION_OF_SENT] = counts[POSITION_OF_SENT] - 1;
                    updateCountSearchResult(POSITION_OF_SENT, counts[POSITION_OF_SENT]);
                } else if (currentUser.getId().equals(dto.getTo().getId())) {
                    friendRequestReceivedFragment.recallFriendRequest(dto);
                    if (counts[POSITION_OF_RECEIVED] > 0)
                        counts[POSITION_OF_RECEIVED] = counts[POSITION_OF_RECEIVED] - 1;
                    updateCountSearchResult(POSITION_OF_RECEIVED, counts[POSITION_OF_RECEIVED]);
                }
            }
        }
    };

    public void updateCountSearchResult(int position, int count) {
        if (position < NUM_PAGES && tabLayout_friend_request.getTabAt(position) != null) {
            counts[position] = count;
            if (count != 0)
                tabLayout_friend_request.getTabAt(position).setText(String.format("%s(%d)", title[position], count));
            else
                tabLayout_friend_request.getTabAt(position).setText(title[position]);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestReceived, new IntentFilter("friendRequest/received"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestAccept, new IntentFilter("friendRequest/accept"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestRecall, new IntentFilter("friendRequest/recall"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestDelete, new IntentFilter("friendRequest/delete"));

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(1f)
                .build();

        Slidr.attach(this, config);

        title[0] = getString(R.string.received);
        title[1] = getString(R.string.sent);

        if (friendRequestReceivedFragment == null)
            friendRequestReceivedFragment = new FriendRequestReceivedFragment(this);

        if (friendRequestSentFragment == null)
            friendRequestSentFragment = new FriendRequestSentFragment(this);

        Gson gson = new Gson();
        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", Context.MODE_PRIVATE);
        currentUser = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        Toolbar toolbar_friend_request = findViewById(R.id.toolbar_friend_request);
        toolbar_friend_request.setTitleTextColor(Color.WHITE);
        toolbar_friend_request.setSubtitleTextColor(Color.WHITE);
        toolbar_friend_request.setTitle(getString(R.string.friend_request));
        setSupportActionBar(toolbar_friend_request);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tabLayout_friend_request = findViewById(R.id.tab_layout_friend_request);
        ViewPager2 viewPager_friend_request = findViewById(R.id.view_paper_friend_request);
        viewPager_friend_request.setAdapter(new ScreenSlidePagerAdapter(this));
        viewPager_friend_request.setPageTransformer(new ZoomOutPageTransformer());
        viewPager_friend_request.setOffscreenPageLimit(NUM_PAGES);

        tabLayout_friend_request.addTab(tabLayout_friend_request.newTab().setText(getString(R.string.received)));
        tabLayout_friend_request.addTab(tabLayout_friend_request.newTab().setText(getString(R.string.sent)));
        tabLayout_friend_request.setSelectedTabIndicatorColor(getResources().getColor(R.color.purple_200));

        new TabLayoutMediator(tabLayout_friend_request, viewPager_friend_request, (tab, position) -> {
            if (counts[position] == 0)
                tab.setText(title[position]);
            else
                tab.setText(String.format("%s(%d)", title[position], counts[position]));
        }).attach();

//        countFriendRequestReceived();
//        countFriendRequestSent();

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return friendRequestReceivedFragment;
            }
            return friendRequestSentFragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
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

    @Override
    public void countFriendRequestReceived() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST + "/count",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        int countReceived = Integer.parseInt(res);
                        counts[POSITION_OF_RECEIVED] = countReceived;
                        updateCountSearchResult(POSITION_OF_RECEIVED, counts[POSITION_OF_RECEIVED]);

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

    @Override
    public void countFriendRequestSent() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST + "/count/sent",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        int countSent = Integer.parseInt(res);
                        counts[POSITION_OF_SENT] = countSent;
                        updateCountSearchResult(POSITION_OF_SENT, counts[POSITION_OF_SENT]);

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

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestReceived);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestAccept);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestRecall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestDelete);
        super.onDestroy();
    }

}