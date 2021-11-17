package com.example.chatapp.ui.main;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.chatapp.cons.SendingData;
import com.example.chatapp.cons.WebSocketClient;
import com.example.chatapp.cons.ZoomOutPageTransformer;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReactionReceiver;
import com.example.chatapp.dto.ReadByReceiver;
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

public class MainActivity extends AppCompatActivity implements SendingData {
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

    /*
    lắng nghe sự kiện thay đổi ngôn ngữ
     */
    private final BroadcastReceiver changeLanguage = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                boolean change = bundle.getBoolean("change");
                if (change) {
                    updateUIAfterChangeLanguage(mCurrentPosition);
                }
            }
        }
    };

    /*
    cập nhật lại ui khi thay đổi ngôn ngữ
     */
    private void updateUIAfterChangeLanguage(int position) {
        if (position == 0) {
            String title = getString(R.string.title_message);
            MenuItem item = bnv_menu.getMenu().getItem(position);
            setTitle(title);
            item.setTitle(title);
        } else if (position == 1) {
            String title = getString(R.string.title_contact);
            MenuItem item = bnv_menu.getMenu().getItem(position);
            setTitle(title);
            item.setTitle(title);
        } else if (position == 2) {
            String title = getString(R.string.title_group);
            MenuItem item = bnv_menu.getMenu().getItem(position);
            setTitle(title);
            item.setTitle(title);
        } else if (position == 3) {
            String title = getString(R.string.title_recent);
            MenuItem item = bnv_menu.getMenu().getItem(position);
            setTitle(title);
            item.setTitle(title);
        } else {
            String title = getString(R.string.title_more);
            MenuItem item = bnv_menu.getMenu().getItem(position);
            setTitle(title);
            item.setTitle(title);
        }
    }

    @SuppressLint("CheckResult")
    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // đăng ký lắng nghe sự kiện thay đổi ngôn ngữ
        LocalBroadcastManager.getInstance(this).registerReceiver(changeLanguage, new IntentFilter("language/change"));

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

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/read")
                .subscribe(x -> {
                    ReadByReceiver readByReceiver = gson.fromJson(x.getPayload(), ReadByReceiver.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("messages/read");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", readByReceiver);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/reaction")
                .subscribe(x -> {
                    ReactionReceiver reactionReceiver = gson.fromJson(x.getPayload(), ReactionReceiver.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("messages/reaction");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", reactionReceiver);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                    });
                }, throwable -> {
                    Log.i("erro--", throwable.getMessage());
                });

        WebSocketClient.getInstance().getStompClient()
                .topic("/users/queue/messages/delete")
                .subscribe(x -> {
                    MessageDto deletedMessage = gson.fromJson(x.getPayload(), MessageDto.class);
                    MainActivity.this.runOnUiThread(() -> {
                        Intent intent = new Intent("messages/delete");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", deletedMessage);
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
                updateUIAfterChangeLanguage(position);
                mCurrentPosition = position;
                if (position == 0) {
                    bnv_menu.getMenu().findItem(R.id.navigation_message).setChecked(true);
                } else if (position == 1) {
                    bnv_menu.getMenu().findItem(R.id.navigation_contact).setChecked(true);
                } else if (position == 2) {
                    bnv_menu.getMenu().findItem(R.id.navigation_group).setChecked(true);
                } else if (position == 3) {
                    bnv_menu.getMenu().findItem(R.id.navigation_recent).setChecked(true);
                } else {
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

//        int page = getIntent().getIntExtra("page", 0);
//        Log.e("page : ====== : ", page + "");
//        viewPager.setCurrentItem(page);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_message) {
                mCurrentPosition = 0;
                viewPager.setCurrentItem(mCurrentPosition);
                updateUIAfterChangeLanguage(mCurrentPosition);
                return true;
            }
            if (item.getItemId() == R.id.navigation_contact) {
                mCurrentPosition = 1;
                viewPager.setCurrentItem(mCurrentPosition);
                updateUIAfterChangeLanguage(mCurrentPosition);
                return true;
            }
            if (item.getItemId() == R.id.navigation_group) {
                mCurrentPosition = 2;
                viewPager.setCurrentItem(mCurrentPosition);
                updateUIAfterChangeLanguage(mCurrentPosition);
                return true;
            }
            if (item.getItemId() == R.id.navigation_recent) {
                mCurrentPosition = 3;
                viewPager.setCurrentItem(mCurrentPosition);
                updateUIAfterChangeLanguage(mCurrentPosition);
                return true;
            }
            if (item.getItemId() == R.id.navigation_info) {
                mCurrentPosition = 4;
                viewPager.setCurrentItem(mCurrentPosition);
                updateUIAfterChangeLanguage(mCurrentPosition);
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

    @Override
    public void sendString(String s) {
//        if (Boolean.parseBoolean(s)) {
//            Intent intent = getIntent();
//            intent.putExtra("page", 4);
//            finish();
//            startActivity(intent);
//        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return messageFragment;
            } else if (position == 1) {
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

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(changeLanguage);

        super.onDestroy();
    }
}