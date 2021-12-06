package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MyMedia;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.enumvalue.MessageType;
import com.example.chatapp.enumvalue.RoomType;
import com.example.chatapp.ui.ChatActivity;
import com.example.chatapp.utils.MyAutoLink;
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
    private static final int maxMessageSizeDisplay = 5;
    private final Gson gson;
    private final UserSummaryDTO user;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String access_token;

    /*
    cập nhật room cho inbox khi có sự kiện thêm hoặc xóa thành viên trên socket
     */
    public void updateRoomChange(RoomDTO newRoom) {
        if (list == null)
            return;
        for (InboxDto inboxDto : list) {
            if (inboxDto.getRoom() != null && newRoom.getId().equals(inboxDto.getRoom().getId())) {
                inboxDto.setRoom(newRoom);
            }
        }
        notifyDataSetChanged();
    }

    public ListMessageAdapter(Context context, List<InboxDto> list) {
        this.context = context;
        if (list == null)
            this.list = new ArrayList<>();
        else
            this.list = list;
        gson = new Gson();

        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        user = gson.fromJson(userJson, UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        access_token = sharedPreferencesToken.getString("access-token", null);
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
        if (inboxDto != null) {
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
            Glide.with(context).load(url).placeholder(R.drawable.img_avatar_placeholer)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop().circleCrop().into(holder.img_lim_avt);

            MessageDto lastMessage = inboxDto.getLastMessage();
            if (lastMessage != null) {
                holder.txt_lim_last_message.setText(Html.fromHtml(lastMessage.getContent()));
                try {
                    holder.txt_lim_time_last_message.setText(TimeAgo.getTime(lastMessage.getCreateAt(), context));
                } catch (ParseException e) {
                    holder.txt_lim_time_last_message.setText("");
                }
                String content = lastMessage.getContent();
                if (lastMessage.getSender() != null) {
                    if (lastMessage.getType().equals(MessageType.TEXT)) {
                        if (MyAutoLink.isContainsLink(lastMessage.getContent())) {
                            content = String.format("[%s]", context.getString(R.string.message_type_link));
                            if (lastMessage.getContent() != null)
                                content = String.format("%s %s", content, lastMessage.getContent());
                        } else
                            content = lastMessage.getContent();
                    } else if (lastMessage.getType().equals(MessageType.MEDIA)) {
                        List<MyMedia> mediaList = lastMessage.getMedia();
                        if (mediaList != null && !mediaList.isEmpty()) {
                            MyMedia media = mediaList.get(mediaList.size() - 1);
                            if (media.getType().equals(MediaType.IMAGE)) {
                                content = String.format("[%s]", context.getString(R.string.message_type_image));
                                if (lastMessage.getContent() != null)
                                    content = String.format("%s %s", content, lastMessage.getContent());
                            } else if (media.getType().equals(MediaType.VIDEO)) {
                                content = String.format("[%s]", context.getString(R.string.message_type_video));
                                if (lastMessage.getContent() != null)
                                    content = String.format("%s %s", content, lastMessage.getContent());
                            } else if (media.getType().equals(MediaType.FILE)) {
                                content = String.format("[%s]", context.getString(R.string.message_type_file));
                                if (lastMessage.getContent() != null)
                                    content = String.format("%s %s", content, media.getName());
                            }
                        }
                    }
//                    nếu tin nhắn của người dùng hiện tại thì hiện "Bạn :" + nội dung tin nhắn
                    if (user.getId().equals(lastMessage.getSender().getId()))
                        content = String.format("%s: %s", context.getString(R.string.you), content);
                    else {
                        if (inboxDto.getRoom().getType().equals(RoomType.GROUP))
                            content = String.format("%s: %s", lastMessage.getSender().getDisplayName(), content);
                    }
                }
                holder.txt_lim_last_message.setText(Html.fromHtml(content));
            }
            holder.txt_lim_display_name.setText(displayName);

        /*
        số tin nhắn chưa đọc lớn hơn 0
         */
            if (inboxDto.getCountNewMessage() > 0) {
                holder.txt_lim_unread_message.setVisibility(View.VISIBLE);
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
                holder.txt_lim_unread_message.setVisibility(View.GONE);
                holder.txt_lim_display_name.setTypeface(null, Typeface.NORMAL);
                holder.txt_lim_last_message.setTypeface(null, Typeface.NORMAL);
                holder.txt_lim_time_last_message.setTypeface(null, Typeface.NORMAL);
            }

            holder.itemView.setOnClickListener(v -> {
            /*
            khi click vào inbox để xem tin nhắn thì set số tin nhắn mới về 0
             */
                Intent intent = new Intent(context, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("dto", inboxDto);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                inboxDto.setCountNewMessage(0);
                list.set(position, inboxDto);
                notifyItemChanged(position);
            });
        }

    }

    /*
    cập nhật thêm inbox khi cuộn
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateList(List<InboxDto> newList) {
        list.addAll(newList);
        sortTimeLastMessage();
        notifyDataSetChanged();
    }

    public void setList(List<InboxDto> list) {
        this.list = list;
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
                if (messageDto.getSender() != null && !messageDto.getSender().getId().equals(user.getId()))
                    inboxDto.setCountNewMessage(inboxDto.getCountNewMessage() + 1);
                else if (messageDto.getSender() == null)
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
                            Log.d("------", inboxDto.toString());
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
            DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(retryPolicy);
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
                    if (x.getLastMessage() == null || y.getLastMessage() == null)
                        return 0;
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

    public void resetUnReadMessageForRoom(String roomId) {
        for (int i = 0; i < list.size(); i++) {
            InboxDto inboxDto = list.get(i);
            if (inboxDto.getRoom().getId().equals(roomId)) {
                inboxDto.setCountNewMessage(0);
                notifyItemChanged(i);
                break;
            }
        }
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

            txt_lim_unread_message.setVisibility(View.GONE);
        }
    }

}
