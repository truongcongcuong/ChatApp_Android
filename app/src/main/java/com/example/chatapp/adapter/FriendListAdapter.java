package com.example.chatapp.adapter;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dialog.ProfileDialog;
import com.example.chatapp.dto.FriendDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.SneakyThrows;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private List<FriendDTO> list;
    private final Context context;
    private static final SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");

    public FriendListAdapter(List<FriendDTO> list, Context context) {
        if (list == null)
            this.list = new ArrayList<>();
        else
            this.list = list;
        this.context = context;
//        Gson gson = new Gson();
//        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
//        String token = sharedPreferencesToken.getString("access-token", null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_friend_list, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SneakyThrows
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        FriendDTO friend = list.get(position);
        if (friend != null) {
            // load image
            Glide.with(context).load(friend.getFriend().getImageUrl())
                    .placeholder(R.drawable.image_placeholer)
                    .centerCrop().circleCrop().into(holder.img_list_contact_avt);

            holder.txt_list_contact_display_name.setText(friend.getFriend().getDisplayName());
            Date dateCreate = sdfFull.parse(friend.getCreateAt());
            holder.txt_list_contact_create_at.setText(String.format("%s %s", context.getString(R.string.friend_from), sdfYMD.format(dateCreate)));
            holder.itemView.setOnClickListener(v -> {
                ProfileDialog profileDialog = new ProfileDialog(context, friend.getFriend(), null);
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

    public void setList(List<FriendDTO> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img_list_contact_avt;
        TextView txt_list_contact_display_name;
        TextView txt_list_contact_create_at;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_list_contact_display_name = itemView.findViewById(R.id.txt_list_contact_display_name);
            img_list_contact_avt = itemView.findViewById(R.id.img_list_contact_avt);
            txt_list_contact_create_at = itemView.findViewById(R.id.txt_list_contact_create_at);
        }
    }

    /**
     * Click vào một bạn bè để mở activity chat
     */
    /*private void getInboxWith(String anotherUserId) {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "/with/" + anotherUserId,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        InboxDto dto = gson.fromJson(res, InboxDto.class);

                        Intent intent = new Intent(context, ChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dto", dto);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (UnsupportedEncodingException e) {
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
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }*/
}
