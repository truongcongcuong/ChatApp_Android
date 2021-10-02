package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.ListMessageAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.enumvalue.RoomType;
import com.example.chatapp.ui.CreateGroupActivity;
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
import java.util.stream.Collectors;

public class GroupFragment extends Fragment {

    private RecyclerView rcv_list_group;
    private ListMessageAdapter adapter;
    private List<InboxDto> list;
    private List<InboxDto> searchGroup;
    private Gson gson;
    private String token;
    private int page = 0;
    private int size = 20;
    private final String type = RoomType.GROUP.toString();

    /*
    kéo để làm mới
     */
    private SwipeRefreshLayout refreshLayout;

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
        /*
        enable menu trên action bar
         */
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        searchGroup = new ArrayList<>(0);
        gson = new Gson();
        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(getActivity().getApplicationContext());
        getNewAccessToken.sendGetNewTokenRequest();
        SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        /*
        sự kiện kéo để làm mới
         */
        refreshLayout = view.findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeColors(Color.RED);
        refreshLayout.setOnRefreshListener(() -> {
            page = 0;
            refreshListInboxGroup();
        });

        rcv_list_group = view.findViewById(R.id.rcv_list_group);
        rcv_list_group.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        this.adapter = new ListMessageAdapter(getActivity().getApplicationContext(), new ArrayList<>(0));
        this.rcv_list_group.setAdapter(adapter);
        updateListInboxGroup();
        return view;
    }

    private void refreshListInboxGroup() {
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size + "&type=" + type,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        adapter.setList(list);
                        refreshLayout.setRefreshing(false);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListInboxGroup() {
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size + "&type=" + type,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        adapter.setList(list);
//                        this.adapter = new ListMessageAdapter(getActivity().getApplicationContext(), list);
//                        this.rcv_list_group.setAdapter(adapter);

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_fragment, menu);
        MenuItem menuItemSearch = menu.findItem(R.id.search_group_fragment);
        MenuItem menuItemCreate = menu.findItem(R.id.create_group_fragment);

        View actionView = menuItemSearch.getActionView();

        SearchView searchView = (SearchView) actionView;
        searchView.setQueryHint("Search group...");
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.requestFocus();

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        searchPlate.setBackgroundResource(R.drawable.search_view_background_light);
        ViewGroup.LayoutParams params = searchPlate.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        searchPlate.setLayoutParams(params);

        int searchIcon = searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView magImage = searchView.findViewById(searchIcon);
        magImage.setVisibility(View.GONE);
        magImage.setImageDrawable(null);

        /*
        tìm icon close và edit text của search view
         */
        int closeIconId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        int editTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        View closeIcon = searchView.findViewById(closeIconId);
        EditText editText = searchView.findViewById(editTextId);
        editText.setHintTextColor(Color.WHITE);
        editText.setPadding(50, 0, 50, 0);

        menuItemCreate.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
            getActivity().startActivity(intent);
            return true;
        });

        /*
        sự kiện click icon close trên search view
         */
        closeIcon.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "icon close", Toast.LENGTH_SHORT).show();
            adapter.setList(list);
            editText.setText("");
        });

        /*
        sự kiện gõ trên search view
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("---submit", query);
                onQueryTextChange(query);
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("---change", newText);

                if (!newText.isEmpty()) {
                    search(newText);
                } else {
                    adapter.setList(list);
                }

                return false;
            }
        });

        /*
        sự kiện click vào để mở rộng search view, và sự kiện click mũi tên để thu gọn search view
         */
        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                /*
                sự kiện thu gọn search view
                 */
                if (adapter != null) {
                    adapter.setList(list);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d("----expand", "ok");
                /*
                sự kiện mở rộng search view
                 */
                try {
                    searchGroup.clear();
//                    adapter.setList(searchFriend);
                } catch (Exception e) {

                }
                return true;
            }
        };

        MenuItemCompat.setOnActionExpandListener(menuItemSearch, expandListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void search(String newText) {
        searchGroup = list.stream().filter(x -> x.getRoom().getName().toLowerCase()
                .contains(newText.toLowerCase()))
                .collect(Collectors.toList());
        adapter.setList(searchGroup);
    }
}