package com.example.chatapp.ui.main.frag;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.custom.MyVideoView;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.utils.FileUtil;

public class ImageVideoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private MyVideoView videoView;
    private String url;

    public ImageVideoFragment() {
    }

    public static ImageVideoFragment newInstance(String param1, String param2, String url) {
        ImageVideoFragment fragment = new ImageVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            url = getArguments().getString("url");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vide_image_video, container, false);
        ImageView imageView = view.findViewById(R.id.image_video_fragment_image);
        ProgressBar progressBar = view.findViewById(R.id.image_video_fragment_progress);
        videoView = view.findViewById(R.id.image_video_fragment_video);

        if (FileUtil.getMessageType(url).equals(MediaType.IMAGE)) {
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            Glide.with(getActivity()).load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        } else if (FileUtil.getMessageType(url).equals(MediaType.VIDEO)) {
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            MediaController mediaController = new MediaController(getActivity());

            Uri uri = Uri.parse(url);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                videoView.start();
                progressBar.setVisibility(View.GONE);
                mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
                    mediaController.setMediaPlayer(videoView);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                    mediaController.show();
                });
            });
        }
        return view;

    }
}