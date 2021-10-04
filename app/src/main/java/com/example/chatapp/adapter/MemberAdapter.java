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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MemberDto;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberAdapter extends ArrayAdapter<MemberDto> {
    private final Context context;
    private List<MemberDto> members;
    private Gson gson;
    private UserSummaryDTO user;
    private boolean currentUserIsAdmin;
    private String access_token;
    private InboxDto inboxDto;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MemberAdapter(Context context, int resource, List<MemberDto> members, InboxDto inboxDto) {
        super(context, resource, members);
        this.members = members;
        this.context = context;
        this.inboxDto = inboxDto;

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);

        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        access_token = sharedPreferencesToken.getString("access-token", null);

        MemberDto memberDto = new MemberDto();
        /*
        kiểm tra xem người dùng hiện tại có phải admin của room hay không
         */
        if (members != null && !members.isEmpty()) {
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
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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

            String creatorId = inboxDto.getRoom().getCreateByUserId();
            /*
            nếu người dùng hiện tại là người tạo nhóm, có quyền cao nhất
             */
            if (creatorId != null && creatorId.equals(user.getId())) {
                /*
                hiện icon set admin và xóa trên các thành viên còn lại
                 */
                if (!member.getUser().getId().equals(user.getId())) {
                    if (!member.isAdmin()) {
                        /*
                        nếu thành viên k phải admin thì hiện thêm icon set admin
                         */
                        setAdmin.setImageResource(R.drawable.ic_baseline_admin_panel_settings_24);
                        setAdmin.setOnClickListener(v -> {
                            setAdminForMember(member);
                        });
                    } else {
                        /*
                        nếu thành viên là admin rồi thì k hiện icon set admin
                         */
                        setAdmin.setImageDrawable(null);
                        setAdmin.setClickable(false);
                    }
                    delete.setImageResource(R.drawable.ic_baseline_delete_forever_24);
                    delete.setOnClickListener(v -> {
                        deleteMember(member);
                    });
                }
            } else {
                /*
                nếu người dùng hiện tại  k phải người tạo nhóm nhưng là admin
                 */
                if (currentUserIsAdmin) {
                    /*
                    thêm icon set admin và xóa trên những người còn lại
                     */
                    if (!member.isAdmin() && creatorId != null && !creatorId.equals(member.getUser().getId())) {
                        setAdmin.setImageResource(R.drawable.ic_baseline_admin_panel_settings_24);
                        delete.setImageResource(R.drawable.ic_baseline_delete_forever_24);

                        setAdmin.setOnClickListener(v -> {
                            setAdminForMember(member);
                        });

                        delete.setOnClickListener(v -> {
                            deleteMember(member);
                        });
                    }
                }
            }

            if (member.isAdmin()) {
                String dt = "Quản trị viên.";
                if (creatorId != null && creatorId.equals(member.getUser().getId())) {
                    dt += " Người tạo nhóm.";
                }
                detail.setText(dt);
            } else {
                try {
                    String s = "Thêm bởi " + member.getAddByUser().getDisplayName() + " vào " + member.getAddTime();
                    detail.setText(s);
                } catch (Exception ignored) {
                    detail.setText("Thêm bởi chưa xác định");
                }
            }
        } catch (Exception ignored) {

        }
        return view;
    }

    /*
    xóa thành viên khỏi nhóm
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteMember(MemberDto member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Xóa " + member.getUser().getDisplayName() + " khỏi nhóm?")
                .setPositiveButton("Hủy", (dialog, id) -> dialog.cancel())
                .setNegativeButton("Xóa", (dialog, id) -> {
                    StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_ROOM + inboxDto.getRoom().getId() + "/" + member.getUser().getId(),
                            response -> {
                                Log.d("delete ok", "--------");
                                Log.d("before delete ", inboxDto.getRoom().getMembers().toString());
                                members.removeIf(x -> x.getUser().getId().equals(member.getUser().getId()));
                                inboxDto.getRoom().getMembers().removeIf(x -> x.getUserId().equals(member.getUser().getId()));
                                Log.d("after delete ", inboxDto.getRoom().getMembers().toString());
                                notifyDataSetChanged();
                            },
                            error -> Log.i("detete error", error.toString())) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Authorization", "Bearer " + access_token);
                            return map;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(request);
                });
        builder.create().show();
    }

    /*
    set thành viên làm admin
     */
    private void setAdminForMember(MemberDto member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Thêm " + member.getUser().getDisplayName() + " làm quản trị viên?")
                .setPositiveButton("Hủy", (dialog, id) -> dialog.cancel())
                .setNegativeButton("Thêm", (dialog, id) -> {
                    StringRequest request = new StringRequest(Request.Method.POST, Constant.API_ROOM + "admin/" + inboxDto.getRoom().getId() + "/" + member.getUser().getId(),
                            response -> {
                                for (MemberDto m : members) {
                                    if (m.getUser().getId().equals(member.getUser().getId()))
                                        m.setAdmin(true);
                                }
                                notifyDataSetChanged();
                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                alert.setMessage("Thêm thành công " + member.getUser().getDisplayName() + " làm quản trị viên")
                                        .setPositiveButton("OK", (dial, identify) -> dialog.cancel());
                                alert.setCancelable(false);
                                alert.create().show();
                            },
                            error -> Log.i("set admin error", error.toString())) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Authorization", "Bearer " + access_token);
                            return map;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(request);
                });
        builder.create().show();
    }
}