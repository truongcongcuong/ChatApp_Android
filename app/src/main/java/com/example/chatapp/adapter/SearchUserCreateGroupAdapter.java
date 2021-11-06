package com.example.chatapp.adapter;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.SendDataCreateRoomActivity;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.utils.TimeAgo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class SearchUserCreateGroupAdapter extends RecyclerView.Adapter<SearchUserCreateGroupAdapter.ViewHolder> {
    private List<UserProfileDto> list;
    private List<UserProfileDto> selected;
    private final Context context;
    private final SendDataCreateRoomActivity sendData;

    public SearchUserCreateGroupAdapter(Context context, List<UserProfileDto> list, List<UserProfileDto> selected) {
        this.context = context;
        if (list == null)
            this.list = new ArrayList<>(0);
        else
            this.list = list;
        if (selected == null)
            this.selected = new ArrayList<>(0);
        else
            this.selected = selected;
//        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
//        String userJson = sharedPreferencesUser.getString("user-info", null);

//        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
//        String token = sharedPreferencesToken.getString("access-token", null);

//        Gson gson = new Gson();
//        UserSummaryDTO user = gson.fromJson(userJson, UserSummaryDTO.class);
        sendData = (SendDataCreateRoomActivity) context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_user_create_group, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (list != null && position < list.size()) {
            /*
            xóa tất cả checkbox về không chọn
             */
            holder.item_create_group_checkbox.setChecked(false);

            UserProfileDto user = list.get(position);
            for (UserProfileDto u : selected) {
                if (u.getId().equals(user.getId())) {
                    /*
                    nếu user nào đã được chọn trước đó trong mảng selected thì set true cho checkbox
                     */
                    holder.item_create_group_checkbox.setChecked(true);
                }
            }

            Glide.with(context).load(user.getImageUrl())
                    .placeholder(R.drawable.image_placeholer)
                    .centerCrop().circleCrop()
                    .into(holder.item_create_group_user_img);
            holder.item_create_group_user_name.setText(user.getDisplayName());
            try {
                holder.item_create_group_user_detail.setText(String.format("%s: %s", context.getString(R.string.online), TimeAgo.getTime(user.getLastOnline())));
            } catch (ParseException e) {
                holder.item_create_group_user_detail.setText("");
            }

            /*
            sự kiện click trên item
             */
            holder.itemView.setOnClickListener(v -> {
                if (!holder.item_create_group_checkbox.isChecked()) {
                    if (!selected.contains(user)) {
                        selected.add(user);
                        sendData.addUserToGroup(user);
                        holder.item_create_group_checkbox.setChecked(true);
                    }
                } else {
                    selected.removeIf(x -> x.getId().equals(user.getId()));
                    sendData.deleteUser(user.getId());
                    holder.item_create_group_checkbox.setChecked(false);
                }
            });

            /*
            sự kiện click trên checkbox
             */
            holder.item_create_group_checkbox.setOnClickListener(v -> {
                if (holder.item_create_group_checkbox.isChecked()) {
                    if (!selected.contains(user)) {
                        selected.add(user);
                        sendData.addUserToGroup(user);
                        holder.item_create_group_checkbox.setChecked(true);
                    }
                } else {
                    selected.removeIf(x -> x.getId().equals(user.getId()));
                    sendData.deleteUser(user.getId());
                    holder.item_create_group_checkbox.setChecked(false);
                }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void uncheckForUser(String idToDelete) {
        /*
        xóa người đã chọn từ CreateGroup activity
         */
        selected.removeIf(x -> x.getId().equals(idToDelete));
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_create_group_user_img;
        TextView item_create_group_user_name;
        TextView item_create_group_user_detail;
        CheckBox item_create_group_checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_create_group_user_img = itemView.findViewById(R.id.item_create_group_user_img);
            item_create_group_user_name = itemView.findViewById(R.id.item_create_group_user_name);
            item_create_group_user_detail = itemView.findViewById(R.id.item_create_group_user_detail);
            item_create_group_checkbox = itemView.findViewById(R.id.item_create_group_checkbox);
        }
    }

}
