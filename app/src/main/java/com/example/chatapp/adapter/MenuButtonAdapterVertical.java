package com.example.chatapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.dto.MyMenuItem;

import java.util.List;

public class MenuButtonAdapterVertical extends ArrayAdapter<MyMenuItem> {
    private final Context context;
    private List<MyMenuItem> items;
    private final int resource;

    public void setItems(List<MyMenuItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public MenuButtonAdapterVertical(Context context, int resource, List<MyMenuItem> items) {
        super(context, resource, items);
        this.items = items;
        this.context = context;
        this.resource = resource;
    }

    @Override
    @SuppressLint("ViewHolder")
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(resource, parent, false);

        MyMenuItem item = items.get(position);

        ImageView image = view.findViewById(R.id.imv_item_menu_button);
        TextView name = view.findViewById(R.id.txt_item_menu_button_name);

        Glide.with(context)
                .load(item.getImageResource())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image);
        name.setText(item.getName());

        return view;
    }

    @Nullable
    @Override
    public MyMenuItem getItem(int position) {
        return items.get(position);
    }

}