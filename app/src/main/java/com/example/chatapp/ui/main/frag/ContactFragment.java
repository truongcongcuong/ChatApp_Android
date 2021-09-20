package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.FriendListAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.FriendDTO;
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

public class ContactFragment extends Fragment {

    private RecyclerView rcv_contact_list;
    private FriendListAdapter adapter;
    private SharedPreferences sharedPreferencesToken;
    private Gson gson;
    private List<FriendDTO> list;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ContactFragment() {
    }

    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        rcv_contact_list = view.findViewById(R.id.rcv_contact_list);
        rcv_contact_list.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        updateListFriends();
        return view;
    }

    private void updateListFriends() {
        list = new ArrayList<>();
        String token = sharedPreferencesToken.getString("access-token", null);
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        Type listType = new TypeToken<List<FriendDTO>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);

                        this.adapter = new FriendListAdapter(list, getActivity().getApplicationContext());
                        this.rcv_contact_list.setAdapter(adapter);
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("list friend error", error.toString())) {
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