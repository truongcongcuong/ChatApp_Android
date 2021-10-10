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
import com.example.chatapp.adapter.ReadbyDialogAdapter;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.ReadByDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import java.util.List;

public class ViewAllReadTrackingDialog extends Dialog {

    private MessageDto messageDto;
    private List<ReadByDto> reads;

    private UserSummaryDTO user;
    private Gson gson;
    private String token;
    private Context context;


    private ViewAllReadTrackingDialog(@NonNull Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ViewAllReadTrackingDialog(@NonNull Context context, List<ReadByDto> reads) {
        super(context);
        this.reads = reads;
        this.context = context;

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        setContentView(R.layout.layout_view_readby_dialog);

        ListView listView = findViewById(R.id.lv_readby_dialog);
        TextView titleOfDialog = findViewById(R.id.txt_readby_dialog_title);

        ReadbyDialogAdapter arrayAdapter = new ReadbyDialogAdapter(context, R.layout.line_item_readby_dialog, reads);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((parent, view, pos, itemId) -> {

        });
        titleOfDialog.setText("Những người đã xem");

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
