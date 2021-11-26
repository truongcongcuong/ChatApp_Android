package com.example.chatapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.dialog.ViewAllReadTrackingDialog;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ReadbyAdapter extends RecyclerView.Adapter<ReadbyAdapter.ViewHolder> {

    private List<ReadByDto> list;
    private final Context context;
    private final int max = 10;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ReadbyAdapter(MessageDto messageDto, Context context) {
        if (messageDto != null)
            this.list = new ArrayList<>(messageDto.getReadbyes());
        else
            this.list = new ArrayList<>();
        this.context = context;
        Gson gson = new Gson();
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        UserSummaryDTO user = gson.fromJson(sharedPreferencesToken.getString("user-info", null), UserSummaryDTO.class);
        /*
        xóa reaction của người dùng hiện tại, không hiện lên
         */
        list.removeIf(x -> x.getReadByUser().getId().equals(user.getId()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_readby, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        holder.setIsRecyclable(false);

        if (!list.isEmpty() && position < list.size()) {
            ReadByDto readBy = list.get(position);

            // hiện ảnh của người xem dùng thư viện Glide
            if (position < max) {
                holder.readby_image.setVisibility(View.VISIBLE);
                holder.readby_image.setBackgroundResource(R.drawable.border_for_circle_image);
                Glide.with(context).load(readBy.getReadByUser().getImageUrl())
                        .placeholder(R.drawable.img_avatar_placeholer)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop().circleCrop().into(holder.readby_image);
                RecyclerView.LayoutParams back_ground_read_moreLayoutParams = (RecyclerView.LayoutParams) holder.back_ground_read_more.getLayoutParams();
                back_ground_read_moreLayoutParams.rightMargin = (int) convertDpToPixel(0, context);
                holder.readby_more.setVisibility(View.GONE);
            }

            if (position == max - 1) {
                int remain = list.size() - max + 1;
                if (remain > 0) {
                    holder.readby_more.setVisibility(View.VISIBLE);
                    holder.readby_image.setVisibility(View.GONE);
                    RecyclerView.LayoutParams back_ground_read_moreLayoutParams = (RecyclerView.LayoutParams) holder.back_ground_read_more.getLayoutParams();
                    back_ground_read_moreLayoutParams.rightMargin = (int) convertDpToPixel(8, context);
                    holder.readby_more.setBackgroundResource(R.drawable.background_read_more);
                    holder.readby_more.setText(String.format("+%d", remain));
                } else {
                    holder.readby_image.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutParams back_ground_read_moreLayoutParams = (RecyclerView.LayoutParams) holder.back_ground_read_more.getLayoutParams();
                    back_ground_read_moreLayoutParams.rightMargin = (int) convertDpToPixel(0, context);
                    holder.readby_more.setVisibility(View.GONE);
                }
            }

            // click vào list readby, sẽ hiện popup danh sách những người đã xem tin nhắn này và thời gian xem
            holder.itemView.setOnClickListener(v -> {
                ViewAllReadTrackingDialog viewAllReactionDialog = new ViewAllReadTrackingDialog(context, list);
                viewAllReactionDialog.show();
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
        ConstraintLayout back_ground_read_more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            readby_more = itemView.findViewById(R.id.readby_more);
            readby_image = itemView.findViewById(R.id.readby_image);
            back_ground_read_more = itemView.findViewById(R.id.back_ground_read_more);
        }
    }

    public float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}
