package com.example.chatapp.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncContactAdapter extends RecyclerView.Adapter<SyncContactAdapter.ViewHolder> {
    private List<PhoneBookFriendDTO> list;
    private final Context context;
    private String token;

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
        if (dto.isFriend()) {
            holder.btn_li_sync_contact_action.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.gray)));
            holder.btn_li_sync_contact_action.setText(context.getString(R.string.added_button));
        } else {
            holder.btn_li_sync_contact_action.setOnClickListener(v -> sendFriendRequest(dto));
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
                    showDialogSignupSuccess(context.getString(R.string.send_friend_request_title)
                            , context.getString(R.string.send_friend_request_message_success));

                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            showDialogSignupSuccess(context.getString(R.string.send_friend_request_title)
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

    private void showDialogSignupSuccess(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.accept, null)
                .create();
        alertDialog.show();
    }

}
