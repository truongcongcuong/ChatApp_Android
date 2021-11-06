package com.example.chatapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dialog.ProfileDialog;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.enumvalue.FriendStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchPhoneAdapter extends RecyclerView.Adapter<SearchPhoneAdapter.ViewHolder> {
    private List<UserProfileDto> list;
    private final Context context;
    private final String token;

    public SearchPhoneAdapter(List<UserProfileDto> list, Context context) {
        this.list = list;
        this.context = context;
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_search_phone, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfileDto user = list.get(position);
        if (user != null) {
            if (user.getFriendStatus().equals(FriendStatus.SENT)) {
                holder.btn_li_search_phone_action.setText(context.getString(R.string.recall_button));
                holder.btn_li_search_phone_action.setOnClickListener(v -> {
                    deleteSentRequest(user);
                });
            } else if (user.getFriendStatus().equals(FriendStatus.RECEIVED)) {
                holder.btn_li_search_phone_action.setText(R.string.accept);
                holder.btn_li_search_phone_action.setOnClickListener(v -> {
                    acceptFriendRequest(user);
                });
            } else if (user.getFriendStatus().equals(FriendStatus.FRIEND)) {
                holder.btn_li_search_phone_action.setText(context.getString(R.string.friend));
                holder.btn_li_search_phone_action.setTextColor(Color.DKGRAY);
                holder.btn_li_search_phone_action.setBackground(null);
                holder.btn_li_search_phone_action.setBackgroundTintList(null);

            } else if (user.getFriendStatus().equals(FriendStatus.NONE)) {
                holder.btn_li_search_phone_action.setText(context.getString(R.string.add_friend));
                holder.btn_li_search_phone_action.setOnClickListener(v -> {
                    sendFriendRequest(user);
                });
            }

            Glide.with(context).load(user.getImageUrl())
                    .placeholder(R.drawable.image_placeholer)
                    .centerCrop().circleCrop()
                    .into(holder.img_li_search_phone_avt);
            holder.txt_li_search_phone_name.setText(user.getDisplayName());
            holder.txt_li_search_phone_detail.setText(user.getPhoneNumber());

            holder.itemView.setOnClickListener(v -> {
                ProfileDialog profileDialog = new ProfileDialog(context, user, null);
                profileDialog.show();
            });
        }

    }

    private void sendFriendRequest(UserProfileDto user) {
        Log.e("is-active", "true");
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_FRIEND_REQUEST + "/" + user.getId(),
                response -> {
                    user.setFriendStatus(FriendStatus.SENT);
                    showDialogSentSuccess(context.getString(R.string.send_friend_request_title)
                            , context.getString(R.string.send_friend_request_message_success));
                    notifyDataSetChanged();
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            showDialogSentSuccess(context.getString(R.string.send_friend_request_title)
                                    , res);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(List<UserProfileDto> newList) {
        this.list = newList;
    }

    private void showDialogSentSuccess(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.accept, null)
                .create();
        alertDialog.show();
    }

    private void deleteSentRequest(UserProfileDto user) {
        StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_FRIEND_REQUEST + "/" + user.getId(),
                response -> {
                    user.setFriendStatus(FriendStatus.NONE);
                    notifyDataSetChanged();
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

    private void acceptFriendRequest(UserProfileDto user) {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_FRIEND_REQUEST + "/" + user.getId(),
                response -> {
                    user.setFriendStatus(FriendStatus.FRIEND);
                    notifyDataSetChanged();
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_li_search_phone_avt;
        TextView txt_li_search_phone_name;
        TextView txt_li_search_phone_detail;
        Button btn_li_search_phone_action;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_li_search_phone_avt = itemView.findViewById(R.id.img_li_search_phone_avt);
            txt_li_search_phone_name = itemView.findViewById(R.id.txt_li_search_phone_name);
            txt_li_search_phone_detail = itemView.findViewById(R.id.txt_li_search_phone_detail);
            btn_li_search_phone_action = itemView.findViewById(R.id.btn_li_search_phone_action);

        }
    }

}
