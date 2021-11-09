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
import com.example.chatapp.adapter.FriendRequestReceivedAdapter;
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

public class FriendRequestReceivedFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private String token;
    private Gson gson;
    private FriendRequestReceivedAdapter adapter;
    private List<FriendRequest> list;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout friend_request_received_refresh_layout;
    private TextView txt_friend_request_receiver_no_request;
    private int page = 0;
    private final int size = 20;
    private boolean scroll = false;
    private final FriendRequestActivity parent;
    private final int POSITION_OF_RECEIVED = 0;

    public FriendRequestReceivedFragment(FriendRequestActivity parent) {
        this.parent = parent;
    }

    public static FriendRequestReceivedFragment newInstance(String param1, String param2, FriendRequestActivity parent) {
        FriendRequestReceivedFragment fragment = new FriendRequestReceivedFragment(parent);
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
        khi list ở adapter trống thì hiện dòng text thông báo
         */
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean empty = intent.getBooleanExtra("dto", true);
                if (empty)
                    txt_friend_request_receiver_no_request.setVisibility(View.VISIBLE);
                else
                    txt_friend_request_receiver_no_request.setVisibility(View.GONE);
            }
        }, new IntentFilter("friendRequest/received/empty"));

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
        View view = inflater.inflate(R.layout.fragment_friend_request_received, container, false);

        recyclerView = view.findViewById(R.id.lsv_friend_request_received);
        txt_friend_request_receiver_no_request = view.findViewById(R.id.txt_friend_request_receiver_no_request);
        txt_friend_request_receiver_no_request.setVisibility(View.GONE);
        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FriendRequestReceivedAdapter(getContext(), list, token);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        getListFriendRequest();

        /*
        sự kiện kéo để làm mới
         */
        friend_request_received_refresh_layout = view.findViewById(R.id.friend_request_received_refresh_layout);
        friend_request_received_refresh_layout.setColorSchemeColors(Color.RED);
        friend_request_received_refresh_layout.setOnRefreshListener(() -> {
            page = 0;
            Log.d("--", "on refresh ");
            refreshListFriendRequest();
            FriendRequestActivity activity = (FriendRequestActivity) getActivity();
            if (activity != null)
                activity.countFriendRequestReceived();
        });

        // scroll event
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scroll = !recyclerView.canScrollVertically(1) && dy > 0;
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && scroll) {
                    loadMoreData();
                }
            }
        });

        return view;
    }

    private void refreshListFriendRequest() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        int totalElements = (int) object.get("totalElements");
                        parent.updateCountSearchResult(POSITION_OF_RECEIVED, totalElements);
                        Type listType = new TypeToken<List<FriendRequest>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        Log.d("--received", list.toString());
                        adapter.setList(list);
                        if (recyclerView.getAdapter().getItemCount() == 0) {
                            txt_friend_request_receiver_no_request.setVisibility(View.VISIBLE);
                        } else {
                            txt_friend_request_receiver_no_request.setVisibility(View.GONE);
                        }
                        friend_request_received_refresh_layout.setRefreshing(false);
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

    private void loadMoreData() {
        page++;
        getListFriendRequest();
    }

    private void getListFriendRequest() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_REQUEST + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        int totalElements = (int) object.get("totalElements");
                        parent.updateCountSearchResult(POSITION_OF_RECEIVED, totalElements);
                        Type listType = new TypeToken<List<FriendRequest>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        Log.d("--received", list.toString());
                        if (!list.isEmpty()) {
                            if (page == 0)
                                adapter.setList(list);
                            else
                                adapter.updateList(list);
                        }
                        if (page != 0) {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                        if (recyclerView.getAdapter().getItemCount() == 0) {
                            txt_friend_request_receiver_no_request.setVisibility(View.VISIBLE);
                        } else {
                            txt_friend_request_receiver_no_request.setVisibility(View.GONE);
                        }
                        friend_request_received_refresh_layout.setRefreshing(false);
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
    public void recallFriendRequest(FriendRequest friendRequest) {
        list.removeIf(x -> x.getFrom() != null && x.getFrom().getId().equals(friendRequest.getFrom().getId()));
        adapter.setList(list);
    }

    public void receivedFriendRequest(FriendRequest friendRequest) {
        if (list == null)
            list = new ArrayList<>();
        list.add(friendRequest);
        adapter.setList(list);
    }

}