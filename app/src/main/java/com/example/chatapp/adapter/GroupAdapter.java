package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.ui.ChatActivity;
import com.example.chatapp.utils.TimeAgo;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private final Context context;
    private List<InboxDto> list;

    public GroupAdapter(Context context, List<InboxDto> dtos) {
        this.context = context;
        this.list = dtos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_message, parent, false);
        return new ViewHolder(view, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        InboxDto inboxDto = list.get(position);
        String url;
        String displayName;
        if (inboxDto.getRoom().getType().equalsIgnoreCase("GROUP")) {
            displayName = inboxDto.getRoom().getName();
            url = inboxDto.getRoom().getImageUrl();
        } else {
            displayName = inboxDto.getRoom().getTo().getDisplayName();
            url = inboxDto.getRoom().getTo().getImageUrl();
        }
        // display image
        Glide.with(context).load(url)
                .placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(holder.img_lim_avt);

        MessageDto lastMessage = inboxDto.getLastMessage();
        if (lastMessage != null) {
            holder.txt_lim_last_message.setText(lastMessage.getContent());
            holder.txt_lim_time_last_message.setText(TimeAgo.getTime(lastMessage.getCreateAt()));
            if (inboxDto.getRoom().getType().equalsIgnoreCase("GROUP")) {
                String content = lastMessage.getSender().getDisplayName() + ": " + lastMessage.getContent();
                holder.txt_lim_last_message.setText(content);
            } else
                holder.txt_lim_last_message.setText(lastMessage.getContent());
        }
        holder.txt_lim_display_name.setText(displayName);
        inboxDto.setCountNewMessage(6L);
        if (inboxDto.getCountNewMessage() != null && inboxDto.getCountNewMessage() > 0) {
            holder.txt_lim_unread_message.setPadding(18, 7, 18, 7);
            holder.txt_lim_unread_message.setBackgroundResource(R.drawable.background_unreadmessage);
            holder.txt_lim_unread_message.setText(inboxDto.getCountNewMessage() < 5
                    ? inboxDto.getCountNewMessage().toString() : "5+");
            holder.txt_lim_last_message.setTypeface(null, Typeface.BOLD);
            holder.txt_lim_time_last_message.setTypeface(null, Typeface.BOLD);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("dto", inboxDto);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        GroupAdapter adapter;
        ImageView img_lim_avt;
        TextView txt_lim_display_name;
        TextView txt_lim_last_message;
        TextView txt_lim_time_last_message;
        TextView txt_lim_unread_message;

        public ViewHolder(@NonNull View itemView, GroupAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            txt_lim_last_message = itemView.findViewById(R.id.txt_lim_last_message);
            img_lim_avt = itemView.findViewById(R.id.img_lim_avt);
            txt_lim_display_name = itemView.findViewById(R.id.txt_lim_display_name);
            txt_lim_time_last_message = itemView.findViewById(R.id.txt_lim_time_last_message);
            txt_lim_unread_message = itemView.findViewById(R.id.txt_lim_unread_message);
        }
    }

}
