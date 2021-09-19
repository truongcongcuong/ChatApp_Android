package com.example.chatapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.cons.WebsocketClient;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.entity.Reaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.Json;

public class ReactionDialogCreateAdapter extends RecyclerView.Adapter<ReactionDialogCreateAdapter.ViewHolder> {

    private List<Integer> resources = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private final Context context;
    private MessageDto messageDto;
    private SharedPreferences sharedPreferencesToken;
    private Map<Integer, String> map = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ReactionDialogCreateAdapter(MessageDto messageDto, Context context) {
        this.messageDto = messageDto;
        resources.add(R.drawable.ic_reaction_haha);
        resources.add(R.drawable.ic_reaction_sad);
        resources.add(R.drawable.ic_reaction_love);
        resources.add(R.drawable.ic_reaction_wow);
        resources.add(R.drawable.ic_reaction_angry);
        resources.add(R.drawable.ic_reaction_like);

        types.add("HAHA");
        types.add("SAD");
        types.add("LOVE");
        types.add("WOW");
        types.add("ANGRY");
        types.add("LIKE");

        this.context = context;
        sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reaction_dialog_create_line_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        holder.image_reaction_item.setImageResource(resources.get(position));

        Reaction reaction = new Reaction();
        reaction.setType(types.get(position));

        holder.itemView.setOnClickListener(v -> {
            WebsocketClient.getInstance().getStompClient()
                    .send("/app/reaction", Json.encode(reaction))
                    .subscribe(() -> {

                    });
            Toast.makeText(context, "sent " + types.get(position) + " to server", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image_reaction_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_reaction_item = itemView.findViewById(R.id.image_reaction_dialog_create_item);
        }
    }

}
