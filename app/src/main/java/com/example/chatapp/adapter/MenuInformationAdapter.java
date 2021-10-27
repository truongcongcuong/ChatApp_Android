package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.chatapp.R;
import com.example.chatapp.dto.MenuItem;

import java.util.List;

public class MenuInformationAdapter extends ArrayAdapter<MenuItem> {
    private final Context context;
    private final List<MenuItem> items;

    public MenuInformationAdapter(Context context, List<MenuItem> items, int resource) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.line_item_information, parent, false);
        }

        MenuItem item = items.get(position);

        TextView txt_lii_title = view.findViewById(R.id.txt_lii_title);
        TextView txt_lii_infor = view.findViewById(R.id.txt_lii_infor);

        txt_lii_infor.setText(item.getName());
        txt_lii_title.setText(item.getKey());

        return view;
    }

    @Nullable
    @Override
    public MenuItem getItem(int position) {
        return items.get(position);
    }

}
