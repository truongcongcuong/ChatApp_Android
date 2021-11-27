package com.example.chatapp.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dialog.ProfileDialog;
import com.example.chatapp.dto.PhoneBookFriendDTO;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.enumvalue.FriendStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncContactAdapter extends RecyclerView.Adapter<SyncContactAdapter.ViewHolder> {
    private List<PhoneBookFriendDTO> list;
    private final Context context;
    private final String token;

    public void setList(List<PhoneBookFriendDTO> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public SyncContactAdapter(List<PhoneBookFriendDTO> list, Context context, String token) {
        if (list == null)
            this.list = new ArrayList<>();
        else
            this.list = list;
        this.context = context;
        this.token = token;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_sync_contact, parent, false);
        return new ViewHolder(view, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhoneBookFriendDTO dto = list.get(position);
        UserProfileDto user = dto.getUser();
        if (user != null) {
            if (user.getFriendStatus().equals(FriendStatus.SENT)) {
                holder.btn_li_sync_contact_action.setText(context.getString(R.string.recall_button));
                holder.btn_li_sync_contact_action.setTextColor(Color.WHITE);
                holder.btn_li_sync_contact_action.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.purple_200)));
                holder.btn_li_sync_contact_action.setOnClickListener(v -> {
                    deleteSentRequest(dto);
                });
            } else if (user.getFriendStatus().equals(FriendStatus.RECEIVED)) {
                holder.btn_li_sync_contact_action.setText(R.string.accept);
                holder.btn_li_sync_contact_action.setTextColor(Color.WHITE);
                holder.btn_li_sync_contact_action.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.purple_200)));
                holder.btn_li_sync_contact_action.setOnClickListener(v -> {
                    acceptFriendRequest(dto);
                });
            } else if (user.getFriendStatus().equals(FriendStatus.FRIEND)) {
                holder.btn_li_sync_contact_action.setText(context.getString(R.string.friend));
                holder.btn_li_sync_contact_action.setTextColor(Color.DKGRAY);
                holder.btn_li_sync_contact_action.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.background_material_light)));
                holder.btn_li_sync_contact_action.setOnClickListener(null);

            } else if (user.getFriendStatus().equals(FriendStatus.NONE)) {
                holder.btn_li_sync_contact_action.setText(context.getString(R.string.add_friend));
                holder.btn_li_sync_contact_action.setTextColor(Color.WHITE);
                holder.btn_li_sync_contact_action.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.purple_200)));
                holder.btn_li_sync_contact_action.setOnClickListener(v -> {
                    sendFriendRequest(dto);
                });
            }

            Glide.with(context).load(user.getImageUrl())
                    .placeholder(R.drawable.img_avatar_placeholer)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop().circleCrop()
                    .into(holder.img_li_sync_contact_avt);
            holder.txt_li_sync_contact_contact_name.setText(dto.getName());
            holder.txt_li_sync_contact_display_name.setText(user.getDisplayName());

            holder.itemView.setOnClickListener(v -> {
                ProfileDialog profileDialog = new ProfileDialog(context, user, null);
                profileDialog.show();
            });
        }

    }

    private void sendFriendRequest(PhoneBookFriendDTO dto) {
        Log.e("is-active", "true");
        UserProfileDto user = dto.getUser();
        if (user != null) {
            StringRequest request = new StringRequest(Request.Method.POST, Constant.API_FRIEND_REQUEST + "/" + user.getId(),
                    response -> {
                        user.setFriendStatus(FriendStatus.SENT);
                        dto.setUser(user);
                        showDialogSentSuccess(context.getString(R.string.send_friend_request_title)
                                , context.getString(R.string.send_friend_request_message_success));
                        notifyDataSetChanged();
                    },
                    error -> {
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && error != null) {
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
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        SyncContactAdapter adapter;
        ImageView img_li_sync_contact_avt;
        TextView txt_li_sync_contact_contact_name;
        TextView txt_li_sync_contact_display_name;
        Button btn_li_sync_contact_action;

        public ViewHolder(@NonNull View itemView, SyncContactAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            img_li_sync_contact_avt = itemView.findViewById(R.id.img_li_sync_contact_avt);
            txt_li_sync_contact_contact_name = itemView.findViewById(R.id.txt_li_sync_contact_contact_name);
            txt_li_sync_contact_display_name = itemView.findViewById(R.id.txt_li_sync_contact_display_name);
            btn_li_sync_contact_action = itemView.findViewById(R.id.btn_li_sync_contact_action);

        }
    }

    private void showDialogSentSuccess(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.accept, null)
                .create();
        alertDialog.show();
    }

    private void deleteSentRequest(PhoneBookFriendDTO dto) {
        UserProfileDto user = dto.getUser();
        if (user != null) {
            StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_FRIEND_REQUEST + "/" + user.getId(),
                    response -> {
                        user.setFriendStatus(FriendStatus.NONE);
                        dto.setUser(user);
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
    }

    private void acceptFriendRequest(PhoneBookFriendDTO dto) {
        UserProfileDto user = dto.getUser();
        if (user != null) {
            StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_FRIEND_REQUEST + "/" + user.getId(),
                    response -> {
                        user.setFriendStatus(FriendStatus.FRIEND);
                        dto.setUser(user);
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
    }

}
