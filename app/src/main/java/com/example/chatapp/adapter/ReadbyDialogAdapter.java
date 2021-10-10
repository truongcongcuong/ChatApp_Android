package com.example.chatapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.ReadByDto;

import java.util.List;

public class ReadbyDialogAdapter extends ArrayAdapter<ReadByDto> {
    private final Context context;
    private final List<ReadByDto> readByes;

    public ReadbyDialogAdapter(Context context, int resource, List<ReadByDto> readByes) {
        super(context, resource, readByes);
        this.readByes = readByes;
        this.context = context;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_readby_dialog, parent, false);

        ImageView imageOfUser = view.findViewById(R.id.image_readby_dialog_item);
        TextView displayName = view.findViewById(R.id.txt_readby_dialog_displayname);
        TextView readAt = view.findViewById(R.id.txt_readby_dialog_read_at);

        ReadByDto readBy = readByes.get(position);
        Glide.with(context).load(readBy.getReadByUser().getImageUrl())
                .placeholder(R.drawable.image_placeholer)
                .centerCrop()
                .circleCrop()
                .into(imageOfUser);

        displayName.setText(readBy.getReadByUser().getDisplayName());
        readAt.setText(String.format("Đã xem: %s", readBy.getReadAt()));

        return view;
    }
}