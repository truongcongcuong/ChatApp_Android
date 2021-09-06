package com.example.chatapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.cons.CroppedDrawable;
import com.example.chatapp.dto.FriendDTO;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    List<FriendDTO> list = new ArrayList<>();
    Context context;

    public FriendListAdapter(List<FriendDTO> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.friend_list_adapter_layout,parent,false);
       return new ViewHolder(this,view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        FriendDTO friend = list.get(position);
        try {
            URL urlOnl = new URL(friend.getFriend().getImageUrl());
            Bitmap bitmap = BitmapFactory.decodeStream(urlOnl.openConnection().getInputStream());
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(),bitmap);
            CroppedDrawable cd = new CroppedDrawable(bitmap);
            holder.img_list_contact_avt.setImageDrawable(cd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.txt_list_contact_display_name.setText(friend.getFriend().getDisplayName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FriendListAdapter friendListAdapter;
        ImageView img_list_contact_avt;
        TextView txt_list_contact_display_name;
        public ViewHolder(FriendListAdapter friendListAdapter, @NonNull View itemView) {
            super(itemView);
            this.friendListAdapter = friendListAdapter;
            txt_list_contact_display_name = itemView.findViewById(R.id.txt_list_contact_display_name);
            img_list_contact_avt = itemView.findViewById(R.id.img_list_contact_avt);
        }
    }
}
