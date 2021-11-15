package com.example.chatapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dialog.MessageOptionDialog;
import com.example.chatapp.dialog.ProfileDialog;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MyMedia;
import com.example.chatapp.dto.ReactionReceiver;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.ReadByReceiver;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.Reaction;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.enumvalue.MessageType;
import com.example.chatapp.ui.ViewImageActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final List<MessageDto> list;
    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVER = 2;
    private static final int ITEM_SYSTEM = 3;
    private UserSummaryDTO user;
    private RecyclerView recyclerView;

    public MessageAdapter(Context context, List<MessageDto> list) {
        this.context = context;
        Gson gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        user = gson.fromJson(userJson, UserSummaryDTO.class);
        if (list == null)
            this.list = new ArrayList<>();
        else
            this.list = list;
    }

    // khi nhận được reaction notification thì cập nhật reaction vào message
    public void updateReactionToMessage(ReactionReceiver receiver) {
        for (int i = 0; i < list.size(); i++) {
            MessageDto m = list.get(i);
            if (m.getId().equals(receiver.getMessageId())) {
                List<Reaction> reactions = m.getReactions();
                if (reactions == null)
                    reactions = new ArrayList<>();
                Reaction reaction = new Reaction();
                reaction.setType(receiver.getType());
                reaction.setReactByUserId(receiver.getReactByUser().getId());
                reactions.add(reaction);

                m.setReactions(reactions);

                /*
                tìm holder tại vị trí message đó, set recyclable=true để cập nhật lại reaction
                sau đó set lại recyclable=false để khi cuộn dữ liệu không bị sai
                 */
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    holder.setIsRecyclable(true);
                    Activity activity = (Activity) context;
                    activity.runOnUiThread(this::notifyDataSetChanged);
                    holder.setIsRecyclable(false);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateReadToMessage(ReadByReceiver readByReceiver) {
        for (int i = 0; i < list.size(); i++) {
            MessageDto m = list.get(i);

            // cập nhật read tracking mới cho message
            Set<ReadByDto> readbyes = m.getReadbyes();
            if (m.getId().equals(readByReceiver.getMessageId())) {
                if (readbyes == null)
                    readbyes = new HashSet<>();
                ReadByDto readByDto = new ReadByDto();
                readByDto.setReadAt(readByReceiver.getReadAt());
                readByDto.setReadByUser(readByReceiver.getReadByUser());
                readbyes.add(readByDto);

                m.setReadbyes(readbyes);
                Log.i("message readed", m.toString());

                /*
                tìm holder tại vị trí message, bật recyclable sau đó cập nhật read tracking
                sau đó tắt recyclable
                 */
                try {
                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                    if (holder != null) {
                        holder.setIsRecyclable(true);
                        Activity activity = (Activity) context;
                        activity.runOnUiThread(this::notifyDataSetChanged);
                        holder.setIsRecyclable(false);
                    }
                } catch (Exception ignored) {

                }
            }
            // xóa read tracking cũ cho message
            if (!m.getId().equals(readByReceiver.getMessageId())) {
                if (readbyes != null && !readbyes.isEmpty()) {
                    readbyes.removeIf(x -> x.getReadByUser().getId().equals(readByReceiver.getReadByUser().getId()));
                    m.setReadbyes(readbyes);
                }
                try {
                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                    if (holder != null) {
                        holder.setIsRecyclable(true);
                        Activity activity = (Activity) context;
                        activity.runOnUiThread(this::notifyDataSetChanged);
                        holder.setIsRecyclable(false);
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    /*
    xóa readTracking cũ
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void deleteOldReadTracking(String userId) {
        for (int i = 0; i < list.size(); i++) {
            MessageDto m = list.get(i);
            Set<ReadByDto> readbyes = m.getReadbyes();
            // xóa readby cũ cho message
            if (readbyes != null && !readbyes.isEmpty()) {
                readbyes.removeIf(x -> x.getReadByUser().getId().equals(userId));
                m.setReadbyes(readbyes);
            }
            try {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    holder.setIsRecyclable(true);
                    Activity activity = (Activity) context;
                    activity.runOnUiThread(this::notifyDataSetChanged);
                    holder.setIsRecyclable(false);
                }
            } catch (Exception ignored) {

            }
        }
    }


    /*
    lấy recycler view hiện đang dùng bởi adapter
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_message_send_chat_activity, parent, false);
            return new SenderViewHolder(view);
        } else if (viewType == ITEM_RECEIVER) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_message_receiver_chat_activity, parent, false);
            return new ReceiverViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_system_message_chat, parent, false);
            return new SystemViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDto messageDto = list.get(position);
        if (messageDto != null) {
            if (position == 0) {
                /*
                nếu là tin nhắn cuối cùng thì set padding top
                 */
                holder.itemView.setPadding(
                        holder.itemView.getPaddingLeft(),
                        30,
                        holder.itemView.getPaddingRight(),
                        holder.itemView.getPaddingBottom()
                );
            }
            switch (getItemViewType(position)) {
                case ITEM_SEND:
                    SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                    /*
                    chỉ hiện thời gian ở tin nhắn cuối cùng
                     */
                    if (position == list.size() - 1) {
                        senderViewHolder.timeOfMessage.setVisibility(View.VISIBLE);
                        senderViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                    } else {
                        if (messageDto.getSender() != null) {
                            if (list.get(position + 1).getSender() == null) {
                                senderViewHolder.timeOfMessage.setVisibility(View.VISIBLE);
                                senderViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                            } else if (!messageDto.getSender().getId().equals(list.get(position + 1).getSender().getId())) {
                                senderViewHolder.timeOfMessage.setVisibility(View.VISIBLE);
                                senderViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                            }
                        }
                    }
                    MessageDto replySend = messageDto.getReply();
                    if (replySend != null) {
                        senderViewHolder.send_message_reply_layout.setVisibility(View.VISIBLE);
                        if (replySend.getSender() != null)
                            senderViewHolder.send_message_reply_name.setText(replySend.getSender().getDisplayName());
                        senderViewHolder.send_message_reply_content.setText(replySend.getContent());
                    }
                    switch (messageDto.getType()) {
                        case TEXT:
                            senderViewHolder.senderMessage.setVisibility(View.VISIBLE);
                            senderViewHolder.senderMessage.setText(messageDto.getContent());
                            if (messageDto.isDeleted()) {
                                senderViewHolder.senderMessage.setTextColor(Color.GRAY);
                            }
                            break;
                        case MEDIA:
                            final int COLUMNS = 4;
                            if (messageDto.getContent() != null && !messageDto.getContent().isEmpty()) {
                                senderViewHolder.senderMessage.setVisibility(View.VISIBLE);
                                senderViewHolder.senderMessage.setText(messageDto.getContent());
                                if (messageDto.isDeleted()) {
                                    senderViewHolder.senderMessage.setTextColor(Color.GRAY);
                                }
                            }
                            List<MyMedia> media = messageDto.getMedia();
                            if (media != null) {
                                media.sort((o1, o2) -> o1.getType().compareTo(o2.getType()));
                                int numMedia = media.size();
                                GridLayoutManager layoutManager;
                                LineItemMediaAdapter mediaAdapter;
                                if (numMedia > 0 && numMedia < COLUMNS) {
                                    senderViewHolder.send_message_rcv_media.setVisibility(View.VISIBLE);
                                    layoutManager = new GridLayoutManager(context, numMedia);
                                    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                        @Override
                                        public int getSpanSize(int position) {
                                            if (media.get(position).getType().equals(MediaType.FILE))
                                                return numMedia;
                                            return 1;
                                        }
                                    });
                                    senderViewHolder.send_message_rcv_media.setLayoutManager(layoutManager);
                                    mediaAdapter = new LineItemMediaAdapter(context, messageDto, numMedia);
                                    senderViewHolder.send_message_rcv_media.setAdapter(mediaAdapter);
                                } else {
                                    senderViewHolder.send_message_rcv_media.setVisibility(View.VISIBLE);
                                    layoutManager = new GridLayoutManager(context, COLUMNS);
                                    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                        @Override
                                        public int getSpanSize(int position) {
                                            if (media.get(position).getType().equals(MediaType.FILE))
                                                return COLUMNS;
                                            return 1;
                                        }
                                    });
                                    senderViewHolder.send_message_rcv_media.setLayoutManager(layoutManager);
                                    mediaAdapter = new LineItemMediaAdapter(context, messageDto, COLUMNS);
                                    senderViewHolder.send_message_rcv_media.setAdapter(mediaAdapter);
                                }
                            }
                            break;
                    }
                    // hiện danh sách người đã xem tin nhắn
                    if (messageDto.getReadbyes() != null) {
                        int numSeen = 0;
                        for (ReadByDto read : messageDto.getReadbyes()) {
                            if (!user.getId().equals(read.getReadByUser().getId()))
                                numSeen++;
                        }
//                        messageDto.getReadbyes().removeIf(x -> x.getReadByUser().getId().equals(user.getId()));
                        ReadbyAdapter readbyAdapter = new ReadbyAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);

                        // nhiều người đã xem tin nhắn
                        if (numSeen > 1) {
                            if (senderViewHolder.rcv_read_many.getLayoutManager() == null)
                                senderViewHolder.rcv_read_many.setLayoutManager(layoutManager);
                            senderViewHolder.rcv_read_many.setAdapter(readbyAdapter);
                            senderViewHolder.send_message_rcv_readby_one_many.setVisibility(View.VISIBLE);
                        }
                        // chỉ có một người đã xem tin nhắn
                        if (numSeen == 1) {
                            if (senderViewHolder.rcv_read_one.getLayoutManager() == null)
                                senderViewHolder.rcv_read_one.setLayoutManager(layoutManager);
                            senderViewHolder.rcv_read_one.setAdapter(readbyAdapter);
                        }
                    }
                    // hiện danh sách cảm xúc
                    if (messageDto.getReactions() != null && !messageDto.getReactions().isEmpty()) {
                        ReactionAdapter reactionAdapter = new ReactionAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//                        layoutManager.setStackFromEnd(true);

                        if (senderViewHolder.send_rcv_reaction.getLayoutManager() == null)
                            senderViewHolder.send_rcv_reaction.setLayoutManager(layoutManager);
                        senderViewHolder.send_rcv_reaction.setAdapter(reactionAdapter);

                        senderViewHolder.send_message_reaction_layout.setVisibility(View.VISIBLE);
                    }
                    senderViewHolder.messageLayout.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                    break;
                case ITEM_RECEIVER:
                    ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                    /*
                     chỉ hiện ảnh của tin nhắn cuối cùng theo user id
                     chỉ hiện thời gian của tin nhắn cuối cùng
                     */
                    if (messageDto.getSender() != null) {
                        if (position == list.size() - 1) {
                            Glide.with(context).load(messageDto.getSender().getImageUrl())
                                    .centerCrop().circleCrop().placeholder(R.drawable.img_avatar_placeholer)
                                    .into(receiverViewHolder.receiverImage);
                            receiverViewHolder.receiverImage.setBackgroundResource(R.drawable.border_for_circle_image);

                            receiverViewHolder.timeOfMessage.setVisibility(View.VISIBLE);
                            receiverViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                        } else {
                            if (messageDto.getSender() != null) {
                                if (list.get(position + 1).getSender() == null) {
                                    Glide.with(context).load(messageDto.getSender().getImageUrl())
                                            .centerCrop().circleCrop().placeholder(R.drawable.img_avatar_placeholer)
                                            .into(receiverViewHolder.receiverImage);

                                    receiverViewHolder.timeOfMessage.setVisibility(View.VISIBLE);
                                    receiverViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));

                                } else if (!messageDto.getSender().getId().equals(list.get(position + 1).getSender().getId())) {
                                    Glide.with(context).load(messageDto.getSender().getImageUrl())
                                            .centerCrop().circleCrop().placeholder(R.drawable.img_avatar_placeholer)
                                            .into(receiverViewHolder.receiverImage);
                                    receiverViewHolder.receiverImage.setBackgroundResource(R.drawable.border_for_circle_image);

                                    receiverViewHolder.timeOfMessage.setVisibility(View.VISIBLE);
                                    receiverViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));

                                }
                            }
                        }
                    }
                    MessageDto replyReceiver = messageDto.getReply();
                    if (replyReceiver != null) {
                        receiverViewHolder.receiver_message_reply_layout.setVisibility(View.VISIBLE);
                        if (replyReceiver.getSender() != null)
                            receiverViewHolder.receiver_message_reply_name.setText(replyReceiver.getSender().getDisplayName());
                        receiverViewHolder.receiver_message_reply_content.setText(replyReceiver.getContent());
                    }
                    switch (messageDto.getType()) {
                        case TEXT:
                            receiverViewHolder.receiverMessage.setVisibility(View.VISIBLE);
                            receiverViewHolder.receiverMessage.setText(messageDto.getContent());
                            if (messageDto.isDeleted()) {
                                receiverViewHolder.receiverMessage.setTextColor(Color.GRAY);
                            }
                            break;
                        case MEDIA:
                            final int COLUMNS = 4;
                            if (messageDto.getContent() != null && !messageDto.getContent().isEmpty()) {
                                receiverViewHolder.receiverMessage.setVisibility(View.VISIBLE);
                                receiverViewHolder.receiverMessage.setText(messageDto.getContent());
                                if (messageDto.isDeleted()) {
                                    receiverViewHolder.receiverMessage.setTextColor(Color.GRAY);
                                }
                            }
                            List<MyMedia> media = messageDto.getMedia();
                            if (media != null) {
                                int numMedia = media.size();
                                media.sort((o1, o2) -> o1.getType().compareTo(o2.getType()));
                                GridLayoutManager layoutManager;
                                LineItemMediaAdapter mediaAdapter;
                                if (numMedia > 0 && numMedia < COLUMNS) {
                                    receiverViewHolder.received_message_rcv_media.setVisibility(View.VISIBLE);
                                    layoutManager = new GridLayoutManager(context, numMedia);
                                    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                        @Override
                                        public int getSpanSize(int position) {
                                            if (media.get(position).getType().equals(MediaType.FILE))
                                                return numMedia;
                                            return 1;
                                        }
                                    });
                                    receiverViewHolder.received_message_rcv_media.setLayoutManager(layoutManager);
                                    mediaAdapter = new LineItemMediaAdapter(context, messageDto, numMedia);
                                    receiverViewHolder.received_message_rcv_media.setAdapter(mediaAdapter);
                                } else {
                                    receiverViewHolder.received_message_rcv_media.setVisibility(View.VISIBLE);
                                    layoutManager = new GridLayoutManager(context, COLUMNS);
                                    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                        @Override
                                        public int getSpanSize(int position) {
                                            if (media.get(position).getType().equals(MediaType.FILE))
                                                return COLUMNS;
                                            return 1;
                                        }
                                    });
                                    receiverViewHolder.received_message_rcv_media.setLayoutManager(layoutManager);
                                    mediaAdapter = new LineItemMediaAdapter(context, messageDto, COLUMNS);
                                    receiverViewHolder.received_message_rcv_media.setAdapter(mediaAdapter);
                                }
                            }
                            break;
                    }
                    // hiện danh sách những người đã xem tin nhắn
                    if (messageDto.getReadbyes() != null) {
                        int numSeen = 0;
                        for (ReadByDto read : messageDto.getReadbyes()) {
                            if (!user.getId().equals(read.getReadByUser().getId()))
                                numSeen++;
                        }
//                        messageDto.getReadbyes().removeIf(x -> x.getReadByUser().getId().equals(user.getId()));
                        ReadbyAdapter readbyAdapter = new ReadbyAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);

                        // nhiều người đã xem tin nhắn
                        if (numSeen > 1) {
                            if (receiverViewHolder.rcv_read_many.getLayoutManager() == null)
                                receiverViewHolder.rcv_read_many.setLayoutManager(layoutManager);
                            receiverViewHolder.rcv_read_many.setAdapter(readbyAdapter);

                            receiverViewHolder.receiver_message_rcv_readby_one_many.setVisibility(View.VISIBLE);
                        }
                        // chỉ có một người đã xem tin nhắn
                        if (numSeen == 1) {
                            if (receiverViewHolder.rcv_read_one.getLayoutManager() == null)
                                receiverViewHolder.rcv_read_one.setLayoutManager(layoutManager);
                            receiverViewHolder.rcv_read_one.setAdapter(readbyAdapter);
                        }
                    }
                    // hiện danh sách cảm xúc
                    if (messageDto.getReactions() != null && !messageDto.getReactions().isEmpty()) {
                        ReactionAdapter reactionAdapter = new ReactionAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//                        layoutManager.setStackFromEnd(true);

                        if (receiverViewHolder.receiver_rcv_reaction.getLayoutManager() == null)
                            receiverViewHolder.receiver_rcv_reaction.setLayoutManager(layoutManager);
                        receiverViewHolder.receiver_rcv_reaction.setAdapter(reactionAdapter);

                        receiverViewHolder.receiver_message_reaction_layout.setVisibility(View.VISIBLE);
                    }
                    receiverViewHolder.messageLayout.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                    receiverViewHolder.receiverImage.setOnClickListener(v -> {
                        ProfileDialog profileDialog = new ProfileDialog(context, messageDto.getSender(), null);
                        profileDialog.show();
                    });
                    break;
                case ITEM_SYSTEM:
                    SystemViewHolder systemViewHolder = (SystemViewHolder) holder;
                    systemViewHolder.system_message_time.setText(messageDto.getCreateAt());
                    systemViewHolder.system_message_content.setText(messageDto.getContent());
                    break;
            }
        }
        // khi cuộn nhanh thì nội dung của item sẽ cập nhật nhiều lần dẫn đến sai
        // set recyable=false để khi cuộn không cập nhật item
        // khi nào cần cập nhật thì set=true
        holder.setIsRecyclable(false);
    }

    private void showImageViewActivity(String activityTitle, String activitySubTitle, String url) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("activityTitle", activityTitle);
        bundle.putString("activitySubTitle", activitySubTitle);
        bundle.putString("imageUrl", url);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean showReactionCreateDialog(MessageDto messageDto) {
        Log.d("message of user id", messageDto.getSender().getId());
        MessageOptionDialog messageOptionDialog = new MessageOptionDialog(context, messageDto);
        messageOptionDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), messageOptionDialog.getTag());
        return true;
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageDto message = list.get(position);
        if (message != null && message.getType().equals(MessageType.SYSTEM))
            return ITEM_SYSTEM;
        if (message != null && message.getSender() != null && message.getSender().getId().equalsIgnoreCase(user.getId()))
            return ITEM_SEND;
        return ITEM_RECEIVER;
    }

    public void updateDeletedMessage(MessageDto deletedMessage) {
        for (int i = 0; i < list.size(); i++) {
            MessageDto m = list.get(i);
            MessageDto reply = m.getReply();
            if (m.getId().equals(deletedMessage.getId())) {
                m.setContent(deletedMessage.getContent());
                m.setDeleted(true);

                Log.d("--del message find", m.toString());

                /*
                tìm holder tại vị trí message đó, set recyclable=true để cập nhật lại reaction
                sau đó set lại recyclable=false để khi cuộn dữ liệu không bị sai
                 */
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    holder.setIsRecyclable(true);
                    Activity activity = (Activity) context;
                    activity.runOnUiThread(this::notifyDataSetChanged);
                    holder.setIsRecyclable(false);
                }
            } else if (reply != null && deletedMessage.getId().equals(reply.getId())) {
                reply.setContent(deletedMessage.getContent());
                reply.setDeleted(true);
                m.setReply(reply);

                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    holder.setIsRecyclable(true);
                    Activity activity = (Activity) context;
                    activity.runOnUiThread(this::notifyDataSetChanged);
                    holder.setIsRecyclable(false);
                }
            }
        }
    }

    /**
     * holder tin nhắn gửi
     */
    static class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessage;
        TextView timeOfMessage;
        RecyclerView rcv_read_one;
        RecyclerView rcv_read_many;
        RecyclerView send_rcv_reaction;
        ImageView contentImage;
        ImageView contentVideo;
        LinearLayout messageLayout;
        LinearLayout send_message_reply_layout;
        LinearLayout send_message_reaction_layout;
        LinearLayout send_message_rcv_readby_one_many;
        TextView send_message_reply_content;
        TextView send_message_reply_name;
        RecyclerView send_message_rcv_media;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            timeOfMessage = itemView.findViewById(R.id.send_message_time);
            senderMessage = itemView.findViewById(R.id.send_message_content);
            rcv_read_one = itemView.findViewById(R.id.send_rcv_read_one);
            rcv_read_many = itemView.findViewById(R.id.send_rcv_read_many);
            send_rcv_reaction = itemView.findViewById(R.id.send_rcv_reaction);
            contentImage = itemView.findViewById(R.id.send_message_content_image);
            contentVideo = itemView.findViewById(R.id.send_message_content_video);
            messageLayout = itemView.findViewById(R.id.blank);
            send_message_rcv_media = itemView.findViewById(R.id.send_message_rcv_media);

            send_message_reply_layout = itemView.findViewById(R.id.send_message_reply_layout);
            send_message_reply_content = itemView.findViewById(R.id.send_message_reply_content);
            send_message_reply_name = itemView.findViewById(R.id.send_message_reply_name);
            send_message_reaction_layout = itemView.findViewById(R.id.send_message_reaction_layout);
            send_message_rcv_readby_one_many = itemView.findViewById(R.id.rcv_readby_one_many);

            send_message_reply_layout.setVisibility(View.GONE);
            send_message_reaction_layout.setVisibility(View.GONE);
            send_message_rcv_readby_one_many.setVisibility(View.GONE);
            senderMessage.setVisibility(View.GONE);
            timeOfMessage.setVisibility(View.GONE);
            contentVideo.setVisibility(View.GONE);
            contentImage.setVisibility(View.GONE);
            send_message_rcv_media.setVisibility(View.GONE);

            rcv_read_one.addItemDecoration(new ItemDecorator(-15));
            rcv_read_many.addItemDecoration(new ItemDecorator(-15));
            send_rcv_reaction.addItemDecoration(new ItemDecorator(-15));
        }
    }

    /**
     * holder tin nhắn nhận
     */
    static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMessage;
        TextView timeOfMessage;
        ImageView receiverImage;
        RecyclerView rcv_read_one;
        RecyclerView rcv_read_many;
        RecyclerView receiver_rcv_reaction;
        ImageView contentImage;
        ImageView contentVideo;
        LinearLayout messageLayout;
        LinearLayout receiver_message_reply_layout;
        LinearLayout receiver_message_reaction_layout;
        LinearLayout receiver_message_rcv_readby_one_many;
        TextView receiver_message_reply_content;
        TextView receiver_message_reply_name;
        RecyclerView received_message_rcv_media;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            timeOfMessage = itemView.findViewById(R.id.receiver_message_time);
            receiverMessage = itemView.findViewById(R.id.receiver_message_content);
            receiverImage = itemView.findViewById(R.id.receiver_sender_image);
            rcv_read_one = itemView.findViewById(R.id.receiver_rcv_read_one);
            rcv_read_many = itemView.findViewById(R.id.receiver_rcv_read_many);
            receiver_rcv_reaction = itemView.findViewById(R.id.receiver_rcv_reaction);
            contentImage = itemView.findViewById(R.id.receiver_message_content_image);
            contentVideo = itemView.findViewById(R.id.receiver_message_content_video);
            messageLayout = itemView.findViewById(R.id.blank);
            received_message_rcv_media = itemView.findViewById(R.id.received_message_rcv_media);

            receiver_message_reply_layout = itemView.findViewById(R.id.receiver_message_reply_layout);
            receiver_message_reply_content = itemView.findViewById(R.id.receiver_message_reply_content);
            receiver_message_reply_name = itemView.findViewById(R.id.receiver_message_reply_name);
            receiver_message_reaction_layout = itemView.findViewById(R.id.receiver_message_reaction_layout);
            receiver_message_rcv_readby_one_many = itemView.findViewById(R.id.receiver_relative_read_many);

            receiver_message_reply_layout.setVisibility(View.GONE);
            receiver_message_reaction_layout.setVisibility(View.GONE);
            receiver_message_rcv_readby_one_many.setVisibility(View.GONE);
            receiverMessage.setVisibility(View.GONE);
            timeOfMessage.setVisibility(View.GONE);
            contentVideo.setVisibility(View.GONE);
            contentImage.setVisibility(View.GONE);
            received_message_rcv_media.setVisibility(View.GONE);

            rcv_read_one.addItemDecoration(new ItemDecorator(-15));
            rcv_read_many.addItemDecoration(new ItemDecorator(-15));
            receiver_rcv_reaction.addItemDecoration(new ItemDecorator(-15));
        }
    }

    /**
     * holder tin nhắn của hệ thống
     */
    static class SystemViewHolder extends RecyclerView.ViewHolder {

        TextView system_message_time;
        TextView system_message_content;

        public SystemViewHolder(@NonNull View itemView) {
            super(itemView);
            system_message_time = itemView.findViewById(R.id.system_message_time);
            system_message_content = itemView.findViewById(R.id.system_message_content);
        }
    }

    public void updateList(List<MessageDto> messageDtos) {
        for (int i = messageDtos.size() - 1; i >= 0; i--) {
            this.list.add(0, messageDtos.get(i));
        }
        Activity activity = (Activity) context;
        activity.runOnUiThread(this::notifyDataSetChanged);
    }

    public void updateMessage(MessageDto messageDto) {
        list.add(messageDto);
        Activity activity = (Activity) context;
        activity.runOnUiThread(this::notifyDataSetChanged);
    }

    public MessageDto getLastMessage() {
        if (list != null && !list.isEmpty())
            return list.get(list.size() - 1);
        return null;
    }

}
