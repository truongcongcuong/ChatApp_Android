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
import com.example.chatapp.utils.TimeAgo;

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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.readby_dialog_line_item, parent, false);
        ImageView imageOfUser = rowView.findViewById(R.id.image_readby_dialog_item);
        TextView displayName = rowView.findViewById(R.id.txt_readby_dialog_displayname);
        TextView readAt = rowView.findViewById(R.id.txt_readby_dialog_read_at);

        ReadByDto readBy = readByes.get(position);
        Glide.with(context).load(readBy.getReadByUser().getImageUrl())
                .placeholder(R.drawable.image_placeholer)
                .centerCrop()
                .circleCrop()
                .into(imageOfUser);

        displayName.setText(readBy.getReadByUser().getDisplayName());
        readAt.setText("Đã xem: " + TimeAgo.getTime(readBy.getReadAt()));

        return rowView;
    }
}