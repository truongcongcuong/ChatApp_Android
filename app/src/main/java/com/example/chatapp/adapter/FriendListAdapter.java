package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.FriendDTO;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.ui.ChatActivity;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<FriendDTO> list;
    private final Context context;
    private final Gson gson = new Gson();
    private final SharedPreferences sharedPreferencesToken;

    public FriendListAdapter(List<FriendDTO> list, Context context) {
        this.list = list;
        this.context = context;
        sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_list_adapter_layout, parent, false);
        return new ViewHolder(this, view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        FriendDTO friend = list.get(position);

        // load image
        Glide.with(context).load(friend.getFriend().getImageUrl())
                .placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(holder.img_list_contact_avt);

        holder.txt_list_contact_display_name.setText(friend.getFriend().getDisplayName());
        holder.txt_list_contact_create_at.setText("Bạn bè từ " + friend.getCreateAt().substring(0, 10));
        holder.itemView.setOnClickListener(v -> getInboxWith(friend.getFriend().getId()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FriendListAdapter friendListAdapter;
        ImageView img_list_contact_avt;
        TextView txt_list_contact_display_name;
        TextView txt_list_contact_create_at;

        public ViewHolder(FriendListAdapter friendListAdapter, @NonNull View itemView) {
            super(itemView);
            this.friendListAdapter = friendListAdapter;
            txt_list_contact_display_name = itemView.findViewById(R.id.txt_list_contact_display_name);
            img_list_contact_avt = itemView.findViewById(R.id.img_list_contact_avt);
            txt_list_contact_create_at = itemView.findViewById(R.id.txt_list_contact_create_at);
        }
    }

    private void getInboxWith(String anotherUserId) {
        String token = sharedPreferencesToken.getString("access-token", null);
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "/with/" + anotherUserId,
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
                error -> Log.i("friend list error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }
}
