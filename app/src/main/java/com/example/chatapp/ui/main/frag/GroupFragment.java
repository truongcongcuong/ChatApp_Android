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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuItemCompat;
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
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.RoomDTO;
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
import java.util.Timer;
import java.util.TimerTask;

public class GroupFragment extends Fragment {

    private ListMessageAdapter adapter;
    private List<InboxDto> list;
    private List<InboxDto> searchGroup;
    private Gson gson;
    private String token;
    private int page = 0;
    private final int size = 1;
    private final String type = RoomType.GROUP.toString();

    private Button btnLoadMore;
    private int pageSearch = 0;
    private Timer timer;
    private final int DELAY_SEARCH = 250;
    private LinearLayout layout_search;
    private LinearLayout layout_content;
    private TextView search_message;
    private String message;
    private String no_result;

    /*
    kéo để làm mới
     */
    private SwipeRefreshLayout refreshLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

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
        searchGroup = new ArrayList<>(0);
        gson = new Gson();
        timer = new Timer();
        message = getString(R.string.search_group);
        no_result = getString(R.string.no_result);
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
            pageSearch = 0;
            refreshListInboxGroup();
        });

        RecyclerView rcv_list_group = view.findViewById(R.id.rcv_list_group);
        btnLoadMore = view.findViewById(R.id.group_frg_btn_load_more);
        btnLoadMore.setVisibility(View.GONE);

        layout_search = view.findViewById(R.id.layout_search);
        layout_search.setVisibility(View.GONE);
        search_message = view.findViewById(R.id.txt_search_notify);
        search_message.setText(message);

        layout_content = view.findViewById(R.id.group_fragment_layout_recyclerview);
        hideLayoutSearch();

        list = new ArrayList<>();
        rcv_list_group.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        this.adapter = new ListMessageAdapter(getActivity().getApplicationContext(), null);
        rcv_list_group.setAdapter(adapter);
        updateListInboxGroup();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcv_list_group.getContext(), DividerItemDecoration.VERTICAL);
        rcv_list_group.addItemDecoration(dividerItemDecoration);

        return view;
    }

    private void refreshListInboxGroup() {
        btnLoadMore.setOnClickListener(v -> {
            page++;
            updateListInboxGroup();
        });
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size + "&type=" + type,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        visibleBtnLoadMore(last);
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);
                        if (!list.isEmpty()) {
                            hideLayoutSearch();
                        }
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
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    private void hideLayoutSearch() {
        layout_content.setVisibility(View.VISIBLE);
        layout_search.setVisibility(View.GONE);
    }

    private void showLayoutSearch() {
        layout_content.setVisibility(View.GONE);
        layout_search.setVisibility(View.VISIBLE);
    }

    private void visibleBtnLoadMore(boolean last) {
        if (last)
            btnLoadMore.setVisibility(View.GONE);
        else
            btnLoadMore.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListInboxGroup() {
        btnLoadMore.setOnClickListener(v -> {
            page++;
            updateListInboxGroup();
        });
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size + "&type=" + type,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        visibleBtnLoadMore(last);
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        List<InboxDto> newList = gson.fromJson(array.toString(), listType);
                        if (!newList.isEmpty()) {
                            if (page == 0) {
                                list = newList;
                            } else {
                                list.addAll(newList);
                            }
                            adapter.setList(list);
                        }
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
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_fragment, menu);
        MenuItem menuItemSearch = menu.findItem(R.id.search_group_fragment);
        MenuItem menuItemCreate = menu.findItem(R.id.create_group_fragment);

        View actionView = menuItemSearch.getActionView();

        SearchView searchView = (SearchView) actionView;
        searchView.setQueryHint(getString(R.string.search_group));
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
        ImageView closeIcon = searchView.findViewById(closeIconId);
        closeIcon.setImageResource(R.drawable.ic_baseline_close_circle_24_white);
        EditText editText = searchView.findViewById(editTextId);
        editText.setHintTextColor(Color.WHITE);
        editText.setTextColor(Color.WHITE);
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
            try {
                searchGroup.clear();
            } catch (Exception ignore) {
            }
//            adapter.setList(list);
            editText.setText("");
            showLayoutSearch();
            search_message.setText(message);
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
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        searchGroup.clear();
                                    } catch (Exception ignore) {

                                    }
                                    search(newText);
                                }
                            },
                            DELAY_SEARCH
                    );
                } else {
                    timer.cancel();
//                    adapter.setList(list);
                    showLayoutSearch();
                    search_message.setText(message);
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
                hideLayoutSearch();
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
                } catch (Exception e) {

                }
                showLayoutSearch();
                return true;
            }
        };

        MenuItemCompat.setOnActionExpandListener(menuItemSearch, expandListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void search(String newText) {
        btnLoadMore.setOnClickListener(v -> {
            pageSearch++;
            search(newText);
        });
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX
                + "?page=" + pageSearch + "&size=" + size + "&type=" + type + "&query=" + newText,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        visibleBtnLoadMore(last);
                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        List<InboxDto> newList = gson.fromJson(array.toString(), listType);
                        System.out.println(newList);
                        if (!newList.isEmpty()) {
                            if (pageSearch == 0) {
                                searchGroup = newList;
                            } else {
                                searchGroup.addAll(newList);
                            }
                        }
                        adapter.setList(searchGroup);
                        if (searchGroup.isEmpty()) {
                            showLayoutSearch();
                            search_message.setText(no_result);
                        } else {
                            hideLayoutSearch();
                        }
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
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    /*
    chỉ khi nào fragment được hiển thị thì mới hiện menu
     */
    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(isVisible());
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(renameRoom);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(changeImageRoom);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(changeLanguage);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setNewMessage(MessageDto newMessage) {
        adapter.setNewMessage(newMessage);
    }

}