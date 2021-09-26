package com.example.chatapp.ui.main;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatapp.R;
import com.example.chatapp.cons.WebsocketClient;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.main.frag.ContactFragment;
import com.example.chatapp.ui.main.frag.GroupFragment;
import com.example.chatapp.ui.main.frag.InforFragment;
import com.example.chatapp.ui.main.frag.MessageFragment;
import com.example.chatapp.ui.main.frag.RecentFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bnv_menu;
    private MessageFragment messageFragment;
    private Gson gson;
    private UserSummaryDTO user;

    @SuppressLint("CheckResult")
    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.title_bar_gray)));
            actionBar.setTitle("");
            actionBar.show();
        }


        bnv_menu = findViewById(R.id.bnv_bot);
        gson = new Gson();
        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        messageFragment = new MessageFragment();

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
                            Log.d("message receiver", messageDto.getSender().getDisplayName() + " " + messageDto.getContent());
                            messageFragment.setNewMessage(messageDto);
                        }
                    });
                }, throwable -> {
                    Log.i("main acti subc mess err", throwable.getMessage());
                });
        bnv_menu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(messageFragment);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_message:
                    messageFragment = new MessageFragment();
                    loadFragment(messageFragment);
                    return true;
                case R.id.navigation_contact:
                    fragment = new ContactFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_group:
                    fragment = new GroupFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_recent:
                    fragment = new RecentFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_info:
                    fragment = new InforFragment();
                    loadFragment(fragment);
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

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fla_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}