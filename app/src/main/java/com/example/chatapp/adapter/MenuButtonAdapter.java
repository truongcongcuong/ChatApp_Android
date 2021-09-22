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

import com.example.chatapp.R;
import com.example.chatapp.dto.MenuItem;

import java.util.List;

public class MenuButtonAdapter extends ArrayAdapter<MenuItem> {
    private final Context context;
    private final List<MenuItem> items;

    public MenuButtonAdapter(Context context, int resource, List<MenuItem> items) {
        super(context, resource, items);
        this.items = items;
        this.context = context;
    }

    @Override
    @SuppressLint("ViewHolder")
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_menu_button, parent, false);

        MenuItem item = items.get(position);

        ImageView image = view.findViewById(R.id.imv_item_menu_button);
        TextView name = view.findViewById(R.id.txt_item_menu_button_name);

        image.setImageResource(item.getImageResource());
        name.setText(item.getName());

        return view;
    }

    @Nullable
    @Override
    public MenuItem getItem(int position) {
        return items.get(position);
    }

}