package com.example.chatapp.adapter;

import android.content.Context;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.dto.MenuItem;

import java.util.List;

public class MenuButtonAdapterHorizontal extends RecyclerView.Adapter<MenuButtonAdapterHorizontal.ViewHolder> {

    private final Context context;
    private final List<MenuItem> items;
    private final View.OnClickListener onClickListener;

    public MenuButtonAdapterHorizontal(Context context, List<MenuItem> items, View.OnClickListener onClickListener) {
        this.context = context;
        this.items = items;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.line_item_menu_button_horizontal, parent, false);
        view.setOnClickListener(onClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (items != null && position < items.size()) {
            MenuItem menuItem = items.get(position);
            holder.imv_item_menu_button_horizontal.setImageResource(menuItem.getImageResource());
            holder.txt_item_menu_button_name_horizontal.setText(menuItem.getName());
        }

    }

    @Override
    public int getItemCount() {
        if (items == null)
            return 0;
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imv_item_menu_button_horizontal;
        TextView txt_item_menu_button_name_horizontal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_item_menu_button_horizontal = itemView.findViewById(R.id.imv_item_menu_button_horizontal);
            txt_item_menu_button_name_horizontal = itemView.findViewById(R.id.txt_item_menu_button_name_horizontal);
        }

    }

}
