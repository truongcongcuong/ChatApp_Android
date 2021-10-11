package com.example.chatapp.adapter;

import android.content.Context;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;

import com.example.chatapp.cons.SendData;
import com.example.chatapp.dto.FriendDTO;

import com.example.chatapp.entity.FriendRequest;
import com.example.chatapp.utils.TimeAgo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    Context context;
    List<FriendRequest> list;
    String token;


    public FriendRequestAdapter(Context context, List<FriendRequest> list, String token) {
        this.context = context;
        this.list = list;
        this.token = token;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_friend_request,parent,false);
        return new ViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest friendDTO = list.get(position);
        Glide.with(context).load(friendDTO.getFrom().getImageUrl())
                .centerCrop().circleCrop().into(holder.img_line_friend_request_avt);
        holder.txt_line_friend_request_name.setText(friendDTO.getFrom().getDisplayName());
        holder.txt_line_friend_request_create_at.setText(TimeAgo.getTime(friendDTO.getCreateAt()));
        holder.btn_line_friend_request_cancel.setOnClickListener(v-> FriendRequestHandle(position, Request.Method.DELETE));

        holder.btn_line_friend_request_accept.setOnClickListener(v-> FriendRequestHandle(position, Request.Method.PUT));
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FriendRequestAdapter adapter;
        ImageView img_line_friend_request_avt;
        TextView txt_line_friend_request_name , txt_line_friend_request_create_at;
        Button btn_line_friend_request_cancel,btn_line_friend_request_accept;
        public ViewHolder(@NonNull View itemView, FriendRequestAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            img_line_friend_request_avt = itemView.findViewById(R.id.img_line_friend_request_avt);
            txt_line_friend_request_name = itemView.findViewById(R.id.txt_line_friend_request_name);
            btn_line_friend_request_cancel = itemView.findViewById(R.id.btn_line_friend_request_cancel);
            btn_line_friend_request_accept = itemView.findViewById(R.id.btn_line_friend_request_accept);
            txt_line_friend_request_create_at = itemView.findViewById(R.id.txt_line_friend_request_create_at);
        }
    }

    private void FriendRequestHandle(int position, int method) {
        FriendRequest friendRequest = list.get(position);
        StringRequest request = new StringRequest(method, Constant.API_FRIEND_REQUEST+"/"+friendRequest.getFrom().getId(),
                response -> {
                    Log.e("response: ",response.toString());
                    SendData sendData = (SendData) context;
                    sendData.SendingData(String.valueOf(true));
                    notifyDataChange(position);
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
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    private void notifyDataChange(int position) {
        list.remove(position);
        notifyDataSetChanged();
    }
}
