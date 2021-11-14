package com.example.chatapp.adapter;

import android.content.Context;
import android.os.Build;
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.cons.SendDataCreateRoomActivity;
import com.example.chatapp.dto.UserProfileDto;

import java.util.ArrayList;
import java.util.List;

public class SelectedUserCreateGroupAdapter extends RecyclerView.Adapter<SelectedUserCreateGroupAdapter.ViewHolder> {
    private List<UserProfileDto> list;
    private final Context context;
    private final SendDataCreateRoomActivity sendDataCreateRoomActivity;

    public SelectedUserCreateGroupAdapter(Context context, List<UserProfileDto> list) {
        this.context = context;
        if (list == null)
            this.list = new ArrayList<>(0);
        else
            this.list = list;
//        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
//        String userJson = sharedPreferencesUser.getString("user-info", null);

//        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
//        String token = sharedPreferencesToken.getString("access-token", null);

//        Gson gson = new Gson();
//        UserSummaryDTO user = gson.fromJson(userJson, UserSummaryDTO.class);
        sendDataCreateRoomActivity = (SendDataCreateRoomActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_user_create_group_selected, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (list != null && position < list.size()) {
            UserProfileDto profileDto = list.get(position);

            Glide.with(context)
                    .load(profileDto.getImageUrl())
                    .centerCrop()
                    .circleCrop()
                    .placeholder(R.drawable.img_avatar_placeholer)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.item_create_group_selected_user_img);
            /*
            khi click icon xóa thì gửi thông điệp xóa đến createGroup activity
            và xóa trong list
             */
            holder.item_create_group_selected_delete.setOnClickListener(v -> {
                sendDataCreateRoomActivity.deleteUser(profileDto.getId());
                list.removeIf(x -> x.getId().equals(profileDto.getId()));
                notifyDataSetChanged();
            });

            /*
            khi click vào ảnh thì hiện lên tên
             */
            holder.item_create_group_selected_user_img.setOnClickListener(v -> {
                Toast.makeText(context, profileDto.getDisplayName(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    public void setList(List<UserProfileDto> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_create_group_selected_user_img;
        ImageView item_create_group_selected_delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_create_group_selected_user_img = itemView.findViewById(R.id.item_create_group_selected_user_img);
            item_create_group_selected_delete = itemView.findViewById(R.id.item_create_group_selected_delete);
        }
    }

}
