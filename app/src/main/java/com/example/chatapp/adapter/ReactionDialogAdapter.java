package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.ReactionDto;

import java.util.List;

public class ReactionDialogAdapter extends ArrayAdapter<ReactionDto> {
    private final Context context;
    private final List<ReactionDto> reactions;

    public ReactionDialogAdapter(Context context, int resource, List<ReactionDto> reactions) {
        super(context, resource, reactions);
        this.reactions = reactions;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.line_item_view_reaction_dialog, parent, false);

        ImageView imageOfUser = view.findViewById(R.id.image_reaction_dialog_item);
        TextView displayName = view.findViewById(R.id.txt_reaction_dialog_displayname);
        ImageView reaction_type = view.findViewById(R.id.image_reaction_dialog_type);

        ReactionDto reactionDto = reactions.get(position);
        Glide.with(context).load(reactionDto.getReactByUser().getImageUrl())
                .placeholder(R.drawable.image_placeholer)
                .centerCrop()
                .circleCrop()
                .into(imageOfUser);

        displayName.setText(reactionDto.getReactByUser().getDisplayName());
        switch (reactionDto.getType()) {
            case HAHA:
                reaction_type.setImageResource(R.drawable.ic_reaction_haha);
                break;
            case SAD:
                reaction_type.setImageResource(R.drawable.ic_reaction_sad);
                break;
            case LOVE:
                reaction_type.setImageResource(R.drawable.ic_reaction_love);
                break;
            case WOW:
                reaction_type.setImageResource(R.drawable.ic_reaction_wow);
                break;
            case ANGRY:
                reaction_type.setImageResource(R.drawable.ic_reaction_angry);
                break;
            case LIKE:
                reaction_type.setImageResource(R.drawable.ic_reaction_like);
                break;
        }

        return view;
    }
}