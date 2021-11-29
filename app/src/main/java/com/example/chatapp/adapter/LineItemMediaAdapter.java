package com.example.chatapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.dialog.MessageOptionDialog;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MyMedia;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.ui.ViewImageVideoActivity;

import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineItemMediaAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final MessageDto messageDto;
    private List<String> urls;
    public final int COLUMNS;
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    public static final int FILE = 3;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LineItemMediaAdapter(Context context, MessageDto messageDto, int numColumn) {
        this.context = context;
        this.messageDto = messageDto;
        if (messageDto != null && messageDto.getMedia() != null) {
            urls = messageDto.getMedia().stream()
                    .filter(x -> !x.getType().equals(MediaType.FILE))
                    .map(MyMedia::getUrl).collect(Collectors.toList());
        }
        this.COLUMNS = numColumn;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.line_item_message_media_image, parent, false);
            return new ImageViewHolder(view);
        }
        if (viewType == VIDEO) {
            View view = LayoutInflater.from(context).inflate(R.layout.line_item_message_media_video, parent, false);
            return new VideoViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_message_media_file, parent, false);
        return new FileViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (messageDto != null) {
            List<MyMedia> mediaList = messageDto.getMedia();
            if (mediaList != null && position < mediaList.size()) {
                MyMedia media = mediaList.get(position);
                if (media.getType().equals(MediaType.IMAGE)) {
                    ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                    imageViewHolder.itemView.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                    imageViewHolder.line_item_message_media_image.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));

                    imageViewHolder.line_item_message_media_image.setOnClickListener(v -> {
                        view(position);
                    });
                    imageViewHolder.line_item_message_media_image.setMaxHeight((int) (getScreenHeight(context) * 0.45));

                    Glide.with(context).load(media.getUrl())
                            .placeholder(R.drawable.background_preload_image_video)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewHolder.line_item_message_media_image);

                } else if (media.getType().equals(MediaType.VIDEO)) {
                    VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                    videoViewHolder.line_item_message_media_icon_center.setImageResource(R.drawable.ic_play_video_24);
                    videoViewHolder.itemView.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                    videoViewHolder.line_item_message_media_thumbnail.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));

                    videoViewHolder.line_item_message_media_icon_center.setOnClickListener(v -> {
                        view(position);
                    });

                    videoViewHolder.line_item_message_media_thumbnail.setOnClickListener(v -> {
                        view(position);
                    });
                    videoViewHolder.line_item_message_media_thumbnail.setMaxHeight((int) (getScreenHeight(context) * 0.45));

                    Glide.with(context).load(media.getUrl())
                            .placeholder(R.drawable.background_preload_image_video)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(videoViewHolder.line_item_message_media_thumbnail);
                } else if (media.getType().equals(MediaType.FILE)) {
                    holder.itemView.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                    FileViewHolder fileViewHolder = (FileViewHolder) holder;
                    fileViewHolder.line_item_message_media_file_left_drawable.setImageResource(R.drawable.ic_round_file_24);
                    fileViewHolder.line_item_message_media_file_right_drawable.setImageResource(R.drawable.ic_round_download_for_offline_24);
                    fileViewHolder.line_item_message_media_file_name.setText(media.getName());
                    fileViewHolder.line_item_message_media_file_size.setText(FileUtils.byteCountToDisplaySize(media.getSize()));

                    fileViewHolder.line_item_message_media_file_right_drawable.setOnClickListener(view -> {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(media.getUrl()));
                        context.startActivity(i);
                    });
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean showReactionCreateDialog(MessageDto messageDto) {
        Log.d("message of user id", messageDto.getSender().getId());
        MessageOptionDialog messageOptionDialog = new MessageOptionDialog(context, messageDto);
        messageOptionDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), messageOptionDialog.getTag());
        return true;
    }

    @Override
    public int getItemCount() {
        if (messageDto == null)
            return 0;
        if (messageDto.getMedia() == null)
            return 0;
        return messageDto.getMedia().size();
    }

    @Override
    public int getItemViewType(int position) {
        List<MyMedia> mediaList = messageDto.getMedia();
        if (mediaList != null && position < mediaList.size()) {
            MyMedia media = mediaList.get(position);
            if (media.getType().equals(MediaType.IMAGE))
                return IMAGE;
            if (media.getType().equals(MediaType.VIDEO))
                return VIDEO;
            if (media.getType().equals(MediaType.FILE))
                return FILE;
        }
        return super.getItemViewType(position);
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView line_item_message_media_thumbnail;
        ImageView line_item_message_media_icon_center;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            line_item_message_media_thumbnail = itemView.findViewById(R.id.line_item_message_media_thumbnail);
            line_item_message_media_icon_center = itemView.findViewById(R.id.line_item_message_media_icon_center);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView line_item_message_media_image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            line_item_message_media_image = itemView.findViewById(R.id.line_item_message_media_image);
        }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView line_item_message_media_file_left_drawable;
        ImageView line_item_message_media_file_right_drawable;
        TextView line_item_message_media_file_name;
        TextView line_item_message_media_file_size;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            line_item_message_media_file_left_drawable = itemView.findViewById(R.id.line_item_message_media_file_left_drawable);
            line_item_message_media_file_right_drawable = itemView.findViewById(R.id.line_item_message_media_file_right_drawable);
            line_item_message_media_file_name = itemView.findViewById(R.id.line_item_message_media_file_name);
            line_item_message_media_file_size = itemView.findViewById(R.id.line_item_message_media_file_size);
        }
    }

    private double getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private void view(int position) {
        Intent intent = new Intent(context, ViewImageVideoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("urls", (ArrayList<String>) urls);
        bundle.putInt("index", position);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}
