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
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.FriendRequestSentAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.entity.FriendRequest;
import com.example.chatapp.ui.FriendRequestActivity;
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

public class FriendRequestSentFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private String token;
    private Gson gson;
    private FriendRequestSentAdapter adapter;
    private List<FriendRequest> list;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout friend_request_sent_refresh_layout;
    private TextView txt_friend_request_sent_no_request;
    private int page = 0;
    private final int size = 1;
    private final FriendRequestActivity parent;
    private final int POSITION_OF_SENT = 1;
    private Button btnLoadMore;
    private int totalElements = 0;

    private final BroadcastReceiver friendRequestSentEmpty = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean empty = intent.getBooleanExtra("dto", true);
            if (empty)
                totalElements = 0;
            shouldShowMessageEmpty();
        }
    };

    public FriendRequestSentFragment(FriendRequestActivity parent) {
        this.parent = parent;
    }

    public static FriendRequestSentFragment newInstance(String param1, String param2, FriendRequestActivity parent) {
        FriendRequestSentFragment fragment = new FriendRequestSentFragment(parent);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(friendRequestSentEmpty, new IntentFilter("friendRequest/sent/empty"));

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        SharedPreferences sharedPreferencesToken = getActivity().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        gson = new Gson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request_sent, container, false);

        recyclerView = view.findViewById(R.id.lsv_friend_request_sent);
        btnLoadMore = view.findViewById(R.id.friend_request_sent_btn_load_more);
        btnLoadMore.setVisibility(View.GONE);
        txt_friend_request_sent_no_request = view.findViewById(R.id.txt_friend_request_sent_no_request);
        txt_friend_request_sent_no_request.setVisibility(View.GONE);

        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FriendRequestSentAdapter(getContext(), list, token);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        getListFriendRequest();

        /*
        sự kiện kéo để làm mới
         */
        friend_request_sent_refresh_layout = view.findViewById(R.id.friend_request_sent_refresh_layout);
        friend_request_sent_refresh_layout.setColorSchemeColors(Color.RED);
        friend_request_sent_refresh_layout.setOnRefreshListener(() -> {
            Log.d("--", "on refresh ");
            refreshListFriendRequest();
            FriendRequestActivity activity = (FriendRequestActivity) getActivity();
            if (activity != null)
                activity.countFriendRequestSent();
        });

        btnLoadMore.setOnClickListener(v -> {
            loadMoreData();
        });

        return view;
    }

    private void shouldShowMessageEmpty() {
        if (totalElements == 0) {
            txt_friend_request_sent_no_request.setVisibility(View.VISIBLE);
        } else {
            txt_friend_request_sent_no_request.setVisibility(View.GONE);
        }
    }

    private void loadMoreData() {
        page++;
        getListFriendRequest();
    }

    private void refreshListFriendRequest() {
        page = 0;
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST + "/sent" + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        totalElements = (int) object.get("totalElements");
                        boolean last = (boolean) object.get("last");
                        if (last)
                            btnLoadMore.setVisibility(View.GONE);
                        else
                            btnLoadMore.setVisibility(View.VISIBLE);
                        parent.updateCountSearchResult(POSITION_OF_SENT, totalElements);
                        Type listType = new TypeToken<List<FriendRequest>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        Log.d("--sent", list.toString());
                        adapter.setList(list);
                        shouldShowMessageEmpty();
                        friend_request_sent_refresh_layout.setRefreshing(false);
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    private void getListFriendRequest() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST + "/sent" + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        totalElements = (int) object.get("totalElements");
                        boolean last = (boolean) object.get("last");
                        if (last)
                            btnLoadMore.setVisibility(View.GONE);
                        else
                            btnLoadMore.setVisibility(View.VISIBLE);
                        parent.updateCountSearchResult(POSITION_OF_SENT, totalElements);
                        Type listType = new TypeToken<List<FriendRequest>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        Log.d("--sent", list.toString());
                        if (!list.isEmpty()) {
                            if (page == 0)
                                adapter.setList(list);
                            else
                                adapter.updateList(list);
                        }
                        if (page != 0) {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                        shouldShowMessageEmpty();
                        friend_request_sent_refresh_layout.setRefreshing(false);
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void deleteFriendRequest(FriendRequest friendRequest) {
        boolean delete = list.removeIf(x -> x.getTo() != null && x.getTo().getId().equals(friendRequest.getTo().getId()));
        if (delete)
            totalElements--;
        adapter.setList(list);
        shouldShowMessageEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void acceptFriendRequest(FriendRequest friendRequest) {
        boolean remove = list.removeIf(x -> x.getTo() != null && x.getTo().getId().equals(friendRequest.getTo().getId()));
        if (remove)
            totalElements--;
        adapter.setList(list);
        shouldShowMessageEmpty();
    }

    public void sentFriendRequest(FriendRequest friendRequest) {
        if (list == null)
            list = new ArrayList<>();
        boolean add = list.add(friendRequest);
        if (add)
            totalElements++;
        adapter.setList(list);
        shouldShowMessageEmpty();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(friendRequestSentEmpty);
        super.onDestroy();
    }

}