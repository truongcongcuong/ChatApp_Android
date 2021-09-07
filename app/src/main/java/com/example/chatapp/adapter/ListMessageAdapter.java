package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.cons.CroppedDrawable;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.ui.ChatActivity;
import com.example.chatapp.utils.TimeAgo;
import com.google.gson.Gson;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListMessageAdapter extends RecyclerView.Adapter<ListMessageAdapter.ViewHolder> {
    private Context context;
    private List<InboxDto> list = new ArrayList<>();
    private Gson gson = new Gson();


    public ListMessageAdapter(Context context, List<InboxDto> dtos) {
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
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
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

        try {
            URL urlOnl = new URL(url);
            Bitmap bitmap = BitmapFactory.decodeStream(urlOnl.openConnection().getInputStream());
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            CroppedDrawable cd = new CroppedDrawable(bitmap);
            holder.img_lim_avt.setImageDrawable(cd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.txt_lim_display_name.setText(displayName);
        holder.txt_lim_last_message.setText(inboxDto.getLastMessage().getContent());
        holder.txt_lim_time_last_message.setText(TimeAgo.get(inboxDto.getLastMessage().getCreateAt()));
//        inboxDto.setCountNewMessage(6L);
        if (inboxDto.getCountNewMessage() != null && inboxDto.getCountNewMessage() > 0) {
            holder.txt_lim_unread_message.setPadding(15, 5, 15, 5);
            holder.txt_lim_unread_message.setBackgroundResource(R.drawable.background_unreadmessage);
            holder.txt_lim_unread_message.setText(inboxDto.getCountNewMessage() < 5
                    ? inboxDto.getCountNewMessage().toString() : inboxDto.getCountNewMessage().toString() + "+");
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListMessageAdapter adapter;
        ImageView img_lim_avt;
        TextView txt_lim_display_name, txt_lim_last_message, txt_lim_time_last_message, txt_lim_unread_message;


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
