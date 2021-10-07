package com.example.chatapp.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.utils.TimeAgo;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

public class ViewImageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageView;
    private MessageDto messageDto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

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
            messageDto = (MessageDto) bundle.getSerializable("message");

        toolbar = findViewById(R.id.toolbar_view_image_activity);
        imageView = findViewById(R.id.img_content_view_image_activity);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (messageDto != null) {
            if (messageDto.getSender() != null) {
                toolbar.setTitle(messageDto.getSender().getDisplayName());
                setTitle(messageDto.getSender().getDisplayName());
            }
            toolbar.setSubtitle(TimeAgo.getTime(messageDto.getCreateAt()));
            Glide.with(this)
                    .load(messageDto.getContent())
                    .into(imageView);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    /*
    tạo menu trên thanh toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_image_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.download_image_view_image_activity);

        menuItem.setOnMenuItemClickListener(item -> {
            Toast.makeText(this, "down load image", Toast.LENGTH_SHORT).show();
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }
}