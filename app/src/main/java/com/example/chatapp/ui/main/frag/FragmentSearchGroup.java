package com.example.chatapp.ui.main.frag;

import android.content.Context;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.LineItemGroupAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.enumvalue.RoomType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentSearchGroup extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private final Context context;
    private Button btn_load_more;
    private int pageSearch = 0;
    private final int size = 5;
    private final Gson gson;
    private final String token;
    private List<InboxDto> inboxList;
    private LineItemGroupAdapter adapter;
    private String query;
    private final MessageFragmentSearch parent;
    private Timer timer;
    private static final int DELAY_SEARCH = 300;

    public FragmentSearchGroup(Context context, MessageFragmentSearch parent) {
        this.context = context;
        this.parent = parent;
        gson = new Gson();
        timer = new Timer();
        SharedPreferences sharedPreferencesToken = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    public static FragmentSearchFriend newInstance(String param1, String param2, Context context, MessageFragmentSearch parent) {
        FragmentSearchFriend fragment = new FragmentSearchFriend(context, parent);
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
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_group, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.frg_search_group_rcv);
        btn_load_more = view.findViewById(R.id.frg_search_group_btn_load_more);
        btn_load_more.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new LineItemGroupAdapter(context, null, Color.DKGRAY);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        btn_load_more.setOnClickListener(v -> {
            pageSearch++;
            doSearch(query);
        });
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void search(String s) {
        query = s;
        if (!query.trim().isEmpty()) {
            pageSearch = 0;
            timer.cancel();
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            doSearch(query);
                        }
                    },
                    DELAY_SEARCH
            );
        } else {
            btn_load_more.setVisibility(View.GONE);
            parent.updateCountSearchResult(1, 0);
            try {
                inboxList.clear();
                adapter.notifyDataSetChanged();
            } catch (Exception ignore) {

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void doSearch(String newText) {
        if (!newText.trim().isEmpty()) {
            StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX
                    + "?query=" + newText + "&size=" + size + "&page=" + pageSearch + "&type=" + RoomType.GROUP.toString(),
                    response -> {
                        try {
                            String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                            JSONObject object = new JSONObject(res);
                            JSONArray array = (JSONArray) object.get("content");
                            boolean last = (boolean) object.get("last");
                            int totalElements = (int) object.get("totalElements");
                            parent.updateCountSearchResult(1, totalElements);
                            if (last)
                                btn_load_more.setVisibility(View.GONE);
                            else
                                btn_load_more.setVisibility(View.VISIBLE);
                            Type listType = new TypeToken<List<InboxDto>>() {
                            }.getType();
                            List<InboxDto> searchGroup = gson.fromJson(array.toString(), listType);
                            if (pageSearch == 0) {
                                inboxList = searchGroup;
                            } else {
                                inboxList.addAll(searchGroup);
                            }
                            adapter.setList(inboxList);
                        } catch (UnsupportedEncodingException | JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.i("list group error", error.toString())) {
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
    }
}