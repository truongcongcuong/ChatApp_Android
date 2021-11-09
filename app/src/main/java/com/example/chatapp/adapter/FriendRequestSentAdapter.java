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

public class FriendRequestSentAdapter extends RecyclerView.Adapter<FriendRequestSentAdapter.ViewHolder> {
    private final Context context;
    private List<FriendRequest> list;
    private final String token;

    public void setList(List<FriendRequest> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateList(List<FriendRequest> list) {
        if (this.list == null)
            this.list = new ArrayList<>();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public FriendRequestSentAdapter(Context context, List<FriendRequest> list, String token) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_friend_request_sent, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collections.sort(list);
        FriendRequest friendDTO = list.get(position);
        if (friendDTO != null) {
            if (friendDTO.getTo() != null) {
                Glide.with(context).load(friendDTO.getTo().getImageUrl())
                        .placeholder(R.drawable.image_placeholer)
                        .centerCrop().circleCrop().into(holder.img_line_friend_request_sent_avt);
                holder.txt_line_friend_request_sent_name.setText(friendDTO.getTo().getDisplayName());
            }

            try {
                holder.txt_line_friend_request_sent_create_at.setText(TimeAgo.getTime(friendDTO.getCreateAt()));
            } catch (ParseException e) {
                holder.txt_line_friend_request_sent_create_at.setText("");
            }
            holder.btn_line_friend_request_sent_cancel.setOnClickListener(v -> deleteSentRequest(position));
            holder.btn_line_friend_request_sent_cancel.setText(context.getString(R.string.recall_button));

            holder.itemView.setOnClickListener(v -> {
                ProfileDialog profileDialog = new ProfileDialog(context, friendDTO.getTo(), null);
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
        ImageView img_line_friend_request_sent_avt;
        TextView txt_line_friend_request_sent_name;
        TextView txt_line_friend_request_sent_create_at;
        Button btn_line_friend_request_sent_cancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_line_friend_request_sent_avt = itemView.findViewById(R.id.img_line_friend_request_sent_avt);
            txt_line_friend_request_sent_name = itemView.findViewById(R.id.txt_line_friend_request_sent_name);
            txt_line_friend_request_sent_create_at = itemView.findViewById(R.id.txt_line_friend_request_sent_create_at);
            btn_line_friend_request_sent_cancel = itemView.findViewById(R.id.btn_line_friend_request_sent_cancel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteSentRequest(int position) {
        FriendRequest friendRequest = list.get(position);
        if (friendRequest != null && friendRequest.getTo() != null) {
            StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_FRIEND_REQUEST + "/" + friendRequest.getTo().getId(),
                    response -> {
                        Log.e("response: ", response);
                        notifyDataChange(position);
                    }, error -> {
                Log.e("error: ", error.toString());

            }) {
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
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void notifyDataChange(int position) {
        FriendRequest friendRequest = list.get(position);
        list.removeIf(x -> x.getTo().getId().equals(friendRequest.getTo().getId()));
        notifyDataSetChanged();

        /*
        khi có thay đổi thì gửi thông báo đến cho FriendRequestActivity,
        nếu list trống thì hiện dòng text lên để thông báo
         */
        Intent intent = new Intent("friendRequest/sent/empty");
        intent.putExtra("dto", list.isEmpty());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        /*
        yêu cầu FriendRequestActivity cập nhật lại số lượng lời mời đã gửi trên tabLayout
         */
        FriendRequestActivity activity = (FriendRequestActivity) this.context;
        activity.countFriendRequestSent();
    }

}
