package com.example.chatapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReactionDto;
import com.example.chatapp.entity.Reaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ViewHolder> {

    private List<Reaction> list;
    private final Context context;
    private MessageDto messageDto;
    private final String token;
    private final int max = 3;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ReactionAdapter(MessageDto messageDto, Context context) {
        this.messageDto = messageDto;
        if (messageDto != null)
            this.list = new ArrayList<>(new HashSet<>(messageDto.getReactions()));
        else
            this.list = new ArrayList<>();
        this.context = context;
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_reaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        holder.setIsRecyclable(false);

        if (!list.isEmpty() && position < list.size()) {
            Reaction reaction = list.get(position);
            if (position < max) {
                bindData(holder, reaction);
            }

            if (position == 0) {
                int remain = list.size() - max;
                bindData(holder, reaction);
                if (remain > 0) {
                    holder.image_reaction_item.setAlpha(0.3f);
                    holder.txt_reaction_more.setText(String.format("+%d", remain));
                    holder.txt_reaction_more.setPadding(
                            5,
                            holder.txt_reaction_more.getPaddingTop(),
                            holder.txt_reaction_more.getPaddingRight(),
                            holder.txt_reaction_more.getPaddingBottom()
                    );
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            StringRequest request = new StringRequest(Request.Method.GET, Constant.API_MESSAGE + "react/" + messageDto.getId(),
                    response -> {
                        try {
                            String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                            Type listType = new TypeToken<List<ReactionDto>>() {
                            }.getType();
                            List<ReactionDto> reactionDtos = new Gson().fromJson(res, listType);

                            showDialogListReaction(reactionDtos);

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.i("get reaction error", error.toString())) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(request);
        });
    }

    private void bindData(@NonNull ViewHolder holder, Reaction reaction) {
        holder.image_reaction_item.setBackgroundResource(R.drawable.background_circle_image);
        switch (reaction.getType()) {
            case "HAHA":
                holder.image_reaction_item.setImageResource(R.drawable.ic_reaction_haha);
                break;
            case "SAD":
                holder.image_reaction_item.setImageResource(R.drawable.ic_reaction_sad);
                break;
            case "LOVE":
                holder.image_reaction_item.setImageResource(R.drawable.ic_reaction_love);
                break;
            case "WOW":
                holder.image_reaction_item.setImageResource(R.drawable.ic_reaction_wow);
                break;
            case "ANGRY":
                holder.image_reaction_item.setImageResource(R.drawable.ic_reaction_angry);
                break;
            case "LIKE":
                holder.image_reaction_item.setImageResource(R.drawable.ic_reaction_like);
                break;
        }
    }

    private void showDialogListReaction(List<ReactionDto> reactionDtos) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.reaction_dialog);
        ListView listView = dialog.findViewById(R.id.lv_reaction_dialog);
        TextView titleOfDialog = dialog.findViewById(R.id.txt_reaction_dialog_title);

        ReactionDialogAdapter arrayAdapter = new ReactionDialogAdapter(context, R.layout.reaction_dialog_line_item, reactionDtos);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, pos, itemId) -> {

        });
        titleOfDialog.setText("Những người đã bày tỏ cảm xúc");
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_readby_dialog);
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return Math.min(list.size(), max);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image_reaction_item;
        TextView txt_reaction_more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_reaction_item = itemView.findViewById(R.id.image_reaction_item);
            txt_reaction_more = itemView.findViewById(R.id.txt_reaction_more);
        }
    }

}
