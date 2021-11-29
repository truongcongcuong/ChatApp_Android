package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.ui.ViewImageVideoActivity;
import com.example.chatapp.utils.FileUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineItemPictureBeforeSendAdapter extends RecyclerView.Adapter<LineItemPictureBeforeSendAdapter.ViewHolder> {
    private List<File> files;
    private final List<String> urls;
    private final Context context;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public LineItemPictureBeforeSendAdapter(Context context, List<File> files) {
        this.context = context;
        if (files == null)
            this.files = new ArrayList<>(0);
        else
            this.files = files;
        urls = files.stream()
                .filter(x -> !FileUtil.getMessageType(x).equals(MediaType.FILE))
                .map(File::getAbsolutePath).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_picture_before_send, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        holder.item_picture_before_send_img_delete.setOnClickListener(null);

        if (files != null && position < files.size()) {
            File f = files.get(position);

            holder.item_picture_before_send_img_delete.setOnClickListener(v -> {
                int index = files.indexOf(f);
                files.remove(f);
                notifyItemRemoved(index);
            });

            /*
            click vào ảnh để xem
             */
            if (FileUtil.getMessageType(f).equals(MediaType.IMAGE)) {
                Glide.with(context)
                        .load(f)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
                        .into(holder.item_picture_before_send_img_content);

                holder.item_picture_before_send_img_content.setOnClickListener(v -> {
                    view(position);
                });
            } else if (FileUtil.getMessageType(f).equals(MediaType.VIDEO)) {
                Glide.with(context)
                        .load(f)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
                        .into(holder.item_picture_before_send_img_content);

                holder.item_picture_before_send_icon_center.setVisibility(View.VISIBLE);
                holder.item_picture_before_send_icon_center.setImageResource(R.drawable.ic_play_video_24);

                holder.item_picture_before_send_img_content.setOnClickListener(v -> {
                    view(position);
                });
            } else if (FileUtil.getMessageType(f).equals(MediaType.FILE)) {
                holder.itemView.setOnClickListener(v -> {
                    Toast.makeText(context, f.getName() + " - " + FileUtils.byteCountToDisplaySize(f.length()), Toast.LENGTH_SHORT).show();
                });

                holder.item_picture_before_send_icon_center.setVisibility(View.VISIBLE);
                holder.item_picture_before_send_icon_center.setImageResource(R.drawable.ic_round_file_24);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (files == null)
            return 0;
        return files.size();
    }

    public void setList(List<File> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_picture_before_send_img_content;
        ImageView item_picture_before_send_img_delete;
        ImageView item_picture_before_send_icon_center;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_picture_before_send_img_content = itemView.findViewById(R.id.item_picture_before_send_img_content);
            item_picture_before_send_img_delete = itemView.findViewById(R.id.item_picture_before_send_img_delete);
            item_picture_before_send_icon_center = itemView.findViewById(R.id.item_picture_before_send_icon_center);
            item_picture_before_send_icon_center.setVisibility(View.GONE);
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

}
