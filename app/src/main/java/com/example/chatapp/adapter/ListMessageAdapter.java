package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
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
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.ChatActivity;
import com.example.chatapp.utils.TimeAgo;
import com.google.gson.Gson;

import java.util.List;

public class ListMessageAdapter extends RecyclerView.Adapter<ListMessageAdapter.ViewHolder> {
    private final Context context;
    private List<InboxDto> list;
    private final int maxMessageSizeDisplay = 5;
    private final Gson gson;
    private final UserSummaryDTO user;

    public ListMessageAdapter(Context context, List<InboxDto> dtos) {
        this.context = context;
        this.list = dtos;
        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        user = gson.fromJson(userJson, UserSummaryDTO.class);
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

        // load image
        Glide.with(context).load(url).placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(holder.img_lim_avt);

        MessageDto lastMessage = inboxDto.getLastMessage();
        if (lastMessage != null) {
            holder.txt_lim_last_message.setText(lastMessage.getContent());
            holder.txt_lim_time_last_message.setText(TimeAgo.getTime(lastMessage.getCreateAt()));
            String content;
            if (inboxDto.getRoom().getType().equalsIgnoreCase("GROUP")) {
                content = lastMessage.getSender().getDisplayName() + ": " + lastMessage.getContent();
            } else {
                content = lastMessage.getContent();
            }
            if (user.getId().equals(lastMessage.getSender().getId()))
                content = "Bạn: " + lastMessage.getContent();
            holder.txt_lim_last_message.setText(content);
        }
        holder.txt_lim_display_name.setText(displayName);
//        inboxDto.setCountNewMessage(6L);
        /*
        số tin nhắn chưa đọc lớn hơn 0
         */
        if (inboxDto.getCountNewMessage() != null && inboxDto.getCountNewMessage() > 0) {
            holder.txt_lim_unread_message.setPadding(18, 7, 18, 7);
            holder.txt_lim_unread_message.setBackgroundResource(R.drawable.background_unreadmessage);
            if (inboxDto.getCountNewMessage() <= maxMessageSizeDisplay)
                holder.txt_lim_unread_message.setText(inboxDto.getCountNewMessage().toString());
            else
                holder.txt_lim_unread_message.setText(maxMessageSizeDisplay + "+");
            holder.txt_lim_last_message.setTypeface(null, Typeface.BOLD);
            holder.txt_lim_time_last_message.setTypeface(null, Typeface.BOLD);
            holder.txt_lim_display_name.setTypeface(null, Typeface.BOLD);
        } else {
            /*
            xóa text về rỗng khi đã đọc tin nhắn
             */
            holder.txt_lim_unread_message.setPadding(0, 0, 0, 0);
            holder.txt_lim_unread_message.setText("");
            holder.txt_lim_display_name.setTypeface(null, Typeface.NORMAL);
            holder.txt_lim_last_message.setTypeface(null, Typeface.NORMAL);
            holder.txt_lim_time_last_message.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            /*
            khi click vào inbox để xem tin nhắn thì set số tin nhắn mới về 0
             */
            inboxDto.setCountNewMessage(0L);
            list.set(position, inboxDto);
            Log.d("counttt", list.get(position).getCountNewMessage() + "");
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
        ListMessageAdapter adapter;
        ImageView img_lim_avt;
        TextView txt_lim_display_name;
        TextView txt_lim_last_message;
        TextView txt_lim_time_last_message;
        TextView txt_lim_unread_message;

        public ViewHolder(@NonNull View itemView, ListMessageAdapter adapter) {
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
