package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.GroupAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.InboxDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFragment extends Fragment {

    private RecyclerView rcv_list_group;
    private GroupAdapter adapter;
    private List<InboxDto> list;
    private Gson gson;
    private SharedPreferences sharedPreferencesToken;
    private int page = 0;
    private int size = 20;
    private final String type = "GROUP";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public GroupFragment() {
    }

    public static GroupFragment newInstance(String param1, String param2) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        gson = new Gson();
        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(getActivity().getApplicationContext());
        getNewAccessToken.sendGetNewTokenRequest();
        sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        rcv_list_group = view.findViewById(R.id.rcv_list_group);
        rcv_list_group.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        updateListString();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListString() {
        list = new ArrayList<>();
        String token = sharedPreferencesToken.getString("access-token", null);
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size + "&type=" + type,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);

                        this.adapter = new GroupAdapter(getActivity().getApplicationContext(), list);
                        this.rcv_list_group.setAdapter(adapter);

                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("group list error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(request);
    }
}