package com.example.chatapp.ui.main.frag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.ListMessageAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.RoomDTO;
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

public class MessageFragmentContent extends Fragment {
    private RecyclerView rcv_list_message;
    private ListMessageAdapter adapter;
    private Gson gson;
    private String token;
    private int page = 0;
    private final int size = 5;
    private Button btnLoadMore;

    /*
    kéo để làm mới
     */
    private SwipeRefreshLayout refreshLayout;
    private final Context context;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private final BroadcastReceiver addMember = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                adapter.updateRoomChange(newRoom);
            }
        }
    };

    private final BroadcastReceiver deleteMember = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                adapter.updateRoomChange(newRoom);
            }
        }
    };

    private final BroadcastReceiver renameRoom = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                adapter.updateRoomChange(newRoom);
            }
        }
    };

    private final BroadcastReceiver changeImageRoom = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                RoomDTO newRoom = (RoomDTO) bundle.getSerializable("dto");
                adapter.updateRoomChange(newRoom);
            }
        }
    };

    /*
    lắng nghe sự kiện thay đổi ngôn ngữ
     */
    private final BroadcastReceiver changeLanguage = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                boolean change = bundle.getBoolean("change");
                if (change) {
                    adapter.notifyDataSetChanged();
                    btnLoadMore.setText(getString(R.string.load_more));
                }
            }
        }
    };

    public MessageFragmentContent(Context context) {
        this.context = context;
    }

    public static MessageFragmentContent newInstance(String param1, String param2, Context context) {
        MessageFragmentContent fragment = new MessageFragmentContent(context);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(addMember, new IntentFilter("room/members/add"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(deleteMember, new IntentFilter("room/members/delete"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(renameRoom, new IntentFilter("room/rename"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(changeImageRoom, new IntentFilter("room/changeImage"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(changeLanguage, new IntentFilter("language/change"));
        /*
        enable menu trên action bar
         */
        setHasOptionsMenu(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        gson = new Gson();
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setNewMessage(MessageDto newMessage) {
        adapter.setNewMessage(newMessage);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_content, container, false);

        rcv_list_message = view.findViewById(R.id.rcv_list_message);
        btnLoadMore = view.findViewById(R.id.message_fragment_content_btn_load_more);
        btnLoadMore.setVisibility(View.GONE);
        rcv_list_message.setLayoutManager(new LinearLayoutManager(context));

        this.adapter = new ListMessageAdapter(context, new ArrayList<>());
        this.rcv_list_message.setAdapter(adapter);
        updateListInbox();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcv_list_message.getContext(), DividerItemDecoration.VERTICAL);
        rcv_list_message.addItemDecoration(dividerItemDecoration);

        /*
        sự kiện kéo để làm mới
         */
        refreshLayout = view.findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeColors(Color.RED);
        refreshLayout.setOnRefreshListener(() -> {
            page = 0;
            refreshListInbox();
        });
        btnLoadMore.setOnClickListener(v -> {
            loadMoreData();
        });
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void refreshListInbox() {
        Log.d("--", "on refresh function");
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        if (last)
                            btnLoadMore.setVisibility(View.GONE);
                        else
                            btnLoadMore.setVisibility(View.VISIBLE);
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        List<InboxDto> list = gson.fromJson(array.toString(), listType);
                        if (!list.isEmpty()) {
                            adapter = new ListMessageAdapter(context, list);
                            rcv_list_message.setAdapter(adapter);
                        }
                        refreshLayout.setRefreshing(false);
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("message fragment error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadMoreData() {
        page++;
        updateListInbox();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateListInbox() {
        Log.d("--", "on update list function ");
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        if (last)
                            btnLoadMore.setVisibility(View.GONE);
                        else
                            btnLoadMore.setVisibility(View.VISIBLE);
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        List<InboxDto> list = gson.fromJson(array.toString(), listType);
                        if (!list.isEmpty()) {
                            adapter.updateList(list);
                            if (page != 0) {
                                Log.d("last-----", (page * size) + "");
                                rcv_list_message.scrollToPosition((page * size));
                            }
                        }
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("message fragment error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(addMember);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(deleteMember);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(renameRoom);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(changeImageRoom);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(changeLanguage);
        super.onDestroy();
    }

    public void resetUnReadMessageForRoom(String roomId) {
        adapter.resetUnReadMessageForRoom(roomId);
    }
}