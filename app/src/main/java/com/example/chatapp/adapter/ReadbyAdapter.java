package com.example.chatapp.adapter;

import android.content.Context;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReadByDto;

import java.util.ArrayList;
import java.util.List;

public class ReadbyAdapter extends RecyclerView.Adapter<ReadbyAdapter.ViewHolder> {

    private List<ReadByDto> list = new ArrayList<>();
    private final Context context;
    private MessageDto messageDto;
    private final int max = 1;

    public ReadbyAdapter(MessageDto messageDto, Context context) {
        this.messageDto = messageDto;
        if (messageDto != null)
            this.list = messageDto.getReadbyes();
        else
            this.list = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_readby, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ReadByDto readBy = list.get(position);

        // hiện ảnh của người xem dùng thư viện Glide
        Glide.with(context).load(readBy.getReadByUser().getImageUrl())
                .placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(holder.readby_image);

        if (position == list.size() - 1 && position > max) {
            holder.readby_image.setAlpha(0.3f);
            holder.readby_more.setText(String.format("%s%d", "+", (list.size() - max - 1)));
        }
        // click vào list readby, sẽ hiện popup danh sách những người đã xem tin nhắn này và thời gian xem
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, messageDto.getId(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size() - max + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView readby_image;
        TextView readby_more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            readby_more = itemView.findViewById(R.id.readby_more);
            readby_image = itemView.findViewById(R.id.readby_image);
        }
    }

}
