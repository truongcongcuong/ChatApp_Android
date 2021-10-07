package com.example.chatapp.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReactionReceiver;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.ReadByReceiver;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.Reaction;
import com.example.chatapp.enumvalue.MessageType;
import com.example.chatapp.ui.ViewImageActivity;
import com.example.chatapp.ui.ViewVideoActivity;
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
    private final Gson gson;
    private UserSummaryDTO user;
    private RecyclerView recyclerView;

    public MessageAdapter(Context context, List<MessageDto> messageDtos) {
        this.context = context;
        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        user = gson.fromJson(userJson, UserSummaryDTO.class);
        list = messageDtos;
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
            View view = LayoutInflater.from(context).inflate(R.layout.sender_chat_layout, parent, false);
            return new SenderViewHolder(view);
        } else if (viewType == ITEM_RECEIVER) {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_chat_layout, parent, false);
            return new ReceiverViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.system_message_chat_layout, parent, false);
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
                        senderViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                    } else {
                        if (messageDto.getSender() != null) {
                            if (list.get(position + 1).getSender() == null) {
                                senderViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                            } else if (!messageDto.getSender().getId().equals(list.get(position + 1).getSender().getId())) {
                                senderViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                            } else {
                                senderViewHolder.timeOfMessage.setHeight(0);
                            }
                        }
                    }
                    switch (messageDto.getType()) {
                        case TEXT:
                            senderViewHolder.senderMessage.setText(messageDto.getContent());
                            // set padding bottom cho text message
                            senderViewHolder.senderMessage.setPadding(
                                    senderViewHolder.senderMessage.getPaddingLeft(),
                                    senderViewHolder.senderMessage.getPaddingTop(),
                                    senderViewHolder.senderMessage.getPaddingRight(),
                                    10
                            );
                            // set chiều rộng tối thiếu cho textview
                            senderViewHolder.senderMessage.setMinEms(2);
                            // set chiều rộng cho videoview =0 để wrapcontent
//                            setVideoWidthToZero(senderViewHolder.contentVideo);
                            break;
                        case IMAGE:
                            Glide.with(context).load(messageDto.getContent())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(48)))
                                    .into(senderViewHolder.contentImage);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            imageParams.setMargins(5, 10, 5, 10);
                            senderViewHolder.contentImage.setLayoutParams(imageParams);
                            // set textview message content height=0
                            senderViewHolder.senderMessage.setHeight(0);

                            senderViewHolder.contentImage.setOnClickListener(v -> {
                                Intent intent = new Intent(context, ViewImageActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("message", messageDto);
                                intent.putExtras(bundle);
                                context.startActivity(intent);
                            });
                            // set chiều rộng cho videoview =0 để wrapcontent
//                            setVideoWidthToZero(senderViewHolder.contentVideo);
                            senderViewHolder.contentImage.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                            break;
                        case VIDEO:
                            senderViewHolder.contentVideo.setBackgroundResource(R.drawable.background_video);
                            senderViewHolder.contentVideo.setOnClickListener(v -> {
                                Intent intent = new Intent(context, ViewVideoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("message", messageDto);
                                intent.putExtras(bundle);
                                context.startActivity(intent);
                            });
                            senderViewHolder.senderMessage.setHeight(0);
                            LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            videoParams.setMargins(5, 10, 5, 10);
                            senderViewHolder.contentVideo.setLayoutParams(videoParams);
                            senderViewHolder.contentVideo.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));

                            /*((Activity) context).runOnUiThread(() -> {
                                Uri uri = Uri.parse(messageDto.getContent());
                                senderViewHolder.contentVideo.setVideoURI(uri);
                                senderViewHolder.senderMessage.setHeight(0);

                                LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                videoParams.setMargins(5, 30, 5, 20);
                                senderViewHolder.contentVideo.setLayoutParams(videoParams);

                                senderViewHolder.contentVideo.setOnPreparedListener(mp -> {
                                    senderViewHolder.contentVideo.seekTo(1);
                                    mp.setLooping(true);
                                    mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
                                        MediaController mediaController = new MediaController(context);
                                        senderViewHolder.contentVideo.setMediaController(mediaController);
                                        mediaController.setAnchorView(senderViewHolder.contentVideo);
                                    });
                                });
                                senderViewHolder.contentVideo.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                            });*/
                            break;
                    }
                    // hiện danh sách người đã xem tin nhắn
                    if (messageDto.getReadbyes() != null) {
                        messageDto.getReadbyes().removeIf(x -> x.getReadByUser().getId().equals(user.getId()));
                        ReadbyAdapter readbyAdapter = new ReadbyAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);

                        // nhiều người đã xem tin nhắn
                        if (messageDto.getReadbyes().size() > 1) {
                            if (senderViewHolder.rcv_read_many.getLayoutManager() == null)
                                senderViewHolder.rcv_read_many.setLayoutManager(layoutManager);
                            senderViewHolder.rcv_read_many.setAdapter(readbyAdapter);
                            LinearLayout.LayoutParams rcv_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            rcv_params.bottomMargin = 10;
                            senderViewHolder.rcv_read_many.setLayoutParams(rcv_params);
                        }
                        // chỉ có một người đã xem tin nhắn
                        if (messageDto.getReadbyes().size() == 1) {
                            if (senderViewHolder.rcv_read_one.getLayoutManager() == null)
                                senderViewHolder.rcv_read_one.setLayoutManager(layoutManager);
                            senderViewHolder.rcv_read_one.setAdapter(readbyAdapter);
                        }
                    }
                    // hiện danh sách cảm xúc
                    if (messageDto.getReactions() != null) {
                        ReactionAdapter reactionAdapter = new ReactionAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//                        layoutManager.setStackFromEnd(true);

                        if (senderViewHolder.send_rcv_reaction.getLayoutManager() == null)
                            senderViewHolder.send_rcv_reaction.setLayoutManager(layoutManager);
                        senderViewHolder.send_rcv_reaction.setAdapter(reactionAdapter);

                        /*
                        nếu danh sách reaction khác rỗng thì set margin left và right
                         */
                        if (!messageDto.getReactions().isEmpty()) {
                            LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(senderViewHolder.send_rcv_reaction.getLayoutParams());
                            marginLayoutParams.setMargins(25, 0, 0, 0);
                            senderViewHolder.send_rcv_reaction.setLayoutParams(marginLayoutParams);
                            senderViewHolder.send_rcv_reaction.requestLayout();
                        }
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
                                    .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                                    .into(receiverViewHolder.senderImage);
                            receiverViewHolder.senderImage.setBackgroundResource(R.drawable.border_for_circle_image);
                            receiverViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                        } else {
                            if (messageDto.getSender() != null) {
                                if (list.get(position + 1).getSender() == null) {
                                    Glide.with(context).load(messageDto.getSender().getImageUrl())
                                            .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                                            .into(receiverViewHolder.senderImage);
                                    receiverViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));

                                } else if (!messageDto.getSender().getId().equals(list.get(position + 1).getSender().getId())) {
                                    Glide.with(context).load(messageDto.getSender().getImageUrl())
                                            .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                                            .into(receiverViewHolder.senderImage);
                                    receiverViewHolder.senderImage.setBackgroundResource(R.drawable.border_for_circle_image);
                                    receiverViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));

                                } else {
                                    receiverViewHolder.timeOfMessage.setHeight(0);
                                }
                            }
                        }
                    }
                    switch (messageDto.getType()) {
                        case TEXT:
                            receiverViewHolder.senderMessage.setText(messageDto.getContent());
                            receiverViewHolder.senderMessage.setPadding(
                                    receiverViewHolder.senderMessage.getPaddingLeft(),
                                    receiverViewHolder.senderMessage.getPaddingTop(),
                                    receiverViewHolder.senderMessage.getPaddingRight(),
                                    10
                            );
                            receiverViewHolder.senderMessage.setMinEms(2);
                            // set chiều rộng cho videoview =0 để wrapcontent
//                            setVideoWidthToZero(receiverViewHolder.contentVideo);
                            break;
                        case IMAGE:
                            Glide.with(context).load(messageDto.getContent())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(48)))
                                    .into(receiverViewHolder.contentImage);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            imageParams.setMargins(5, 10, 5, 10);
                            receiverViewHolder.contentImage.setLayoutParams(imageParams);
                            receiverViewHolder.senderMessage.setHeight(0);

                            receiverViewHolder.contentImage.setOnClickListener(v -> {
                                Intent intent = new Intent(context, ViewImageActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("message", messageDto);
                                intent.putExtras(bundle);
                                context.startActivity(intent);
                            });
                            // set chiều rộng cho videoview =0 để wrapcontent
//                            setVideoWidthToZero(receiverViewHolder.contentVideo);
                            receiverViewHolder.contentImage.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                            break;
                        case VIDEO:
                            receiverViewHolder.contentVideo.setBackgroundResource(R.drawable.background_video);
                            receiverViewHolder.contentVideo.setOnClickListener(v -> {
                                Intent intent = new Intent(context, ViewVideoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("message", messageDto);
                                intent.putExtras(bundle);
                                context.startActivity(intent);
                            });
                            receiverViewHolder.senderMessage.setHeight(0);
                            LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            videoParams.setMargins(5, 10, 5, 10);
                            receiverViewHolder.contentVideo.setLayoutParams(videoParams);
                            receiverViewHolder.contentVideo.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));

                            /*((Activity) context).runOnUiThread(() -> {
                                Uri uri = Uri.parse(messageDto.getContent());
//                                receiverViewHolder.contentVideo.setVideoURI(uri);
                                receiverViewHolder.senderMessage.setHeight(0);

                                LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                videoParams.setMargins(5, 30, 5, 20);
                                receiverViewHolder.contentVideo.setLayoutParams(videoParams);

                                receiverViewHolder.contentVideo.setOnPreparedListener(mp -> {
                                    receiverViewHolder.contentVideo.seekTo(1);
                                    mp.setLooping(true);
                                    mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
                                        MediaController mediaController = new MediaController(context);
                                        receiverViewHolder.contentVideo.setMediaController(mediaController);
                                        mediaController.setAnchorView(receiverViewHolder.contentVideo);
                                    });
                                });
                                receiverViewHolder.contentVideo.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                            });*/
                            break;
                    }
                    // hiện danh sách những người đã xem tin nhắn
                    if (messageDto.getReadbyes() != null) {
                        messageDto.getReadbyes().removeIf(x -> x.getReadByUser().getId().equals(user.getId()));
                        ReadbyAdapter readbyAdapter = new ReadbyAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);

                        // nhiều người đã xem tin nhắn
                        if (messageDto.getReadbyes().size() > 1) {
                            if (receiverViewHolder.rcv_read_many.getLayoutManager() == null)
                                receiverViewHolder.rcv_read_many.setLayoutManager(layoutManager);
                            receiverViewHolder.rcv_read_many.setAdapter(readbyAdapter);
                            LinearLayout.LayoutParams rcv_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            rcv_params.bottomMargin = 10;
                            receiverViewHolder.rcv_read_many.setLayoutParams(rcv_params);
                        }
                        // chỉ có một người đã xem tin nhắn
                        if (messageDto.getReadbyes().size() == 1) {
                            if (receiverViewHolder.rcv_read_one.getLayoutManager() == null)
                                receiverViewHolder.rcv_read_one.setLayoutManager(layoutManager);
                            receiverViewHolder.rcv_read_one.setAdapter(readbyAdapter);
                        }
                    }
                    // hiện danh sách cảm xúc
                    if (messageDto.getReactions() != null) {
                        ReactionAdapter reactionAdapter = new ReactionAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//                        layoutManager.setStackFromEnd(true);

                        if (receiverViewHolder.receiver_rcv_reaction.getLayoutManager() == null)
                            receiverViewHolder.receiver_rcv_reaction.setLayoutManager(layoutManager);
                        receiverViewHolder.receiver_rcv_reaction.setAdapter(reactionAdapter);

                        if (!messageDto.getReactions().isEmpty()) {
                            LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(receiverViewHolder.receiver_rcv_reaction.getLayoutParams());
                            marginLayoutParams.setMargins(25, 0, 0, 0);
                            receiverViewHolder.receiver_rcv_reaction.setLayoutParams(marginLayoutParams);
                            receiverViewHolder.receiver_rcv_reaction.requestLayout();
                        }
                    }
                    receiverViewHolder.messageLayout.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean showReactionCreateDialog(MessageDto messageDto) {
        Log.d("message of userid", messageDto.getSender().getId());
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.reaction_dialog_create);

        RecyclerView rcvReaction = dialog.findViewById(R.id.rcv_reaction_dialog_create);
        TextView titleOfDialog = dialog.findViewById(R.id.txt_reaction_dialog_create_title);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        ReactionDialogCreateAdapter arrayAdapter = new ReactionDialogCreateAdapter(messageDto, context);

        rcvReaction.setLayoutManager(layoutManager);
        rcvReaction.setAdapter(arrayAdapter);

        titleOfDialog.setText("Bày tỏ cảm xúc");
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_corner_white);
        dialog.show();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(dialog.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        dialog.getWindow().setAttributes(lp);
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

            rcv_read_one.addItemDecoration(new ItemDecorator(-15));
            rcv_read_many.addItemDecoration(new ItemDecorator(-15));
            send_rcv_reaction.addItemDecoration(new ItemDecorator(-15));
        }
    }

    /**
     * holder tin nhắn nhận
     */
    static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessage;
        TextView timeOfMessage;
        ImageView senderImage;
        RecyclerView rcv_read_one;
        RecyclerView rcv_read_many;
        RecyclerView receiver_rcv_reaction;
        ImageView contentImage;
        ImageView contentVideo;
        LinearLayout messageLayout;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            timeOfMessage = itemView.findViewById(R.id.receiver_message_time);
            senderMessage = itemView.findViewById(R.id.receiver_message_content);
            senderImage = itemView.findViewById(R.id.receiver_sender_image);
            rcv_read_one = itemView.findViewById(R.id.receiver_rcv_read_one);
            rcv_read_many = itemView.findViewById(R.id.receiver_rcv_read_many);
            receiver_rcv_reaction = itemView.findViewById(R.id.receiver_rcv_reaction);
            contentImage = itemView.findViewById(R.id.receiver_message_content_image);
            contentVideo = itemView.findViewById(R.id.receiver_message_content_video);
            messageLayout = itemView.findViewById(R.id.blank);

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

/*    private void setVideoWidthToZero(VideoView videoView) {
        ViewGroup.LayoutParams params = videoView.getLayoutParams();
        params.width = 0;
        videoView.setLayoutParams(params);
        videoView.requestLayout();
    }*/

}
