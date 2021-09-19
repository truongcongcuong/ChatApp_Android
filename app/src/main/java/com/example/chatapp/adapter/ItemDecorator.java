package com.example.chatapp.adapter;

import android.graphics.Rect;
import android.view.View;

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
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position != 0)
            outRect.left = space;
    }
}