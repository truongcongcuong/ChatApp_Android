package com.example.chatapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.chatapp.R;
import com.example.chatapp.dto.MyMenuItem;

import java.util.List;

public class MenuInformationAdapter extends ArrayAdapter<MyMenuItem> {
    private final Context context;
    private List<MyMenuItem> items;
    private final int resource;

    public MenuInformationAdapter(Context context, List<MyMenuItem> items, int resource) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
        this.resource = resource;
    }

    public void setItems(List<MyMenuItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(context).inflate(resource, parent, false);

        MyMenuItem item = items.get(position);

        TextView txt_lii_title = view.findViewById(R.id.txt_lii_title);
        TextView txt_lii_infor = view.findViewById(R.id.txt_lii_infor);

        txt_lii_infor.setText(item.getName());
        txt_lii_title.setText(item.getKey());

        return view;
    }

    @Nullable
    @Override
    public MyMenuItem getItem(int position) {
        return items.get(position);
    }

}
