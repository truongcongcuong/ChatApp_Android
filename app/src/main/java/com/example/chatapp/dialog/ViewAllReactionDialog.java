package com.example.chatapp.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ReactionDialogAdapter;
import com.example.chatapp.dto.ReactionDto;

import java.util.List;

public class ViewAllReactionDialog extends Dialog {

    private ViewAllReactionDialog(@NonNull Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ViewAllReactionDialog(@NonNull Context context, List<ReactionDto> reactions) {
        super(context);

        setContentView(R.layout.layout_view_reaction_dialog);
        ListView listView = findViewById(R.id.lv_reaction_dialog);
        TextView titleOfDialog = findViewById(R.id.txt_reaction_dialog_title);
        ImageView imv_close = findViewById(R.id.imv_reaction_dialog_close);
        imv_close.setOnClickListener(v -> cancel());

        ReactionDialogAdapter arrayAdapter = new ReactionDialogAdapter(context, R.layout.line_item_view_reaction_dialog, reactions);
        listView.setAdapter(arrayAdapter);
//        listView.setOnItemClickListener((parent, view, pos, itemId) -> {
//
//        });
        titleOfDialog.setText("Những người đã bày tỏ cảm xúc");

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        layoutParams.height = (int) (displayHeight * 0.5f);
        getWindow().setAttributes(layoutParams);

    }

}
