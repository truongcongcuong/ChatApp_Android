package com.example.chatapp.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageOptionDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private MessageDto messageDto;
    private RecyclerView rcvMenuOption;
    private List<MenuItem> menuItems;
    private String token;
    private final Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_message_option_dialog, container, false);

        RecyclerView rcvReaction = view.findViewById(R.id.rcv_reaction_dialog_create);
        rcvMenuOption = view.findViewById(R.id.rcv_message_option_menu);

        ConstraintLayout layout_menu_in_message_option_dialog = view.findViewById(R.id.layout_menu_in_message_option_dialog);

        Gson gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        UserSummaryDTO user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        if (messageDto.isDeleted()) {
            layout_menu_in_message_option_dialog.setVisibility(View.GONE);
        }
        menuItems = new ArrayList<>();
        menuItems.add(MenuItem.builder()
                .key("reply")
                .imageResource(R.drawable.ic_round_reply_36)
                .name(context.getString(R.string.reply))
                .build());

        if (messageDto.getSender() != null && user.getId().equals(messageDto.getSender().getId())) {
            menuItems.add(MenuItem.builder()
                    .key("deleteMessage")
                    .imageResource(R.drawable.ic_baseline_delete_forever_24)
                    .name(context.getString(R.string.remove))
                    .build());
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        ReactionDialogCreateAdapter arrayAdapter = new ReactionDialogCreateAdapter(messageDto, context, this);
        rcvReaction.setLayoutManager(layoutManager);
        rcvReaction.setAdapter(arrayAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4, LinearLayoutManager.VERTICAL, false);
        MenuButtonAdapterHorizontal menuButtonAdapterHorizontal = new MenuButtonAdapterHorizontal(context, menuItems, this);
        rcvMenuOption.setLayoutManager(gridLayoutManager);
        rcvMenuOption.setAdapter(menuButtonAdapterHorizontal);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MessageOptionDialog(@NonNull Context context, MessageDto messageDto) {
        this.messageDto = messageDto;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        int position = rcvMenuOption.indexOfChild(v);
        MenuItem menuItem = menuItems.get(position);
        if (menuItem.getKey().equals("deleteMessage")) {
            deleteMessage();
            dismiss();
        } else if (menuItem.getKey().equals("reply")) {
            SendDataReplyMessage sendDataReplyMessage = (SendDataReplyMessage) context;
            sendDataReplyMessage.reply(messageDto);
            dismiss();
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
