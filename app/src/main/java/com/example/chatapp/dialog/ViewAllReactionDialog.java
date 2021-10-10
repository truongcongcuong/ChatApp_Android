package com.example.chatapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ReactionDialogAdapter;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReactionDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import java.util.List;

public class ViewAllReactionDialog extends Dialog {

    private MessageDto messageDto;
    private List<ReactionDto> reactions;

    private UserSummaryDTO user;
    private Gson gson;
    private String token;
    private Context context;


    private ViewAllReactionDialog(@NonNull Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ViewAllReactionDialog(@NonNull Context context, List<ReactionDto> reactions) {
        super(context);
        this.reactions = reactions;
        this.context = context;


        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        setContentView(R.layout.layout_view_reaction_dialog);
        ListView listView = findViewById(R.id.lv_reaction_dialog);
        TextView titleOfDialog = findViewById(R.id.txt_reaction_dialog_title);

        ReactionDialogAdapter arrayAdapter = new ReactionDialogAdapter(context, R.layout.line_item_view_reaction_dialog, reactions);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, pos, itemId) -> {

        });
        titleOfDialog.setText("Những người đã bày tỏ cảm xúc");

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        getWindow().setBackgroundDrawableResource(R.drawable.dark_background_dialog_circle);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setAttributes(layoutParams);

    }

}
