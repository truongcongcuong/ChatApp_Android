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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.ui.ViewImageActivity;
import com.example.chatapp.ui.ViewVideoActivity;
import com.example.chatapp.utils.FileUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LineItemPictureBeforeSendAdapter extends RecyclerView.Adapter<LineItemPictureBeforeSendAdapter.ViewHolder> {
    private List<File> files;
    private final Context context;

    public LineItemPictureBeforeSendAdapter(Context context, List<File> files) {
        this.context = context;
        if (files == null)
            this.files = new ArrayList<>(0);
        else
            this.files = files;
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

            Glide.with(context)
                    .load(f)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
                    .into(holder.item_picture_before_send_img_content);

            holder.item_picture_before_send_img_delete.setOnClickListener(v -> {
                int index = files.indexOf(f);
                files.remove(f);
                notifyItemRemoved(index);
            });

            /*
            click vào ảnh để xem
             */
            if (FileUtil.getMessageType(f).equals(MediaType.IMAGE)) {
                holder.item_picture_before_send_img_content.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ViewImageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("activityTitle", context.getString(R.string.view_image_before_send));
                    bundle.putString("activitySubTitle", FileUtils.byteCountToDisplaySize(f.length()));
                    bundle.putString("imageUrl", f.getAbsolutePath());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                });
            } else if (FileUtil.getMessageType(f).equals(MediaType.VIDEO)) {
                holder.item_picture_before_send_icon_center.setVisibility(View.VISIBLE);
                holder.item_picture_before_send_icon_center.setImageResource(R.drawable.ic_play_video_24);
                holder.item_picture_before_send_img_content.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ViewVideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("activityTitle", context.getString(R.string.view_video_before_send));
                    bundle.putString("activitySubTitle", FileUtils.byteCountToDisplaySize(f.length()));
                    bundle.putString("videoUrl", f.getAbsolutePath());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                });
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

}
