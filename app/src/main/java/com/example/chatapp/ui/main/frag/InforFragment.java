package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.ui.ChangePasswordActivity;
import com.example.chatapp.ui.HomePageActivity;
import com.example.chatapp.ui.ViewInformationActivity;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class InforFragment extends Fragment {
    private TextView txt_info_error;
    private TextView txt_info_name,txt_infor_update,txt_frg_infor_change_password;
    private Button btn_info_signout;
    private ImageView image_info_image;
    private Gson gson;
    private UserSummaryDTO user;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public InforFragment() {
    }

    public static InforFragment newInstance(String param1, String param2) {
        InforFragment fragment = new InforFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        enable menu trên action bar
         */
        setHasOptionsMenu(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        gson = new Gson();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_infor, container, false);

        txt_info_error = view.findViewById(R.id.txt_info_error);
        txt_infor_update = view.findViewById(R.id.txt_infor_update);
        txt_info_error.setTextColor(getActivity().getResources().getColor(R.color.red));
        btn_info_signout = view.findViewById(R.id.btn_info_signout);
        txt_info_name = view.findViewById(R.id.txt_info_name);
        image_info_image = view.findViewById(R.id.image_info_image);
        txt_frg_infor_change_password = view.findViewById(R.id.txt_frg_infor_change_password);

        btn_info_signout.setOnClickListener(v -> {
            callSignout();
            SharedPreferences sharedPreferencesIsLogin = getActivity().getApplicationContext().getSharedPreferences("is-login", getActivity().getApplicationContext().MODE_PRIVATE);
            SharedPreferences.Editor editorIsLogin = sharedPreferencesIsLogin.edit();
            editorIsLogin.putBoolean("status-login", false).apply();
            Intent i = new Intent(getActivity().getApplicationContext(), HomePageActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("EXIT", true);
            startActivity(i);
            getActivity().finish();
        });
        getUserInfo();
        txt_infor_update.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), ViewInformationActivity.class);
            startActivity(intent);
        });

        txt_frg_infor_change_password.setOnClickListener(v->{
            Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void getUserInfo() {
        SharedPreferences sharedPreferencesUser = getActivity().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        txt_info_name.setText(user.getDisplayName());
        Glide.with(this).load(user.getImageUrl())
                .centerCrop().circleCrop().into(image_info_image);
    }

    private void callSignout() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_AUTH + "signout",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        txt_info_error.setText(res);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            txt_info_error.setText(object.getString("message"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", getActivity().getApplicationContext().MODE_PRIVATE);
                String rfToken = sharedPreferencesToken.getString("refresh-token", null);
                map.put("Cookie", rfToken);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(request);
    }

    /*
    chỉ khi nào fragment được hiển thị thì mới hiện menu
     */
    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(isVisible());
    }

}