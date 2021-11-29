package com.example.chatapp.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatapp.R;
import com.example.chatapp.ui.main.frag.ImageVideoFragment;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;

public class ViewImageVideoActivity extends AppCompatActivity {

    private List<String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_video);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.VERTICAL)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            urls = bundle.getStringArrayList("urls");
            int index = bundle.getInt("index");

            ViewPager2 mViewPager = findViewById(R.id.view_paper_activity_view_image_video);
            mViewPager.setAdapter(new ScreenSlidePagerAdapter(this));
            mViewPager.setCurrentItem(index, false);
        }

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return ImageVideoFragment.newInstance(null, null, urls.get(position));
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }
    }

}