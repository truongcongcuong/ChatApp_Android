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
import com.example.chatapp.adapter.ListMessageAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageFragment extends Fragment {
    private RecyclerView rcv_list_message;
    private ListMessageAdapter adapter;
    private List<InboxDto> list;
    private Gson gson;
    private String token;
    private int page = 0;
    private int size = 20;
    private SimpleDateFormat dateFormat;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private MessageDto messageDto = null;

    public MessageFragment() {
    }

    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
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
        list = new ArrayList<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setNewMessage(MessageDto newMessage) {
        messageDto = newMessage;
        for (InboxDto inboxDto : list) {
            if (messageDto != null && inboxDto.getRoom().getId().equals(messageDto.getRoomId())) {
                inboxDto.setLastMessage(messageDto);
            }
        }
        sortTimeLastMessage();
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        rcv_list_message = view.findViewById(R.id.rcv_list_message);
        rcv_list_message.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        updateListString();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateListString() {
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");

                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);

                        sortTimeLastMessage();
                        this.adapter = new ListMessageAdapter(getActivity().getApplicationContext(), list);
                        this.rcv_list_message.setAdapter(adapter);

                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("mesage fragment error", error.toString())) {
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortTimeLastMessage() {
        if (list != null) {
            list.sort((x, y) -> {
                try {
                    Date d1 = dateFormat.parse(x.getLastMessage().getCreateAt());
                    Date d2 = dateFormat.parse(y.getLastMessage().getCreateAt());
                    if (d1 == null || d2 == null)
                        return 0;
                    return d2.compareTo(d1);
                } catch (ParseException | NullPointerException e) {
                    e.printStackTrace();
                    return 0;
                }
            });
        }
    }
}