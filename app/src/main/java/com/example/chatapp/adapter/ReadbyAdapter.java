package com.example.chatapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.readby_dialog);
//            if (dialog.getWindow() != null) {
//                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // this is optional
//            }
            ListView listView = dialog.findViewById(R.id.lv_readby_dialog);
            TextView titleOfDialog = dialog.findViewById(R.id.txt_readby_dialog_title);

            ReadbyDialogAdapter arrayAdapter = new ReadbyDialogAdapter(context, R.layout.readby_dialog_line_item, messageDto.getReadbyes());
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener((adapterView, view, which, l) -> {
                Log.d("readby on cli", "showAssignmentsList: " + messageDto.getReadbyes().get(which).getReadByUser().getId());
            });
            titleOfDialog.setText("Danh sách người đã xem tin nhắn");
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_readby_dialog);
            dialog.show();
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
