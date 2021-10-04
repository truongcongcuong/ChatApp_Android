package com.example.chatapp.ui.main;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatapp.R;
import com.example.chatapp.cons.WebsocketClient;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.main.frag.ContactFragment;
import com.example.chatapp.ui.main.frag.GroupFragment;
import com.example.chatapp.ui.main.frag.InfoFragment;
import com.example.chatapp.ui.main.frag.MessageFragment;
import com.example.chatapp.ui.main.frag.RecentFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bnv_menu;
    private MessageFragment messageFragment;
    private ContactFragment contactFragment;
    private GroupFragment groupFragment;
    private RecentFragment recentFragment;
    private InfoFragment infoFragment;
    private Gson gson;
    private UserSummaryDTO user;

    private static final int NUM_PAGES = 5;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    private int mCurrentPosition;
    private int mScrollState;

    @SuppressLint("CheckResult")
    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tlb_main_activity);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        bnv_menu = findViewById(R.id.bnv_bot);
        gson = new Gson();
        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        messageFragment = new MessageFragment();
//        contactFragment = new ContactFragment();
//        groupFragment = new GroupFragment();
//        recentFragment = new RecentFragment();
//        inforFragment = new InforFragment();

        // connect to websocket
        WebsocketClient.getInstance().connect(user.getId(), user.getAccessToken());

        //subcribe message
        WebsocketClient.getInstance().getStompClient()
                .topic("/users/queue/messages")
                .subscribe(x -> {
                    MessageDto messageDto = gson.fromJson(x.getPayload(), MessageDto.class);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageFragment.setNewMessage(messageDto);
                        }
                    });
                }, throwable -> {
                    Log.i("main acti subc mess err", throwable.getMessage());
                });
        bnv_menu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        /*
        sửa lỗi mất list inbox ở message fragment khi từ fragment thứ 3 chuyển qua
         */
        viewPager.setOffscreenPageLimit(NUM_PAGES);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            /*
            khi thay đổi trang thì set checked cho item ở bottom menu
             */
            @Override
            public void onPageSelected(final int position) {
                mCurrentPosition = position;
                if (position == 0) {
                    setTitle(R.string.title_message);
                    bnv_menu.getMenu().findItem(R.id.navigation_message).setChecked(true);
                } else if (position == 1) {
                    setTitle(R.string.title_contact);
                    bnv_menu.getMenu().findItem(R.id.navigation_contact).setChecked(true);
                } else if (position == 2) {
                    setTitle(R.string.title_group);
                    bnv_menu.getMenu().findItem(R.id.navigation_group).setChecked(true);
                } else if (position == 3) {
                    setTitle(R.string.title_recent);
                    bnv_menu.getMenu().findItem(R.id.navigation_recent).setChecked(true);
                } else {
                    setTitle(R.string.title_more);
                    bnv_menu.getMenu().findItem(R.id.navigation_info).setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                handleScrollState(state);
                mScrollState = state;
            }

            private void handleScrollState(final int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && mScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
                    setNextItemIfNeeded();
                }
            }

            private void setNextItemIfNeeded() {
                if (!isScrollStateSettling()) {
                    handleSetNextItem();
                }
            }

            private boolean isScrollStateSettling() {
                return mScrollState == ViewPager.SCROLL_STATE_SETTLING;
            }

            private void handleSetNextItem() {
                /*
                cuộn về trang đầu khi đến trang cuối và ngược lại
                 */
                /*final int lastPosition = viewPager.getAdapter().getItemCount() - 1;
                if (mCurrentPosition == 0) {
                    viewPager.setCurrentItem(lastPosition, true);
                } else if (mCurrentPosition == lastPosition) {
                    viewPager.setCurrentItem(0, true);
                }*/
            }

            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_message:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_contact:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_group:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_recent:
                    viewPager.setCurrentItem(3);
                    return true;
                case R.id.navigation_info:
                    viewPager.setCurrentItem(4);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("resume", "main activity resume");
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0)
                return messageFragment;
            else if (position == 1) {
                if (contactFragment == null)
                    contactFragment = new ContactFragment();
                return contactFragment;
            } else if (position == 2) {
                if (groupFragment == null)
                    groupFragment = new GroupFragment();
                return groupFragment;
            } else if (position == 3) {
                if (recentFragment == null)
                    recentFragment = new RecentFragment();
                return recentFragment;
            } else {
                if (infoFragment == null)
                    infoFragment = new InfoFragment();
                return infoFragment;
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
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

}