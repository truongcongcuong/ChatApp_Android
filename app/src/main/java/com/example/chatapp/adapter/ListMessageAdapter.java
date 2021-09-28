package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.enumvalue.RoomType;
import com.example.chatapp.ui.ChatActivity;
import com.example.chatapp.utils.TimeAgo;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListMessageAdapter extends RecyclerView.Adapter<ListMessageAdapter.ViewHolder> {
    private final Context context;
    private List<InboxDto> list;
    private final int maxMessageSizeDisplay = 5;
    private final Gson gson;
    private final UserSummaryDTO user;
    private SimpleDateFormat dateFormat;
    private String access_token;

    public ListMessageAdapter(Context context, List<InboxDto> dtos) {
        this.context = context;
        if (dtos != null)
            this.list = dtos;
        else
            list = new ArrayList<>();
        gson = new Gson();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        user = gson.fromJson(userJson, UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        access_token = sharedPreferencesToken.getString("access-token", null);
        Log.d("accessssss", access_token);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        InboxDto inboxDto = list.get(position);
        String url;
        String displayName;
        /*
        lấy tên và imageurl nếu là chat group
         */
        if (inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
            displayName = inboxDto.getRoom().getName();
            url = inboxDto.getRoom().getImageUrl();

        } else {
            /*
            lấy tên và iamgeurl nếu là chat one
             */
            displayName = inboxDto.getRoom().getTo().getDisplayName();
            url = inboxDto.getRoom().getTo().getImageUrl();
        }

        // load image
        Glide.with(context).load(url).placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(holder.img_lim_avt);

        MessageDto lastMessage = inboxDto.getLastMessage();
        if (lastMessage != null) {
            holder.txt_lim_last_message.setText(lastMessage.getContent());
            holder.txt_lim_time_last_message.setText(TimeAgo.getTime(lastMessage.getCreateAt()));
            String content;
            if (inboxDto.getRoom().getType().equals(RoomType.GROUP)) {
                /*
                chat group thì hiện tên người gửi cộng nội dung tin nhắn
                 */
                content = lastMessage.getSender().getDisplayName() + ": " + lastMessage.getContent();
            } else {
                /*
                chat one thì hiện mình nội dung tin nhắn
                 */
                content = lastMessage.getContent();
            }
            /*
            nếu tin nhắn của người dùng hiện tại thì hiện "Bạn :" + nội dung tin nhắn
             */
            if (user.getId().equals(lastMessage.getSender().getId()))
                content = "Bạn: " + lastMessage.getContent();
            holder.txt_lim_last_message.setText(content);
        }
        holder.txt_lim_display_name.setText(displayName);

        /*
        số tin nhắn chưa đọc lớn hơn 0
         */
        if (inboxDto.getCountNewMessage() > 0) {
            /*
            set padding và background cho icon số tin nhắn mới
             */
            holder.txt_lim_unread_message.setPadding(20, 7, 20, 7);
            holder.txt_lim_unread_message.setBackgroundResource(R.drawable.background_unreadmessage);
            if (inboxDto.getCountNewMessage() <= maxMessageSizeDisplay)
                holder.txt_lim_unread_message.setText(String.format("%d", inboxDto.getCountNewMessage()));
            else
                holder.txt_lim_unread_message.setText(String.format("%d%s", maxMessageSizeDisplay, "+"));
            /*
            khi có tin nhắn mới thì set font in đậm
             */
            holder.txt_lim_last_message.setTypeface(null, Typeface.BOLD);
            holder.txt_lim_time_last_message.setTypeface(null, Typeface.BOLD);
            holder.txt_lim_display_name.setTypeface(null, Typeface.BOLD);
        } else {
            /*
            xóa text về rỗng khi đã đọc tin nhắn, set phông chữ bình thường
             */
            holder.txt_lim_unread_message.setPadding(0, 0, 0, 0);
            holder.txt_lim_unread_message.setText("");
            holder.txt_lim_display_name.setTypeface(null, Typeface.NORMAL);
            holder.txt_lim_last_message.setTypeface(null, Typeface.NORMAL);
            holder.txt_lim_time_last_message.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            /*
            khi click vào inbox để xem tin nhắn thì set số tin nhắn mới về 0
             */
            inboxDto.setCountNewMessage(0);
            list.set(position, inboxDto);
            Log.d("counttt", list.get(position).getCountNewMessage() + "");

            Intent intent = new Intent(context, ChatActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("dto", inboxDto);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

    }

    /*
    cập nhật thêm inbox khi cuộn
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateList(List<InboxDto> inboxs) {
        list.addAll(inboxs);
        sortTimeLastMessage();
        notifyDataSetChanged();
    }

    /*
    cập nhật khi có tin nhắn mới
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setNewMessage(MessageDto messageDto) {
        boolean inboxIsExists = false;
        for (InboxDto inboxDto : list) {
            if (messageDto != null && inboxDto.getRoom().getId().equals(messageDto.getRoomId())) {
                inboxDto.setLastMessage(messageDto);
                /*
                nếu người gửi của message trùng với người dùng hiện tại thì không tăng số tin nhắn mới
                 */
                if (!user.getId().equals(messageDto.getSender().getId()))
                    inboxDto.setCountNewMessage(inboxDto.getCountNewMessage() + 1);
                inboxIsExists = true;
            }
        }
        /*
        tin nhắn mới đến nhưng mà trong list chưa có inbox của message này nên phải
        lấy inbox này từ server sau đó thêm vào list
         */
        if (!inboxIsExists) {
            StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "/ofRoomId/" + messageDto.getRoomId(),
                    response -> {
                        try {
                            String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                            InboxDto inboxDto = gson.fromJson(res, InboxDto.class);
                            list.add(inboxDto);
                            sortTimeLastMessage();
                            notifyDataSetChanged();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.i("", error.toString())) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + access_token);
                    return map;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(request);
        } else {
            sortTimeLastMessage();
            notifyDataSetChanged();
        }
    }

    /*
    sắp xếp inbox theo thời gian mới nhất
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortTimeLastMessage() {
        if (list != null) {
            list.sort((x, y) -> {
                try {
                    Date d1 = dateFormat.parse(x.getLastMessage().getCreateAt());
                    Date d2 = dateFormat.parse(y.getLastMessage().getCreateAt());
                    if (d1 == null || d2 == null)
                        return 0;
                    return d2.compareTo(d1);
                } catch (ParseException | NullPointerException e) {
                    e.printStackTrace();
                    return 0;
                }
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
        ImageView img_lim_avt;
        TextView txt_lim_display_name;
        TextView txt_lim_last_message;
        TextView txt_lim_time_last_message;
        TextView txt_lim_unread_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_lim_last_message = itemView.findViewById(R.id.txt_lim_last_message);
            img_lim_avt = itemView.findViewById(R.id.img_lim_avt);
            txt_lim_display_name = itemView.findViewById(R.id.txt_lim_display_name);
            txt_lim_time_last_message = itemView.findViewById(R.id.txt_lim_time_last_message);
            txt_lim_unread_message = itemView.findViewById(R.id.txt_lim_unread_message);
        }
    }

}
