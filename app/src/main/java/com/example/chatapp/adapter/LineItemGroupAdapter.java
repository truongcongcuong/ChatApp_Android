package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.enumvalue.RoomType;
import com.example.chatapp.ui.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class LineItemGroupAdapter extends RecyclerView.Adapter<LineItemGroupAdapter.ViewHolder> {
    private final Context context;
    private List<InboxDto> list;
    private final Drawable backGround;
    private final int foreGround;

    public LineItemGroupAdapter(Context context, List<InboxDto> list, Drawable backGround, int foreGround) {
        this.context = context;
        if (list == null)
            this.list = new ArrayList<>(0);
        else
            this.list = list;
        this.backGround = backGround;
        this.foreGround = foreGround;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_group, parent, false);
        return new ViewHolder(view, backGround, foreGround);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (list != null && position < list.size()) {
            InboxDto inbox = list.get(position);
            if (inbox != null && inbox.getRoom().getType().equals(RoomType.GROUP)) {
                Glide.with(context).load(inbox.getRoom().getImageUrl())
                        .placeholder(R.drawable.image_placeholer)
                        .centerCrop().circleCrop()
                        .into(holder.line_item_group_image);
                holder.line_item_group_name.setText(inbox.getRoom().getName());
                int members = inbox.getRoom().getMembers() != null ? inbox.getRoom().getMembers().size() : 0;
                holder.txt_search_group_detail.setText(String.format("%s %s %s",
                        context.getString(R.string.title_group), members, context.getString(R.string.members)));

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("dto", inbox);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    public void setList(List<InboxDto> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView line_item_group_image;
        TextView line_item_group_name;
        TextView txt_search_group_detail;
        View line_item_group_background;

        public ViewHolder(@NonNull View itemView, Drawable backGround, int foreGround) {
            super(itemView);
            line_item_group_image = itemView.findViewById(R.id.line_item_group_img);
            line_item_group_name = itemView.findViewById(R.id.line_item_group_name);
            txt_search_group_detail = itemView.findViewById(R.id.line_item_group_detail);
            line_item_group_background = itemView.findViewById(R.id.line_item_group_background);

            line_item_group_background.setBackground(backGround);
            line_item_group_name.setTextColor(foreGround);
            txt_search_group_detail.setTextColor(foreGround);
        }
    }

}
