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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MemberDto;
import com.example.chatapp.dto.RoomDTO;
import com.example.chatapp.dto.UserSummaryDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemberAdapter extends ArrayAdapter<MemberDto> {
    private final Context context;
    private List<MemberDto> members;
    private UserSummaryDTO user;
    private boolean currentUserIsAdmin;
    private final String access_token;
    private InboxDto inboxDto;
    private final Gson gson;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MemberAdapter(Context context, int resource, List<MemberDto> members, InboxDto inboxDto) {
        super(context, resource, members);
        if (members == null)
            this.members = new ArrayList<>();
        else
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
        if (this.members != null && !this.members.isEmpty()) {
            boolean find = false;
            int i = 0;
            do {
                MemberDto mem = this.members.get(i);
                if (mem.getUser().getId().equals(user.getId())) {
                    currentUserIsAdmin = mem.isAdmin();
                    memberDto = mem;
                    find = true;
                }
                i++;
            }
            while (!find && i < this.members.size());
            this.members.removeIf(x -> x.getUser().getId().equals(user.getId()));
            this.members.add(0, memberDto);

            Log.d("isAdmin", currentUserIsAdmin + "");
            Log.d("current member", memberDto.toString());
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
        if (member != null) {
            try {
                nameOfMember.setText(member.getUser().getDisplayName());
                Glide.with(context).load(member.getUser().getImageUrl())
                        .placeholder(R.drawable.img_avatar_placeholer)
                        .transition(DrawableTransitionOptions.withCrossFade())
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
                            setAdmin.setImageResource(R.drawable.ic_baseline_set_admin_24);
                            setAdmin.setOnClickListener(v -> setAdminForMember(member));
                        } else {
                            /*
                            nếu thành viên là admin rồi thì hiện icon thu hồi quyền admin
                             */
                            setAdmin.setImageResource(R.drawable.ic_baseline_recall_admin_24);
                            setAdmin.setOnClickListener(v -> recallAdminForMember(member));
                        }
                        delete.setImageResource(R.drawable.ic_baseline_delete_forever_24);
                        delete.setOnClickListener(v -> deleteMember(member));
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
                            setAdmin.setImageResource(R.drawable.ic_baseline_set_admin_24);
                            delete.setImageResource(R.drawable.ic_baseline_delete_forever_24);

                            setAdmin.setOnClickListener(v -> setAdminForMember(member));
                            delete.setOnClickListener(v -> deleteMember(member));
                        }
                    } else {
                        /*
                        nếu là thành viên bình thường thì có quyền xóa thành viên nào mà mình đã thêm vào
                         */
                        if (!member.isAdmin() && member.getAddByUser() != null && user.getId().equals(member.getAddByUser().getId())) {
                            delete.setImageResource(R.drawable.ic_baseline_delete_forever_24);
                            delete.setOnClickListener(v -> deleteMember(member));
                        }
                    }
                }

                if (creatorId != null && creatorId.equals(member.getUser().getId())) {
                    String s = context.getString(R.string.group_admin) + " " + context.getString(R.string.group_creator);
                    detail.setText(s);
                } else if (member.isAdmin()) {
                    detail.setText(context.getString(R.string.group_admin));
                } else {
                    try {
                        String s = context.getString(R.string.member_add_by, member.getAddByUser().getDisplayName(), member.getAddTime());
                        detail.setText(s);
                    } catch (Exception ignored) {
                        detail.setText(context.getString(R.string.member_add_by_undefine));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    private void recallAdminForMember(MemberDto member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String message = context.getString(R.string.recall_admin_for_member, member.getUser().getDisplayName());
        builder.setMessage(message)
                .setPositiveButton(R.string.cancel_button, (dialog, id) -> dialog.cancel())
                .setNegativeButton(R.string.confirm_button, (dialog, id) -> {
                    StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_ROOM + "admin/" + inboxDto.getRoom().getId() + "/" + member.getUser().getId(),
                            response -> {
                                for (MemberDto m : members) {
                                    if (m.getUser().getId().equals(member.getUser().getId()))
                                        m.setAdmin(false);
                                }
                                notifyDataSetChanged();
                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                alert.setMessage(context.getString(R.string.recall_admin_for_member_success, member.getUser().getDisplayName()))
                                        .setPositiveButton(R.string.confirm_button, (dial, identify) -> dialog.cancel());
                                alert.setCancelable(false);
                                alert.create().show();
                            },
                            error -> Log.i("recall admin error", error.toString())) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Authorization", "Bearer " + access_token);
                            return map;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    request.setRetryPolicy(retryPolicy);
                    requestQueue.add(request);
                });
        builder.create().show();
    }

    /*
    xóa thành viên khỏi nhóm
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteMember(MemberDto member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String message = context.getString(R.string.delete_member, member.getUser().getDisplayName());
        builder.setMessage(message)
                .setPositiveButton(R.string.cancel_button, (dialog, id) -> dialog.cancel())
                .setNegativeButton(R.string.delete, (dialog, id) -> {
                    RoomDTO room = inboxDto.getRoom();
                    StringRequest request = new StringRequest(Request.Method.DELETE, Constant.API_ROOM + room.getId() + "/" + member.getUser().getId(),
                            response -> {
                                try {
                                    String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                    Type type = new TypeToken<Set<MemberDto>>() {
                                    }.getType();
                                    Set<MemberDto> newMembers = gson.fromJson(res, type);
                                    members.removeIf(x -> x.getUser().getId().equals(member.getUser().getId()));
                                    room.setMembers(newMembers);
                                    inboxDto.setRoom(room);
                                    notifyDataSetChanged();
                                } catch (UnsupportedEncodingException e) {

                                }
                            },
                            error -> Log.i("delete error", error.toString())) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Authorization", "Bearer " + access_token);
                            return map;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    request.setRetryPolicy(retryPolicy);
                    requestQueue.add(request);
                });
        builder.create().show();
    }

    /*
    set thành viên làm admin
     */
    private void setAdminForMember(MemberDto member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String message = context.getString(R.string.set_member_to_admin, member.getUser().getDisplayName());
        builder.setMessage(message)
                .setPositiveButton(R.string.cancel_button, (dialog, id) -> dialog.cancel())
                .setNegativeButton(R.string.confirm_button, (dialog, id) -> {
                    StringRequest request = new StringRequest(Request.Method.POST, Constant.API_ROOM + "admin/" + inboxDto.getRoom().getId() + "/" + member.getUser().getId(),
                            response -> {
                                for (MemberDto m : members) {
                                    if (m.getUser().getId().equals(member.getUser().getId()))
                                        m.setAdmin(true);
                                }
                                notifyDataSetChanged();
                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                alert.setMessage(context.getString(R.string.set_member_to_admin_success, member.getUser().getDisplayName()))
                                        .setPositiveButton(R.string.confirm_button, (dial, identify) -> dialog.cancel());
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
                    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    request.setRetryPolicy(retryPolicy);
                    requestQueue.add(request);
                });
        builder.create().show();
    }
}