package com.example.chatapp.ui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.chatapp.R;
import com.example.chatapp.custom.MyVideoView;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.utils.TimeAgo;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

public class ViewVideoActivity extends AppCompatActivity {

    private MyVideoView videoView;
    private MessageDto messageDto;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            messageDto = (MessageDto) bundle.getSerializable("message");

        Toolbar toolbar = findViewById(R.id.toolbar_view_video_activity);
        videoView = findViewById(R.id.video_content_view_video_activity);
        progressBar = findViewById(R.id.progressbar_video_view_activity);
        progressBar.setVisibility(View.VISIBLE);

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

            MediaController mediaController = new MediaController(this);
            mediaController.setMediaPlayer(videoView);
            mediaController.setAnchorView(videoView);

            Uri uri = Uri.parse(messageDto.getContent());
            videoView.setVideoURI(uri);
            videoView.setMediaController(mediaController);
            videoView.requestFocus();
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                progressBar.setVisibility(View.GONE);
                mediaController.show();
                videoView.start();
            });
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
        getMenuInflater().inflate(R.menu.menu_view_video_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.download_video_view_video_activity);

        menuItem.setOnMenuItemClickListener(item -> {
            Toast.makeText(this, "down load video", Toast.LENGTH_SHORT).show();
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }

}