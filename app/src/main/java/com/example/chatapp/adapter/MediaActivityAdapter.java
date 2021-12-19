package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MyMedia;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.ui.ViewImageVideoActivity;
import com.example.chatapp.utils.MyAutoLink;
import com.example.chatapp.utils.TimeAgo;

import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

public class MediaActivityAdapter extends RecyclerView.Adapter {

    private List<MessageDto> list;
    private Context context;
    private String token;
    private final int VIDEO = 1;
    private final int IMAGE = 2;
    private final int FILE = 3;
    private final int LINK = 4;
    private List<String> urls;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MediaActivityAdapter(Context context, List<MessageDto> list, String token) {
        if (list == null)
            this.list = new ArrayList<>();
        else
            this.list = list;
        this.context = context;
        this.token = token;
        urls = getListUrlFromListMessage(this.list);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<String> getListUrlFromListMessage(List<MessageDto> list) {
        List<String> newUrl = new ArrayList<>(0);
        if (list != null) {
            for (MessageDto m : list) {
                List<String> collect = m.getMedia().stream()
                        .filter(x -> !x.getType().equals(MediaType.FILE))
                        .map(MyMedia::getUrl).collect(Collectors.toList());
                newUrl.addAll(collect);
            }
        }
        return newUrl;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIDEO) {
            View view = LayoutInflater.from(context).inflate(R.layout.line_item_media_video_in_media_activity, parent, false);
            return new MediaActivityAdapter.VideoViewHolder(view);
        }
        if (viewType == IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.line_item_media_image_in_media_activity, parent, false);
            return new MediaActivityAdapter.ImageViewHolder(view);
        }
        if (viewType == FILE) {
            View view = LayoutInflater.from(context).inflate(R.layout.line_item_media_file_in_media_activity, parent, false);
            return new MediaActivityAdapter.FileViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_media_link_in_media_activity, parent, false);
        return new MediaActivityAdapter.LinkViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < list.size()) {
            MessageDto messageDto = list.get(position);
            List<MyMedia> mediaList = messageDto.getMedia();
            if (mediaList != null && !mediaList.isEmpty()) {
                MyMedia media = mediaList.get(0);
                if (media.getType().equals(MediaType.VIDEO))
                    return VIDEO;
                if (media.getType().equals(MediaType.IMAGE))
                    return IMAGE;
                if (media.getType().equals(MediaType.FILE))
                    return FILE;
            }
            if (MyAutoLink.isContainsLink(messageDto.getContent()))
                return LINK;
        }
        return super.getItemViewType(position);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SneakyThrows
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        MessageDto messageDto = list.get(position);
        if (messageDto != null) {
            if (getItemViewType(position) == IMAGE) {
                ImageViewHolder imageViewHolder = (ImageViewHolder) holder;

                Glide.with(context).load(messageDto.getMedia().get(0).getUrl())
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewHolder.line_item_media_image);
                imageViewHolder.line_item_media_image.setOnClickListener(v -> view(position));

            } else if (getItemViewType(position) == VIDEO) {
                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                videoViewHolder.line_item_media_video_icon_center.setBackgroundResource(R.drawable.ic_play_video_24);

                Glide.with(context).load(messageDto.getMedia().get(0).getUrl())
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(videoViewHolder.line_item_media_video_thumbnail);

                videoViewHolder.line_item_media_video_thumbnail.setOnClickListener(v -> view(position));
                videoViewHolder.line_item_media_video_icon_center.setOnClickListener(v -> view(position));


            } else if (getItemViewType(position) == FILE) {
                FileViewHolder fileViewHolder = (FileViewHolder) holder;
                fileViewHolder.line_item_media_file_left_drawable.setImageResource(R.drawable.ic_round_file_24);
                fileViewHolder.line_item_media_file_right_drawable.setImageResource(R.drawable.ic_round_download_for_offline_24_media);
                fileViewHolder.line_item_media_file_name.setText(messageDto.getMedia().get(0).getName());
                fileViewHolder.line_item_media_file_size.setText(FileUtils.byteCountToDisplaySize(messageDto.getMedia().get(0).getSize()));

                fileViewHolder.line_item_media_file_right_drawable.setOnClickListener(view -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(messageDto.getMedia().get(0).getUrl()));
                    context.startActivity(i);
                });
            } else if (getItemViewType(position) == LINK) {
                LinkViewHolder linkViewHolder = (LinkViewHolder) holder;
                linkViewHolder.line_item_media_link.setText(Html.fromHtml(messageDto.getContent()));
                linkViewHolder.line_item_media_link_detail.setText(TimeAgo.getTime(messageDto.getCreateAt(), context));

                linkViewHolder.itemView.setOnClickListener(view -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(messageDto.getContent()));
                    context.startActivity(i);
                });
            }
        }
    }

    private void view(int position) {
        Intent intent = new Intent(context, ViewImageVideoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("urls", (ArrayList<String>) urls);
        bundle.putInt("index", position);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setList(List<MessageDto> list) {
        this.list = list;
        urls = getListUrlFromListMessage(this.list);
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateList(List<MessageDto> list) {
        if (this.list == null)
            this.list = new ArrayList<>();
        int from = this.list.size();
        this.list.addAll(list);
        urls = getListUrlFromListMessage(this.list);
        notifyItemRangeInserted(from, list.size());
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView line_item_media_video_thumbnail;
        ImageView line_item_media_video_icon_center;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            line_item_media_video_thumbnail = itemView.findViewById(R.id.line_item_media_video_thumbnail);
            line_item_media_video_icon_center = itemView.findViewById(R.id.line_item_media_video_icon_center);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView line_item_media_image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            line_item_media_image = itemView.findViewById(R.id.line_item_media_image);
        }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView line_item_media_file_left_drawable;
        ImageView line_item_media_file_right_drawable;
        TextView line_item_media_file_name;
        TextView line_item_media_file_size;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            line_item_media_file_left_drawable = itemView.findViewById(R.id.line_item_media_file_left_drawable);
            line_item_media_file_right_drawable = itemView.findViewById(R.id.line_item_media_file_right_drawable);
            line_item_media_file_name = itemView.findViewById(R.id.line_item_media_file_name);
            line_item_media_file_size = itemView.findViewById(R.id.line_item_media_file_size);
        }
    }

    static class LinkViewHolder extends RecyclerView.ViewHolder {
        TextView line_item_media_link;
        TextView line_item_media_link_detail;

        public LinkViewHolder(@NonNull View itemView) {
            super(itemView);
            line_item_media_link = itemView.findViewById(R.id.line_item_media_link);
            line_item_media_link_detail = itemView.findViewById(R.id.line_item_media_link_detail);
        }
    }

}
