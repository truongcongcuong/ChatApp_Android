package com.example.chatapp.ui.signup;

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
import com.example.chatapp.ui.signup.frag.SignupEmailFragment1;
import com.example.chatapp.ui.signup.frag.SignupPhoneFragment1;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;


public class SignUpActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 2;
    private SignupEmailFragment1 emailFragment1;
    private SignupPhoneFragment1 phoneFragment1;
    private String[] title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .edge(true)
                .edgeSize(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_signup_activity);
        toolbar.setTitle(getString(R.string.create_account12));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TabLayout tableLayout = findViewById(R.id.tab_layout_signup_activity);
        ViewPager2 viewPager = findViewById(R.id.view_paper_signup_activity);

        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        title = new String[NUM_PAGES];
        title[0] = getString(R.string.create_account_by_phone);
        title[1] = getString(R.string.create_account_by_email);

        new TabLayoutMediator(tableLayout, viewPager, (tab, position) -> {
            tab.setText(String.format("%s", title[position]));
        }).attach();

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

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                if (phoneFragment1 == null)
                    phoneFragment1 = SignupPhoneFragment1.newInstance(null, null);
                return phoneFragment1;
            }
            if (emailFragment1 == null)
                emailFragment1 = SignupEmailFragment1.newInstance(null, null);
            return emailFragment1;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

}