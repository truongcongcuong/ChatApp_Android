package com.example.chatapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.chatapp.dto.PhoneBookFriendDTO;
import com.example.chatapp.enumvalue.FriendStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncContactAdapter extends RecyclerView.Adapter<SyncContactAdapter.ViewHolder> {
    private List<PhoneBookFriendDTO> list;
    private final Context context;
    private final String token;

    public SyncContactAdapter(List<PhoneBookFriendDTO> list, Context context, String token) {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhoneBookFriendDTO dto = list.get(position);
        if (dto.getFriendStatus().equals(FriendStatus.SENT)) {
            holder.btn_li_sync_contact_action.setText("Thu hồi");
            holder.btn_li_sync_contact_action.setOnClickListener(v -> {
                Toast.makeText(context, "thu hoi loi moi", Toast.LENGTH_SHORT).show();
                deleteSentRequest(dto);
            });
        } else if (dto.getFriendStatus().equals(FriendStatus.RECEIVED)) {
            holder.btn_li_sync_contact_action.setText("Đồng ý");
            holder.btn_li_sync_contact_action.setOnClickListener(v -> {
                Toast.makeText(context, "chap nhan loi moi", Toast.LENGTH_SHORT).show();
                acceptFriendRequest(dto);
            });
        } else if (dto.getFriendStatus().equals(FriendStatus.FRIEND)) {
            holder.btn_li_sync_contact_action.setText("Đã kết bạn");
            holder.btn_li_sync_contact_action.setTextColor(Color.DKGRAY);
            holder.btn_li_sync_contact_action.setBackground(null);
            holder.btn_li_sync_contact_action.setBackgroundTintList(null);

        } else if (dto.getFriendStatus().equals(FriendStatus.NONE)) {
            holder.btn_li_sync_contact_action.setText("Kết bạn");
            holder.btn_li_sync_contact_action.setOnClickListener(v -> {
                Toast.makeText(context, "add friend", Toast.LENGTH_SHORT).show();
                sendFriendRequest(dto);
            });
        }

        Glide.with(context).load(dto.getUser().getImageUrl())
                .centerCrop().circleCrop()
                .into(holder.img_li_sync_contact_avt);
        holder.txt_li_sync_contact_contact_name.setText(dto.getName());
        holder.txt_li_sync_contact_display_name.setText(dto.getUser().getDisplayName());

    }

    private void sendFriendRequest(PhoneBookFriendDTO dto) {
        Log.e("is-active", "true");
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_FRIEND_REQUEST + "/" + dto.getUser().getId(),
                response -> {
                    dto.setFriendStatus(FriendStatus.SENT);
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

    @Override
    public int getItemCount() {
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
        StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_FRIEND_REQUEST + "/" + dto.getUser().getId(),
                response -> {
                    dto.setFriendStatus(FriendStatus.NONE);
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

    private void acceptFriendRequest(PhoneBookFriendDTO dto) {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_FRIEND_REQUEST + "/" + dto.getUser().getId(),
                response -> {
                    dto.setFriendStatus(FriendStatus.FRIEND);
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