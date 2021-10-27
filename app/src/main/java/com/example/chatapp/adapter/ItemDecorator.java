package com.example.chatapp.adapter;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
làm cho ảnh của readby bên phải chồng lên ảnh readby bên trái
 */
public class ItemDecorator extends RecyclerView.ItemDecoration {
    private final int space;

    public ItemDecorator(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position != 0)
            outRect.right = space;
    }
}