package com.example.chatapp.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.utils.TimeAgo;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    Context context;

    List<MessageDto> list = new ArrayList<>();
    private static final int ROW_TYPE_LOAD_EARLIER_MESSAGES = 0;
    private static final int ITEM_SEND = 1;
    private static final int ITEM_REVIEVER = 2;
    SharedPreferences sharedPreferencesUser;
    Gson gson = new Gson();
    boolean isLoadEarlierMsgs = true;
    LoadEarlierMessages mLoadEarlierMessages;

    public MessageAdapter(Context context, List<MessageDto> messageDtos) {
        this.context = context;
        this.sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        this.list = messageDtos;
        mLoadEarlierMessages = (LoadEarlierMessages) this.context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_chat_layout, parent, false);
            return new SenderViewHolder(view);
        } else if (viewType == ITEM_REVIEVER) {
            View view = LayoutInflater.from(context).inflate(R.layout.reciever_chat_layout, parent, false);
            return new RecieverViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_load_earlier_messages, parent, false);
            return new LoadEarlierMsgsViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDto messageDto = null;
        switch (getItemViewType(position)) {
            case ROW_TYPE_LOAD_EARLIER_MESSAGES:
                LoadEarlierMsgsViewHolder loadEarlierMsgsViewHolder =
                        (LoadEarlierMsgsViewHolder) holder;
                if (isLoadEarlierMsgs) {
                    loadEarlierMsgsViewHolder.btLoadEarlierMessages
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (mLoadEarlierMessages != null) {
                                        mLoadEarlierMessages.onLoadEarlierMessages();
                                    }
                                }
                            });
                } else {
                    loadEarlierMsgsViewHolder.btLoadEarlierMessages.setVisibility(View.GONE);
                }
                break;
            case ITEM_SEND:
                messageDto = list.get(position - 1);
                SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                senderViewHolder.sendermessage.setText(messageDto.getContent());
                senderViewHolder.timeofmessage.setText(TimeAgo.getTimeStamp(messageDto.getCreateAt()));
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
                }
                break;
            case ITEM_REVIEVER:
                messageDto = list.get(position - 1);
                RecieverViewHolder recieverViewHolder = (RecieverViewHolder) holder;
                if (messageDto != null) {
                    if (messageDto.getSender() != null) {
                        recieverViewHolder.sendermessage.setText(messageDto.getContent());
                        Glide.with(context).load(messageDto.getSender().getImageUrl())
                                .centerCrop().circleCrop().placeholder(R.drawable.image_placeholer)
                                .into(recieverViewHolder.senderImage);
                    }
                    recieverViewHolder.timeofmessage.setText(TimeAgo.getTimeStamp(messageDto.getCreateAt()));
                    if (messageDto.getReadbyes() != null) {
                        ReadbyAdapter readbyAdapter = new ReadbyAdapter(messageDto, context);
                        LinearLayoutManager layoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager2.setStackFromEnd(true);
                        if (messageDto.getReadbyes().size() == 1) {
                            recieverViewHolder.rcv_read_one.setLayoutManager(layoutManager2);
                            recieverViewHolder.rcv_read_one.setAdapter(readbyAdapter);
                        }
                        if (messageDto.getReadbyes().size() > 1) {
                            recieverViewHolder.rcv_read_many.setLayoutManager(layoutManager2);
                            recieverViewHolder.rcv_read_many.setAdapter(readbyAdapter);
                            if (recieverViewHolder.rcv_read_one.getAdapter() != null) {
                                ReadbyAdapter empty = new ReadbyAdapter(null, context);
                                recieverViewHolder.rcv_read_one.setAdapter(empty);
                            }
                        }
                    }
                    if (messageDto.getReactions() != null) {
                        ReactionAdapter reactionAdapter = new ReactionAdapter(messageDto, context);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        layoutManager.setStackFromEnd(true);
                        recieverViewHolder.receiver_rcv_reaction.setLayoutManager(layoutManager);
                        recieverViewHolder.receiver_rcv_reaction.setAdapter(reactionAdapter);
                    }
                }
                break;
        }
        if (messageDto != null) {
            MessageDto finalMessageDto = messageDto;
            holder.itemView.setOnLongClickListener(v -> {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.reaction_dialog_create);

                RecyclerView listView = dialog.findViewById(R.id.rcv_reaction_dialog_create);
                TextView titleOfDialog = dialog.findViewById(R.id.txt_reaction_dialog_create_title);

                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                ReactionDialogCreateAdapter arrayAdapter = new ReactionDialogCreateAdapter(finalMessageDto, context);
                listView.setLayoutManager(layoutManager);
                listView.setAdapter(arrayAdapter);

                titleOfDialog.setText("Bày tỏ cảm xúc");
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_readby_dialog);
                dialog.show();

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(lp);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        String user = sharedPreferencesUser.getString("user-info", null);
        UserSummaryDTO dto = gson.fromJson(user, UserSummaryDTO.class);
        if (position == 0)
            return ROW_TYPE_LOAD_EARLIER_MESSAGES;
        else {
            MessageDto message = list.get(position - 1);
            if (message != null && message.getSender() != null && message.getSender().getId().equalsIgnoreCase(dto.getId()))
                return ITEM_SEND;
            else
                return ITEM_REVIEVER;
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView sendermessage, timeofmessage;
        RecyclerView rcv_read_one;
        RecyclerView rcv_read_many;
        RecyclerView send_rcv_reaction;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            timeofmessage = itemView.findViewById(R.id.send_message_time);
            sendermessage = itemView.findViewById(R.id.send_message_content);
            rcv_read_one = itemView.findViewById(R.id.send_rcv_read_one);
            rcv_read_many = itemView.findViewById(R.id.send_rcv_read_many);
            send_rcv_reaction = itemView.findViewById(R.id.send_rcv_reaction);

            rcv_read_one.addItemDecoration(new ItemDecorator(-15));
            rcv_read_many.addItemDecoration(new ItemDecorator(-15));
            send_rcv_reaction.addItemDecoration(new ItemDecorator(-15));
        }
    }

    static class RecieverViewHolder extends RecyclerView.ViewHolder {

        TextView sendermessage, timeofmessage;
        ImageView senderImage;
        RecyclerView rcv_read_one;
        RecyclerView rcv_read_many;
        RecyclerView receiver_rcv_reaction;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            timeofmessage = itemView.findViewById(R.id.receiver_message_time);
            sendermessage = itemView.findViewById(R.id.receiver_message_content);
            senderImage = itemView.findViewById(R.id.receiver_sender_image);
            rcv_read_one = itemView.findViewById(R.id.receiver_rcv_read_one);
            rcv_read_many = itemView.findViewById(R.id.receiver_rcv_read_many);
            receiver_rcv_reaction = itemView.findViewById(R.id.receiver_rcv_reaction);

            rcv_read_one.addItemDecoration(new ItemDecorator(-15));
            rcv_read_many.addItemDecoration(new ItemDecorator(-15));
            receiver_rcv_reaction.addItemDecoration(new ItemDecorator(-15));
        }
    }

    static class LoadEarlierMsgsViewHolder extends RecyclerView.ViewHolder {

        Button btLoadEarlierMessages;

        public LoadEarlierMsgsViewHolder(@NonNull View itemView) {
            super(itemView);
            btLoadEarlierMessages = itemView.findViewById(R.id.btLoadEarlierMessages);
        }
    }

    public void updateList(List<MessageDto> messageDtos) {
        for (int i = messageDtos.size() - 1; i >= 0; i--) {
            this.list.add(0, messageDtos.get(i));
        }
        notifyDataSetChanged();
    }

    public interface LoadEarlierMessages {
        void onLoadEarlierMessages();
    }

    public void setLoadEarlierMsgs(boolean isLoadEarlierMsgs) {
        this.isLoadEarlierMsgs = isLoadEarlierMsgs;
    }

    public void updateMessage(MessageDto messageDto) {
        list.add(messageDto);
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
