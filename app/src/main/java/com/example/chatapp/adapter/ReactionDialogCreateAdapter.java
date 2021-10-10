package com.example.chatapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.cons.WebSocketClient;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReactionSend;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.enumvalue.ReactionType;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.Json;

public class ReactionDialogCreateAdapter extends RecyclerView.Adapter<ReactionDialogCreateAdapter.ViewHolder> {

    private final List<Integer> resources;
    private final List<ReactionType> types;
    private final Context context;
    private MessageDto messageDto;
    private final UserSummaryDTO user;
    private final Gson gson;
    private Dialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ReactionDialogCreateAdapter(MessageDto messageDto, Context context, Dialog dialog) {
        this.context = context;
        this.messageDto = messageDto;
        this.dialog = dialog;

        resources = new ArrayList<>();
        types = new ArrayList<>();

        resources.add(R.drawable.ic_reaction_haha);
        resources.add(R.drawable.ic_reaction_sad);
        resources.add(R.drawable.ic_reaction_love);
        resources.add(R.drawable.ic_reaction_wow);
        resources.add(R.drawable.ic_reaction_angry);
        resources.add(R.drawable.ic_reaction_like);

        types.add(ReactionType.HAHA);
        types.add(ReactionType.SAD);
        types.add(ReactionType.LOVE);
        types.add(ReactionType.WOW);
        types.add(ReactionType.ANGRY);
        types.add(ReactionType.LIKE);

        gson = new Gson();
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesToken.getString("user-info", null), UserSummaryDTO.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_reaction_create_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        holder.image_reaction_item.setImageResource(resources.get(position));

        ReactionSend reactionSend = new ReactionSend();
        reactionSend.setType(types.get(position));
        reactionSend.setMessageId(messageDto.getId());
        reactionSend.setRoomId(messageDto.getRoomId());
        reactionSend.setUserId(user.getId());

        /*
        nếu click thì gửi reaction lên server
         */
        holder.itemView.setOnClickListener(v -> {
            WebSocketClient.getInstance().getStompClient()
                    .send("/app/reaction", Json.encode(reactionSend))
                    .subscribe(() -> {

                    });
            dialog.cancel();
        });
    }

    @Override
    public int getItemCount() {
        if (resources == null)
            return 0;
        return resources.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image_reaction_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_reaction_item = itemView.findViewById(R.id.image_reaction_dialog_create_item);
        }
    }

}
