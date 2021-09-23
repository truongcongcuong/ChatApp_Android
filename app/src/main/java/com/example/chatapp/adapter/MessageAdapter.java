package com.example.chatapp.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReactionReceiver;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.ReadByReceiver;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.Reaction;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final List<MessageDto> list;
    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVER = 2;
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

    // cap nhat reaction realtime
    public void updateReactionToMessage(ReactionReceiver receiver) {
        for (MessageDto m : list) {
            if (m.getId().equals(receiver.getMessageId())) {
                List<Reaction> reactions = m.getReactions();
                if (reactions == null)
                    reactions = new ArrayList<>();
                Reaction reaction = new Reaction();
                reaction.setType(receiver.getType());
                reaction.setReactByUserId(receiver.getReactByUser().getId());
                reactions.add(reaction);

                m.setReactions(reactions);

                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(list.indexOf(m));
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
        for (MessageDto m : list) {
            // cập nhật readby cho new message
            if (m.getId().equals(readByReceiver.getMessageId())) {
                List<ReadByDto> readbyes = m.getReadbyes();
                if (readbyes == null)
                    readbyes = new ArrayList<>();
                ReadByDto readByDto = new ReadByDto();
                readByDto.setReadAt(readByReceiver.getReadAt());
                readByDto.setReadByUser(readByReceiver.getReadByUser());
                if (!readbyes.contains(readByDto))
                    readbyes.add(readByDto);

                m.setReadbyes(readbyes);
                Log.i("message readed", m.toString());
            }
            // cập nhật readby cho tin nhắn cũ
            if (m.getId().equals(readByReceiver.getOldMessageId()) && !m.getId().equals(readByReceiver.getMessageId())) {
                List<ReadByDto> readbyes = m.getReadbyes();
                if (readbyes != null) {
                    Log.i("------read before", m.getReadbyes().toString());
                    readbyes.removeIf(x -> x.getReadByUser().getId().equals(readByReceiver.getReadByUser().getId()));
                    readbyes.removeIf(x -> x.getReadByUser().getId().equals(user.getId()));
                }

                m.setReadbyes(readbyes);
                Log.i("------read after", m.getReadbyes().toString());
            }
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(list.indexOf(m));
            if (holder != null) {
                holder.setIsRecyclable(true);
                Activity activity = (Activity) context;
                activity.runOnUiThread(this::notifyDataSetChanged);
                holder.setIsRecyclable(false);
            }
        }
    }


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
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciever_chat_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDto messageDto = list.get(position);
        if (messageDto != null) {
            // khi cuộn nhanh thì nội dung của item sẽ cập nhật nhiều lần dẫn đến sai
            // set recyable=false để khi cuộn không cập nhật item
            // khi nào cần cập nhật thì set=true
            holder.setIsRecyclable(false);
            switch (getItemViewType(position)) {
                case ITEM_SEND:
                    SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                    switch (messageDto.getType()) {
                        case "TEXT":
                            senderViewHolder.senderMessage.setText(messageDto.getContent());
                            break;
                        case "IMAGE":
                            Glide.with(context).load(messageDto.getContent())
                                    .into(senderViewHolder.contentImage);
                            break;
                        case "VIDEO":
                            ViewGroup.LayoutParams params = senderViewHolder.contentVideo.getLayoutParams();
                            params.width = 1080;
                            params.height = 720;
                            senderViewHolder.contentVideo.setLayoutParams(params);
                            senderViewHolder.contentVideo.requestLayout();

                            MediaController mediaController = new MediaController(context);
                            mediaController.setMediaPlayer(senderViewHolder.contentVideo);
                            mediaController.setAnchorView(senderViewHolder.contentVideo);

                            Uri uri = Uri.parse(messageDto.getContent());
                            senderViewHolder.contentVideo.setVideoURI(uri);
                            senderViewHolder.contentVideo.setMediaController(mediaController);
//                            senderViewHolder.contentVideo.requestFocus();
//                            senderViewHolder.contentVideo.setOnPreparedListener(mp -> {
//                            mp.setLooping(true);
//                            senderViewHolder.contentVideo.start();
//                            });

                            break;
                    }
                    senderViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                    if (messageDto.getReadbyes() != null) {
                        ReadbyAdapter readbyAdapter = new ReadbyAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);
                        if (messageDto.getReadbyes().size() == 1) {
                            senderViewHolder.rcv_read_one.setLayoutManager(layoutManager);
                            senderViewHolder.rcv_read_one.setAdapter(readbyAdapter);
                        }
                        if (messageDto.getReadbyes().size() > 1) {
                            senderViewHolder.rcv_read_many.setLayoutManager(layoutManager);
                            senderViewHolder.rcv_read_many.setAdapter(readbyAdapter);
                            if (senderViewHolder.rcv_read_one.getAdapter() != null) {
                                ReadbyAdapter empty = new ReadbyAdapter(null, context);
                                senderViewHolder.rcv_read_one.setAdapter(empty);
                            }
                        }
                    }
                    if (messageDto.getReactions() != null) {
                        ReactionAdapter reactionAdapter = new ReactionAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);
                        senderViewHolder.send_rcv_reaction.setLayoutManager(layoutManager);
                        senderViewHolder.send_rcv_reaction.setAdapter(reactionAdapter);

                        if (!messageDto.getReactions().isEmpty()) {
                            LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(senderViewHolder.send_rcv_reaction.getLayoutParams());
                            marginLayoutParams.setMargins(25, 0, 20, 0);
                            senderViewHolder.send_rcv_reaction.setLayoutParams(marginLayoutParams);
                            senderViewHolder.send_rcv_reaction.requestLayout();
                        }
                    }
                    senderViewHolder.messageLayout.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                    break;
                case ITEM_RECEIVER:
                    ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                    if (messageDto.getSender() != null) {
                        Glide.with(context).load(messageDto.getSender().getImageUrl())
                                .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                                .into(receiverViewHolder.senderImage);
                    }
                    switch (messageDto.getType()) {
                        case "TEXT":
                            receiverViewHolder.senderMessage.setText(messageDto.getContent());
                            break;
                        case "IMAGE":
                            Glide.with(context).load(messageDto.getContent())
                                    .into(receiverViewHolder.contentImage);
                            break;
                        case "VIDEO":
                            ViewGroup.LayoutParams params = receiverViewHolder.contentVideo.getLayoutParams();
                            params.width = 1080;
                            params.height = 720;
                            receiverViewHolder.contentVideo.setLayoutParams(params);
                            receiverViewHolder.contentVideo.requestLayout();

                            MediaController mediaController = new MediaController(context);
                            mediaController.setMediaPlayer(receiverViewHolder.contentVideo);
                            mediaController.setAnchorView(receiverViewHolder.contentVideo);

                            Uri uri = Uri.parse(messageDto.getContent());
                            receiverViewHolder.contentVideo.setVideoURI(uri);
                            receiverViewHolder.contentVideo.setMediaController(mediaController);
//                            senderViewHolder.contentVideo.requestFocus();
//                            receiverViewHolder.contentVideo.setOnPreparedListener(mp -> {
//                            mp.setLooping(true);
//                            senderViewHolder.contentVideo.start();
//                            });

                            break;
                    }
                    receiverViewHolder.timeOfMessage.setText(messageDto.getCreateAt().replaceAll("-", "/"));
                    if (messageDto.getReadbyes() != null) {
                        ReadbyAdapter readbyAdapter = new ReadbyAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);
                        if (messageDto.getReadbyes().size() == 1) {
                            receiverViewHolder.rcv_read_one.setLayoutManager(layoutManager);
                            receiverViewHolder.rcv_read_one.setAdapter(readbyAdapter);
                        }
                        if (messageDto.getReadbyes().size() > 1) {
                            receiverViewHolder.rcv_read_many.setLayoutManager(layoutManager);
                            receiverViewHolder.rcv_read_many.setAdapter(readbyAdapter);
                            if (receiverViewHolder.rcv_read_one.getAdapter() != null) {
                                ReadbyAdapter empty = new ReadbyAdapter(null, context);
                                receiverViewHolder.rcv_read_one.setAdapter(empty);
                            }
                        }
                    }
                    if (messageDto.getReactions() != null) {
                        ReactionAdapter reactionAdapter = new ReactionAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);
                        receiverViewHolder.receiver_rcv_reaction.setLayoutManager(layoutManager);
                        receiverViewHolder.receiver_rcv_reaction.setAdapter(reactionAdapter);

                        if (!messageDto.getReactions().isEmpty()) {
                            LinearLayout.LayoutParams marginLayoutParams = new LinearLayout.LayoutParams(receiverViewHolder.receiver_rcv_reaction.getLayoutParams());
                            marginLayoutParams.setMargins(25, 0, 20, 0);
                            receiverViewHolder.receiver_rcv_reaction.setLayoutParams(marginLayoutParams);
                            receiverViewHolder.receiver_rcv_reaction.requestLayout();
                        }
                    }
                    receiverViewHolder.messageLayout.setOnLongClickListener(v -> showReactionCreateDialog(messageDto));
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean showReactionCreateDialog(MessageDto messageDto) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.reaction_dialog_create);

        RecyclerView rcvReaction = dialog.findViewById(R.id.rcv_reaction_dialog_create);
        TextView titleOfDialog = dialog.findViewById(R.id.txt_reaction_dialog_create_title);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        ReactionDialogCreateAdapter arrayAdapter = new ReactionDialogCreateAdapter(messageDto, context);

        rcvReaction.setLayoutManager(layoutManager);
        rcvReaction.setAdapter(arrayAdapter);

        titleOfDialog.setText("Bày tỏ cảm xúc");
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_readby_dialog);
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
        if (message != null && message.getSender() != null && message.getSender().getId().equalsIgnoreCase(user.getId()))
            return ITEM_SEND;
        return ITEM_RECEIVER;
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessage;
        TextView timeOfMessage;
        RecyclerView rcv_read_one;
        RecyclerView rcv_read_many;
        RecyclerView send_rcv_reaction;
        ImageView contentImage;
        VideoView contentVideo;
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

            ViewGroup.LayoutParams params = contentVideo.getLayoutParams();
            params.width = 0;
            contentVideo.setLayoutParams(params);
            contentVideo.requestLayout();

        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessage;
        TextView timeOfMessage;
        ImageView senderImage;
        RecyclerView rcv_read_one;
        RecyclerView rcv_read_many;
        RecyclerView receiver_rcv_reaction;
        ImageView contentImage;
        VideoView contentVideo;
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

            ViewGroup.LayoutParams params = contentVideo.getLayoutParams();
            params.width = 0;
            contentVideo.setLayoutParams(params);
            contentVideo.requestLayout();

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
