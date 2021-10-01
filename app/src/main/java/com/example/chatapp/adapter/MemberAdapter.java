package com.example.chatapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.dto.MemberDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import java.util.List;

public class MemberAdapter extends ArrayAdapter<MemberDto> {
    private final Context context;
    private final List<MemberDto> members;
    private Gson gson;
    private UserSummaryDTO user;
    private boolean currentUserIsAdmin;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MemberAdapter(Context context, int resource, List<MemberDto> members) {
        super(context, resource, members);
        this.members = members;
        this.context = context;

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        MemberDto memberDto = new MemberDto();
        /*
        kiểm tra xem ngừi dùng hiện tại có phải admin của room hay không
         */
        boolean find = false;
        int i = 0;
        do {
            MemberDto mem = members.get(i);
            if (mem.getUser().getId().equals(user.getId())) {
                currentUserIsAdmin = mem.isAdmin();
                memberDto = mem;
                find = true;
            }
            i++;
        }
        while (!find && i < members.size());
        members.removeIf(x -> x.getUser().getId().equals(user.getId()));
        members.add(0, memberDto);

        Log.d("isAdmin", currentUserIsAdmin + "");
        Log.d("currentmember", memberDto.toString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.line_item_member, parent, false);
        }
        ImageView imageOfMember = view.findViewById(R.id.image_member_item);
        TextView nameOfMember = view.findViewById(R.id.txt_member_displayname);
        TextView detail = view.findViewById(R.id.txt_member_detail);
        ImageButton setAdmin = view.findViewById(R.id.ibt_member_set_admin);
        ImageButton delete = view.findViewById(R.id.ibt_member_delete);

        MemberDto member = members.get(position);
        try {
            nameOfMember.setText(member.getUser().getDisplayName());
            Glide.with(context).load(member.getUser().getImageUrl())
                    .placeholder(R.drawable.image_placeholer)
                    .centerCrop()
                    .circleCrop()
                    .into(imageOfMember);

            if (member.isAdmin()) {
                detail.setText("Trưởng nhóm");
                if (member.getUser().getId().equals(user.getId()))
                    detail.setText("Tôi");
            } else {
                if (currentUserIsAdmin) {
                    setAdmin.setImageResource(R.drawable.friend_request_custom);
                    delete.setImageResource(R.drawable.ic_baseline_clear_24);
                    setAdmin.setOnClickListener(v -> {
                        Toast.makeText(context, "set admin for member id" + member.getUser().getId(), Toast.LENGTH_SHORT).show();
                    });

                    delete.setOnClickListener(v -> {
                        Toast.makeText(context, "delete member id" + member.getUser().getId(), Toast.LENGTH_SHORT).show();
                    });
                }
                try {
                    String s = "Thêm bởi " + member.getAddByUser().getDisplayName() + " vào " + member.getAddTime();
                    if (member.getUser().getId().equals(user.getId()))
                        s = "Tôi. " + s;
                    detail.setText(s);
                } catch (Exception ignored) {
                    detail.setText("Thêm bởi chưa xác định");
                }
            }
        } catch (Exception ignored) {

        }
        return view;
    }
}