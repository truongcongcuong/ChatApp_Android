package com.example.chatapp.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapterVertical;
import com.example.chatapp.dto.MenuItem;
import com.example.chatapp.dto.UserDetailDTO;
import com.example.chatapp.ui.ViewImageActivity;

import java.util.ArrayList;
import java.util.List;

public class ChangeAvatarDialog extends Dialog {

    private List<MenuItem> menuItems;

    public ChangeAvatarDialog(@NonNull Context context, final int REQUEST_GALLERY,
                              final int REQUEST_IMAGE_CAPTURE,
                              List<MenuItem> listMenu,
                              UserDetailDTO userDetailDTO) {
        super(context);

        if (listMenu == null || listMenu.isEmpty()) {
            menuItems = new ArrayList<>();
            menuItems.add(MenuItem.builder()
                    .key("view")
                    .name(context.getString(R.string.view_avatar))
                    .imageResource(R.drawable.ic_outline_remove_red_eye_24)
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("takeNew")
                    .name(context.getString(R.string.take_photo))
                    .imageResource(R.drawable.ic_outline_camera_alt_24)
                    .build());
            menuItems.add(MenuItem.builder()
                    .key("takeFromGallery")
                    .name(context.getString(R.string.choose_from_gallery))
                    .imageResource(R.drawable.ic_outline_image_24)
                    .build());
        } else {
            menuItems = listMenu;
        }

        setContentView(R.layout.dialog_change_avatar);

        MenuButtonAdapterVertical menuAdapter = new MenuButtonAdapterVertical(context, R.layout.line_item_menu_button_vertical, menuItems);
        ListView listView = findViewById(R.id.change_avt_dialog_list_view);
        listView.setAdapter(menuAdapter);

        listView.setOnItemClickListener((parent, view, position, itemId) -> {
            MenuItem item = menuItems.get(position);
            switch (item.getKey()) {
                case "view":
                    Intent intent = new Intent(context, ViewImageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("activityTitle", userDetailDTO.getDisplayName());
                    bundle.putString("activitySubTitle", context.getString(R.string.avatar));
                    bundle.putString("imageUrl", userDetailDTO.getImageUrl());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    break;
                case "takeFromGallery":
                    Toast.makeText(context, "Action take photo active", Toast.LENGTH_LONG).show();
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    ((Activity) context).startActivityForResult(galleryIntent, REQUEST_GALLERY);

                    break;
                case "takeNew":
                    Toast.makeText(context, "Action take photo from gallery active", Toast.LENGTH_LONG).show();
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ((Activity) context).startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
                    break;
            }
        });

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        layoutParams.height = (int) (displayHeight * 0.5f);
        getWindow().setAttributes(layoutParams);
    }

}
