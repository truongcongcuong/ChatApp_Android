package com.example.chatapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.FriendRequestSentDto;
import com.example.chatapp.entity.FriendRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageFriendRequestAdapter extends RecyclerView.Adapter<ManageFriendRequestAdapter.ViewHolder> {
    List<FriendRequestSentDto> list;
    Context context;
    String token;

    public ManageFriendRequestAdapter(List<FriendRequestSentDto> list, Context context) {
        this.list = list;
        this.context = context;
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        this.token = sharedPreferencesToken.getString("access-token", null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_manage_friend_request,parent,false);
        return new ViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequestSentDto dto = list.get(position);
        holder.txt_li_manage_friend_request_display_name.setText(dto.getTo().getDisplayName());
        Glide.with(context).load(dto.getTo().getImageUrl())
                .centerCrop().circleCrop().into(holder.img_li_manage_friend_request_avt);
        holder.btn_li_manage_friend_request_recall.setOnClickListener(v->RecallFriendRequest(dto));
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ManageFriendRequestAdapter adapter;
        ImageView img_li_manage_friend_request_avt;
        TextView txt_li_manage_friend_request_display_name;
        Button btn_li_manage_friend_request_recall;
        public ViewHolder(@NonNull View itemView, ManageFriendRequestAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            img_li_manage_friend_request_avt = itemView.findViewById(R.id.img_li_manage_friend_request_avt);
            txt_li_manage_friend_request_display_name = itemView.findViewById(R.id.txt_li_manage_friend_request_display_name);
            btn_li_manage_friend_request_recall = itemView.findViewById(R.id.btn_li_manage_friend_request_recall);
        }
    }


    private void RecallFriendRequest(FriendRequestSentDto dto) {
        StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_FRIEND_REQUEST+"/"+dto.getTo().getId(),
                response -> {
                    Log.e("response: ",response.toString());
                    notifyDataChange(dto);
                }, error -> {
            Log.e("error: ",error.toString());

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    private void notifyDataChange(FriendRequestSentDto dto) {
        list.remove(dto);
        notifyDataSetChanged();
    }
}
