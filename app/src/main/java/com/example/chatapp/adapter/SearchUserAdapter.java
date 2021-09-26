package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.ChatActivity;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private final Context context;
    private List<UserProfileDto> list;
    private final Gson gson;
    private final UserSummaryDTO user;
    private final String token;

    public SearchUserAdapter(Context context, List<UserProfileDto> list) {
        this.context = context;
        if (list == null)
            this.list = new ArrayList<>(0);
        else
            this.list = list;
        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        user = gson.fromJson(userJson, UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
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

            holder.itemView.setOnClickListener(v -> {
                StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "/with/" + user.getId(),
                        response -> {
                            try {
                                String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                JSONObject object = new JSONObject(res);
                                InboxDto dto = gson.fromJson(object.toString(), InboxDto.class);

                                Intent intent = new Intent(context, ChatActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("dto", dto);
                                intent.putExtras(bundle);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            } catch (JSONException | UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.i("", error.toString())) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + token);
                        return map;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(context);
                requestQueue.add(request);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_search_user_avt;
        TextView txt_search_user_display_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_search_user_avt = itemView.findViewById(R.id.img_search_user_avt);
            txt_search_user_display_name = itemView.findViewById(R.id.txt_search_user_display_name);
        }
    }

}
