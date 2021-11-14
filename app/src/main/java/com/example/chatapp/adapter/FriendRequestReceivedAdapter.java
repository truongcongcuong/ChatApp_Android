package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dialog.ProfileDialog;
import com.example.chatapp.entity.FriendRequest;
import com.example.chatapp.ui.FriendRequestActivity;
import com.example.chatapp.utils.TimeAgo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestReceivedAdapter extends RecyclerView.Adapter<FriendRequestReceivedAdapter.ViewHolder> {
    private final Context context;
    private List<FriendRequest> list;
    private final String token;

    public void updateList(List<FriendRequest> list) {
        if (this.list == null)
            this.list = new ArrayList<>();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public void setList(List<FriendRequest> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public FriendRequestReceivedAdapter(Context context, List<FriendRequest> list, String token) {
        this.context = context;
        if (list == null)
            this.list = new ArrayList<>();
        else
            this.list = list;
        this.token = token;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_friend_request_received, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collections.sort(list);
        FriendRequest friendDTO = list.get(position);

        if (friendDTO != null) {
            Glide.with(context).load(friendDTO.getFrom().getImageUrl())
                    .placeholder(R.drawable.img_avatar_placeholer)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop().circleCrop().into(holder.img_line_friend_request_avt);
            holder.txt_line_friend_request_name.setText(friendDTO.getFrom().getDisplayName());
            try {
                holder.txt_line_friend_request_create_at.setText(TimeAgo.getTime(friendDTO.getCreateAt()));
            } catch (ParseException | NullPointerException e) {
                holder.txt_line_friend_request_create_at.setText("");
            }
            holder.btn_line_friend_request_cancel.setOnClickListener(v -> {
//                delete friend request
                FriendRequestHandle(position, Request.Method.DELETE);
            });

            holder.btn_line_friend_request_accept.setOnClickListener(v -> {
//                accept friend request
                FriendRequestHandle(position, Request.Method.PUT);
            });

            holder.itemView.setOnClickListener(v -> {
                ProfileDialog profileDialog = new ProfileDialog(context, friendDTO.getFrom(), null);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_line_friend_request_avt;
        TextView txt_line_friend_request_name;
        TextView txt_line_friend_request_create_at;
        Button btn_line_friend_request_cancel;
        Button btn_line_friend_request_accept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_line_friend_request_avt = itemView.findViewById(R.id.img_line_friend_request_avt);
            txt_line_friend_request_name = itemView.findViewById(R.id.txt_line_friend_request_name);
            btn_line_friend_request_cancel = itemView.findViewById(R.id.btn_line_friend_request_cancel);
            btn_line_friend_request_accept = itemView.findViewById(R.id.btn_line_friend_request_accept);
            txt_line_friend_request_create_at = itemView.findViewById(R.id.txt_line_friend_request_create_at);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void FriendRequestHandle(int position, int method) {
        FriendRequest friendRequest = list.get(position);
        StringRequest request = new StringRequest(method, Constant.API_FRIEND_REQUEST + "/" + friendRequest.getFrom().getId(),
                response -> {
                    Log.e("response: ", response);
                    notifyDataChange(friendRequest);
                }, error -> Log.e("error: ", error.toString())) {
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void notifyDataChange(FriendRequest friendRequest) {
        list.removeIf(x -> x.getFrom().getId().equals(friendRequest.getFrom().getId()));
        notifyDataSetChanged();

        /*
        khi có thay đổi thì gửi thông báo đến cho FriendRequestActivity,
        nếu list trống thì hiện dòng text lên để thông báo
         */
        Intent intent = new Intent("friendRequest/received/empty");
        intent.putExtra("dto", list.isEmpty());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        /*
        yêu cầu FriendRequestActivity cập nhật lại số lượng lời mời đã nhận trên tabLayout
         */
        FriendRequestActivity activity = (FriendRequestActivity) this.context;
        activity.countFriendRequestReceived();
    }

}
