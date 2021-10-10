package com.example.chatapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapterHorizontal;
import com.example.chatapp.adapter.ReactionDialogCreateAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.SendDataReplyMessage;
import com.example.chatapp.dto.MenuItem;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageOptionDialog extends Dialog implements View.OnClickListener {

    private MessageDto messageDto;
    private RecyclerView rcvReaction;
    private RecyclerView rcvMenuOption;
    private ReactionDialogCreateAdapter arrayAdapter;
    private MenuButtonAdapterHorizontal menuButtonAdapterHorizontal;
    private List<MenuItem> menuItems;

    private UserSummaryDTO user;
    private Gson gson;
    private String token;
    private Context context;


    private MessageOptionDialog(@NonNull Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MessageOptionDialog(@NonNull Context context, MessageDto messageDto) {
        super(context);
        this.messageDto = messageDto;
        this.context = context;
        setContentView(R.layout.layout_message_option_dialog);

        rcvReaction = findViewById(R.id.rcv_reaction_dialog_create);
        rcvMenuOption = findViewById(R.id.rcv_message_option_menu);

        LinearLayout layout_menu_in_message_option_dialog = findViewById(R.id.layout_menu_in_message_option_dialog);

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        if (messageDto.isDeleted()) {
            layout_menu_in_message_option_dialog.setVisibility(View.GONE);
        }
        menuItems = new ArrayList<>();
        menuItems.add(MenuItem.builder()
                .key("reply")
                .imageResource(R.drawable.ic_round_reply_36)
                .name("Phản hồi")
                .build());
        if (messageDto.getSender() != null && user.getId().equals(messageDto.getSender().getId())) {
            menuItems.add(MenuItem.builder()
                    .key("deleteMessage")
                    .imageResource(R.drawable.ic_baseline_delete_forever_24)
                    .name("Gỡ tin nhắn")
                    .build());
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        arrayAdapter = new ReactionDialogCreateAdapter(messageDto, context, this);
        rcvReaction.setLayoutManager(layoutManager);
        rcvReaction.setAdapter(arrayAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4, LinearLayoutManager.VERTICAL, false);
        menuButtonAdapterHorizontal = new MenuButtonAdapterHorizontal(context, menuItems, this);
        rcvMenuOption.setLayoutManager(gridLayoutManager);
        rcvMenuOption.setAdapter(menuButtonAdapterHorizontal);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setAttributes(layoutParams);

    }

    @Override
    public void onClick(View v) {
        int position = rcvMenuOption.indexOfChild(v);
        MenuItem menuItem = menuItems.get(position);
        if (menuItem.getKey().equals("deleteMessage")) {
            deleteMessage();
            this.cancel();
        } else if (menuItem.getKey().equals("reply")) {
            SendDataReplyMessage sendDataReplyMessage = (SendDataReplyMessage) context;
            sendDataReplyMessage.reply(messageDto);
            this.cancel();
        }
    }

    private void deleteMessage() {
        StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_MESSAGE + messageDto.getId(),
                response -> {
                    Log.i("delete message ok", "ok");
                },
                error -> Log.i("delete message error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

}
