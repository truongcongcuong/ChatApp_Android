package com.example.chatapp.adapter;

import android.content.Context;
import android.os.Build;
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
import com.example.chatapp.dialog.ProfileDialog;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.utils.TimeAgo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private final Context context;
    private List<UserProfileDto> list;

    public SearchUserAdapter(Context context, List<UserProfileDto> list) {
        this.context = context;
        if (list == null)
            this.list = new ArrayList<>(0);
        else
            this.list = list;
//        Gson gson = new Gson();
//        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
//        String userJson = sharedPreferencesUser.getString("user-info", null);
//        UserSummaryDTO user = gson.fromJson(userJson, UserSummaryDTO.class);

//        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
//        String token = sharedPreferencesToken.getString("access-token", null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_search_user, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (list != null && position < list.size()) {
            UserProfileDto user = list.get(position);
            Glide.with(context).load(user.getImageUrl())
                    .placeholder(R.drawable.image_placeholer)
                    .centerCrop().circleCrop()
                    .into(holder.img_search_user_avt);
            holder.txt_search_user_display_name.setText(user.getDisplayName());
            try {
                holder.txt_search_user_detail.setText(String.format("%s: %s",
                        context.getString(R.string.online), TimeAgo.getTime(user.getLastOnline())));
            } catch (ParseException e) {
                holder.txt_search_user_detail.setText("");
            }

            holder.itemView.setOnClickListener(v -> {
                ProfileDialog profileDialog = new ProfileDialog(context, user, null);
                profileDialog.show();
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
        ImageView img_search_user_avt;
        TextView txt_search_user_display_name;
        TextView txt_search_user_detail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_search_user_avt = itemView.findViewById(R.id.img_search_user_avt);
            txt_search_user_display_name = itemView.findViewById(R.id.txt_search_user_display_name);
            txt_search_user_detail = itemView.findViewById(R.id.txt_search_user_detail);
        }
    }

}
