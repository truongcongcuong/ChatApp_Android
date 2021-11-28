package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
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
import com.example.chatapp.adapter.MediaActivityAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.enumvalue.MediaType;
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

public class MediaFileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private String token;
    private Gson gson;
    private MediaActivityAdapter adapter;
    private List<MessageDto> list;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout media_file_refresh_layout;
    private int page = 0;
    private final int size = 1;
    private Button btnLoadMore;
    private InboxDto inboxDto;
    private final String typeFile = MediaType.FILE.toString();

    public static MediaFileFragment newInstance(String param1, String param2, InboxDto inboxDto) {
        MediaFileFragment fragment = new MediaFileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putSerializable("inboxDto", inboxDto);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            inboxDto = (InboxDto) getArguments().getSerializable("inboxDto");
        }
        SharedPreferences sharedPreferencesToken = getActivity().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
        gson = new Gson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_file, container, false);

        recyclerView = view.findViewById(R.id.rcv_media_file);
        btnLoadMore = view.findViewById(R.id.media_file_btn_load_more);
        btnLoadMore.setVisibility(View.GONE);
        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MediaActivityAdapter(getActivity(), list, token);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        getListMessage();

        /*
        sự kiện kéo để làm mới
         */
        media_file_refresh_layout = view.findViewById(R.id.media_file_refresh_layout);
        media_file_refresh_layout.setColorSchemeColors(Color.RED);
        media_file_refresh_layout.setOnRefreshListener(() -> {
            page = 0;
            getListMessage();
        });

        btnLoadMore.setOnClickListener(v -> {
            loadMoreData();
        });

        return view;
    }

    private void loadMoreData() {
        page++;
        getListMessage();
    }

    private void getListMessage() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_MESSAGE + "media/" + inboxDto.getRoom().getId()
                + "?size=" + size + "&page=" + page
                + "&type=" + typeFile,
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
                        Type listType = new TypeToken<List<MessageDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        if (!list.isEmpty()) {
                            if (page == 0)
                                adapter.setList(list);
                            else
                                adapter.updateList(list);
                        }
                        if (page != 0) {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                        media_file_refresh_layout.setRefreshing(false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}