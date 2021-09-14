package com.example.chatapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.LogPrinter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageDto messageDto;
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
                messageDto = list.get(position-1);
                SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                senderViewHolder.sendermessage.setText(messageDto.getContent());
                senderViewHolder.timeofmessage.setText(TimeAgo.getTimeStamp(messageDto.getCreateAt()));
                break;
            case ITEM_REVIEVER:
                messageDto = list.get(position-1);
                Log.i("----", messageDto.getSender().getId());
                RecieverViewHolder recieverViewHolder = (RecieverViewHolder) holder;
                recieverViewHolder.sendermessage.setText(messageDto.getSender().getId() + "_" + messageDto.getContent());
                recieverViewHolder.timeofmessage.setText(TimeAgo.getTimeStamp(messageDto.getCreateAt()));

//                Bitmap bitmap = null;
//                try {
//                    URL urlOnl = new URL(messageDto.getSender().getImageUrl());
//                    bitmap = BitmapFactory.decodeStream(urlOnl.openConnection().getInputStream());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                CroppedDrawable cd = new CroppedDrawable(bitmap);
//                recieverViewHolder.senderImage.setImageDrawable(cd);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        String user = sharedPreferencesUser.getString("user-info", null);
        UserSummaryDTO dto = gson.fromJson(user, UserSummaryDTO.class);
        if (position == 0)
            return ROW_TYPE_LOAD_EARLIER_MESSAGES;
        else {
            MessageDto message = list.get(position - 1);
            if (message.getSender().getId().equalsIgnoreCase(dto.getId()))
                return ITEM_SEND;
            else
                return ITEM_REVIEVER;
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView sendermessage, timeofmessage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            timeofmessage = itemView.findViewById(R.id.timeofmessage);
            sendermessage = itemView.findViewById(R.id.sendermessage);

        }
    }

    static class RecieverViewHolder extends RecyclerView.ViewHolder {

        TextView sendermessage, timeofmessage;
        ImageView senderImage;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            timeofmessage = itemView.findViewById(R.id.timeofmessage);
            sendermessage = itemView.findViewById(R.id.sendermessage);
            senderImage = itemView.findViewById(R.id.senderImage);

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

        for (int i = messageDtos.size()-1; i >=0; i--) {
            this.list.add(0, messageDtos.get(i));
        }
//        list.addAll(messageDtos);
        notifyDataSetChanged();
    }

    public interface LoadEarlierMessages {
        void onLoadEarlierMessages();
    }

    public void setLoadEarlierMsgs(boolean isLoadEarlierMsgs) {
        this.isLoadEarlierMsgs = isLoadEarlierMsgs;
    }

    public void updateMessage(MessageDto messageDto){
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
