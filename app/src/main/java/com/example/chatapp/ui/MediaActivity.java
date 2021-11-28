package com.example.chatapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatapp.R;
import com.example.chatapp.cons.ZoomOutPageTransformer;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.main.frag.MediaFileFragment;
import com.example.chatapp.ui.main.frag.MediaImageVideoFragment;
import com.example.chatapp.ui.main.frag.MediaLinkFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

public class MediaActivity extends AppCompatActivity {

    private MediaImageVideoFragment mediaImageVideoFragment;
    private MediaFileFragment mediaFileFragment;
    private MediaLinkFragment mediaLinkFragment;
    private final int NUM_PAGES = 3;
    private final int POSITION_OF_IMAGE = 0;
    private final int POSITION_OF_FILE = 1;
    private final int POSITION_OF_LINK = 2;
    private String token;
    private int[] counts = new int[NUM_PAGES];
    private String[] title = new String[NUM_PAGES];
    private UserSummaryDTO currentUser;
    private InboxDto inboxDto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            inboxDto = (InboxDto) bundle.getSerializable("inboxDto");

        title[POSITION_OF_IMAGE] = getString(R.string.message_type_image);
        title[POSITION_OF_FILE] = getString(R.string.message_type_file);
        title[POSITION_OF_LINK] = getString(R.string.message_type_link);

        if (mediaImageVideoFragment == null)
            mediaImageVideoFragment = MediaImageVideoFragment.newInstance(null, null, inboxDto);
        if (mediaFileFragment == null)
            mediaFileFragment = MediaFileFragment.newInstance(null, null, inboxDto);
        if (mediaLinkFragment == null)
            mediaLinkFragment = MediaLinkFragment.newInstance(null, null, inboxDto);

        Gson gson = new Gson();
        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        SharedPreferences sharedPreferencesUser = getSharedPreferences("user", Context.MODE_PRIVATE);
        currentUser = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        Toolbar toolbar_media_activity = findViewById(R.id.toolbar_media_activity);
        toolbar_media_activity.setTitleTextColor(Color.WHITE);
        toolbar_media_activity.setSubtitleTextColor(Color.WHITE);
        toolbar_media_activity.setTitle(getString(R.string.stored_media));
        setSupportActionBar(toolbar_media_activity);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TabLayout tabLayout_media_activity = findViewById(R.id.tab_layout_media_activity);
        ViewPager2 view_paper_media_activity = findViewById(R.id.view_paper_media_activity);
        view_paper_media_activity.setAdapter(new ScreenSlidePagerAdapter(this));
        view_paper_media_activity.setPageTransformer(new ZoomOutPageTransformer());
        view_paper_media_activity.setOffscreenPageLimit(NUM_PAGES);

        tabLayout_media_activity.addTab(tabLayout_media_activity.newTab().setText(getString(R.string.message_type_image)));
        tabLayout_media_activity.addTab(tabLayout_media_activity.newTab().setText(getString(R.string.message_type_file)));
        tabLayout_media_activity.addTab(tabLayout_media_activity.newTab().setText(getString(R.string.message_type_link)));
        tabLayout_media_activity.setSelectedTabIndicatorColor(getResources().getColor(R.color.purple_200));

        new TabLayoutMediator(tabLayout_media_activity, view_paper_media_activity, (tab, position) -> {
            if (counts[position] == 0)
                tab.setText(title[position]);
            else
                tab.setText(String.format("%s(%d)", title[position], counts[position]));
        }).attach();

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return mediaImageVideoFragment;
            }
            if (position == 1) {
                return mediaFileFragment;
            }
            return mediaLinkFragment;
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

}
