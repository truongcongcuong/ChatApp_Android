package com.example.chatapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ReadbyAdapter extends RecyclerView.Adapter<ReadbyAdapter.ViewHolder> {

    private List<ReadByDto> list;
    private final Context context;
    private MessageDto messageDto;
    private final int max = 10;
    private final UserSummaryDTO user;
    private final Gson gson;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ReadbyAdapter(MessageDto messageDto, Context context) {
        this.messageDto = messageDto;
        if (messageDto != null)
            this.list = new ArrayList<>(messageDto.getReadbyes());
        else
            this.list = new ArrayList<>();
        this.context = context;
        gson = new Gson();
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesToken.getString("user-info", null), UserSummaryDTO.class);
        /*
        xóa reaction của người dùng hiện tại, không hiện lên
         */
//        list.removeIf(x -> x.getReadByUser().getId().equals(user.getId()));
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
        holder.setIsRecyclable(false);

        if (!list.isEmpty() && position < list.size()) {
            ReadByDto readBy = list.get(position);

            // hiện ảnh của người xem dùng thư viện Glide
            if (position < max - 1) {
                holder.readby_image.setBackgroundResource(R.drawable.border_for_circle_image);
                Glide.with(context).load(readBy.getReadByUser().getImageUrl())
                        .placeholder(R.drawable.image_placeholer)
                        .centerCrop().circleCrop().into(holder.readby_image);
            }

            if (position == max - 1) {
                int remain = list.size() - max;
                holder.readby_image.setBackgroundResource(R.drawable.border_for_circle_image);
                Glide.with(context).load(readBy.getReadByUser().getImageUrl())
                        .placeholder(R.drawable.image_placeholer)
                        .centerCrop().circleCrop().into(holder.readby_image);
                if (remain > 0) {
                    holder.readby_image.setAlpha(0.3f);
                    holder.readby_more.setText(String.format("+%d", remain));
                }
            }

            // click vào list readby, sẽ hiện popup danh sách những người đã xem tin nhắn này và thời gian xem
            holder.itemView.setOnClickListener(v -> {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.readby_dialog);

                ListView listView = dialog.findViewById(R.id.lv_readby_dialog);
                TextView titleOfDialog = dialog.findViewById(R.id.txt_readby_dialog_title);

                ReadbyDialogAdapter arrayAdapter = new ReadbyDialogAdapter(context, R.layout.readby_dialog_line_item, list);
                listView.setAdapter(arrayAdapter);
                listView.setOnItemClickListener((parent, view, pos, itemId) -> {

                });
                titleOfDialog.setText("Những người đã xem");
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_corner_white);
                dialog.show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(list.size(), max);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView readby_image;
        TextView readby_more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            readby_more = itemView.findViewById(R.id.readby_more);
            readby_image = itemView.findViewById(R.id.readby_image);
        }
    }

}
