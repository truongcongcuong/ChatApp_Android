package com.example.chatapp.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatapp.R;
import com.example.chatapp.cons.WebSocketClient;
import com.example.chatapp.cons.ZoomOutPageTransformer;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.FriendRequest;
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

    private final int NUM_PAGES = 5;
    private ViewPager2 viewPager;

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
        UserSummaryDTO user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        messageFragment = new MessageFragment(this);
//        contactFragment = new ContactFragment();
//        groupFragment = new GroupFragment();
//        recentFragment = new RecentFragment();
//        inforFragment = new InforFragment();

        // connect to websocket
        WebSocketClient.getInstance().connect(user.getId(), user.getAccessToken());

        //subcribe message
        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/messages")
                .subscribe(x -> {
                    MessageDto messageDto = gson.fromJson(x.getPayload(), MessageDto.class);
                    MainActivity.this.runOnUiThread(() -> {
                        messageFragment.setNewMessage(messageDto);
                        Intent intent = new Intent("messages/new");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", messageDto);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("main acti subc mess err", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/friendRequest/received")
                .subscribe(x -> {
                    FriendRequest dto = gson.fromJson(x.getPayload(), FriendRequest.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("friendRequest/received");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", dto);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/friendRequest/accept")
                .subscribe(x -> {
                    FriendRequest dto = gson.fromJson(x.getPayload(), FriendRequest.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("friendRequest/accept");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", dto);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);

                        /*
                        khi đồng ý thêm bạn bè thì sẽ thông báo đến contact fragment để update lại list friend
                         */
                        Intent acceptFriendIntent = new Intent("accept_friend");
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(acceptFriendIntent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/friendRequest/recall")
                .subscribe(x -> {
                    FriendRequest dto = gson.fromJson(x.getPayload(), FriendRequest.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("friendRequest/recall");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", dto);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/friendRequest/delete")
                .subscribe(x -> {
                    FriendRequest dto = gson.fromJson(x.getPayload(), FriendRequest.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("friendRequest/delete");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", dto);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/room/members/add")
                .subscribe(x -> {
                    RoomDTO newRoom = gson.fromJson(x.getPayload(), RoomDTO.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("room/members/add");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", newRoom);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/room/members/delete")
                .subscribe(x -> {
                    RoomDTO newRoom = gson.fromJson(x.getPayload(), RoomDTO.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("room/members/delete");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", newRoom);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/room/rename")
                .subscribe(x -> {
                    RoomDTO newRoom = gson.fromJson(x.getPayload(), RoomDTO.class);
                    System.out.println("newRoom rename = " + newRoom);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("room/rename");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", newRoom);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/room/changeImage")
                .subscribe(x -> {
                    RoomDTO newRoom = gson.fromJson(x.getPayload(), RoomDTO.class);
                    System.out.println("newRoom changeImage = " + newRoom);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("room/changeImage");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", newRoom);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });
        bnv_menu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
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

}