package com.example.chatapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter {


    Context context ;

    List<MessageDto> list = new ArrayList<>();
    private static final int ROW_TYPE_LOAD_EARLIER_MESSAGES = 0;
    private static final int ITEM_SEND =1;
    private static final int ITEM_REVIEVER = 2;
    SharedPreferences sharedPreferencesUser ;
    Gson gson = new Gson();
    boolean isLoadEarlierMsgs = true;
    LoadEarlierMessages mLoadEarlierMessages;


    public MessageAdapter(Context context, List<MessageDto> messageDtos) {
        this.context = context;
        this.sharedPreferencesUser = context.getSharedPreferences("user",Context.MODE_PRIVATE);
        this.list = messageDtos;
        Collections.reverse(this.list);
        mLoadEarlierMessages = (LoadEarlierMessages) this.context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.sender_chat_layout,parent,false);
            return new SenderViewHolder(view);
        }
        else if(viewType == ITEM_REVIEVER){
            View view = LayoutInflater.from(context).inflate(R.layout.reciever_chat_layout,parent,false);
            return new RecieverViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_load_earlier_messages,parent,false);
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
                senderViewHolder.timeofmessage.setText("");
                break;
            case ITEM_REVIEVER:
                messageDto = list.get(position-1);
                RecieverViewHolder recieverViewHolder = (RecieverViewHolder) holder;
                recieverViewHolder.sendermessage.setText(messageDto.getContent());
                recieverViewHolder.timeofmessage.setText("");
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
        UserSummaryDTO dto = gson.fromJson(user,UserSummaryDTO.class);
        if(position ==0)
            return ROW_TYPE_LOAD_EARLIER_MESSAGES;
        else {
            MessageDto message = list.get(position-1);
            if (message.getSender().getId().equalsIgnoreCase(dto.getId()))
                return ITEM_SEND;
            else
                return ITEM_REVIEVER;
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder{

        TextView  sendermessage,timeofmessage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            timeofmessage = itemView.findViewById(R.id.timeofmessage);
            sendermessage = itemView.findViewById(R.id.sendermessage);

        }
    }

    class RecieverViewHolder extends RecyclerView.ViewHolder{

        TextView  sendermessage,timeofmessage;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            timeofmessage = itemView.findViewById(R.id.timeofmessage);
            sendermessage = itemView.findViewById(R.id.sendermessage);

        }

    }

    class LoadEarlierMsgsViewHolder extends RecyclerView.ViewHolder {

        Button btLoadEarlierMessages;

        public LoadEarlierMsgsViewHolder(@NonNull View itemView) {
            super(itemView);
            btLoadEarlierMessages = itemView.findViewById(R.id.btLoadEarlierMessages);
        }
    }


 public void updateList(List<MessageDto> messageDtos) {

     for (int i = 0;i<messageDtos.size();i++){
         this.list.add(0,messageDtos.get(i));
     }
        notifyDataSetChanged();
   }

    public interface LoadEarlierMessages {
        void onLoadEarlierMessages();
    }
    public void setLoadEarlierMsgs(boolean isLoadEarlierMsgs) {
        this.isLoadEarlierMsgs = isLoadEarlierMsgs;
    }
}
