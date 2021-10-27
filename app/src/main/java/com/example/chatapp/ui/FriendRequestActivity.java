package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.example.chatapp.ui.main.frag.FriendRequestReceivedFragment;
import com.example.chatapp.ui.main.frag.FriendRequestSentFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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
    private static final int NUM_PAGES = 2;
    private String token;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

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

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

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
        }).attach();

        countFriendRequestReceived();
        countFriendRequestSent();

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                if (friendRequestReceivedFragment == null)
                    friendRequestReceivedFragment = new FriendRequestReceivedFragment();
                return friendRequestReceivedFragment;
            }
            if (friendRequestSentFragment == null)
                friendRequestSentFragment = new FriendRequestSentFragment();
            return friendRequestSentFragment;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    /*
    hiệu ứng chuyển trang khi cuộn
     */
    static class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 1f;
        private static final float MIN_ALPHA = 1f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
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
                        TabLayout.Tab firstTab = tabLayout_friend_request.getTabAt(0);
                        Log.d("--countReceived", countReceived + "");

                        if (firstTab != null) {
                            if (countReceived != 0)
                                firstTab.setText(String.format("%s(%d)", getString(R.string.received), countReceived));
                            else
                                firstTab.setText(getString(R.string.received));
                        } else
                            Log.d("--firstTab", "null");

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
                        TabLayout.Tab secondTab = tabLayout_friend_request.getTabAt(1);
                        Log.d("--countReceived", countSent + "");
                        if (secondTab != null) {
                            if (countSent != 0)
                                secondTab.setText(String.format("%s(%d)", getString(R.string.sent), countSent));
                            else
                                secondTab.setText(getString(R.string.sent));
                        } else
                            Log.d("--firstTab", "null");

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

}