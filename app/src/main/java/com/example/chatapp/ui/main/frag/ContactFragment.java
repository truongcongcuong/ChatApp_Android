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
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.NestedScrollView;
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
import com.example.chatapp.adapter.FriendListAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.FriendDTO;
import com.example.chatapp.ui.AddFriendActivity;
import com.example.chatapp.ui.FriendRequestActivity;
import com.example.chatapp.ui.SyncContactActivity;
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

public class ContactFragment extends Fragment {

    private FriendListAdapter adapter;
    private String token;
    private Gson gson;
    private List<FriendDTO> list;
    private List<FriendDTO> searchFriend;
    private NestedScrollView scrollView;
    private Button btnLoadMore;
    private final int size = 1;
    private int page = 0;
    private int pageSearch = 0;
    private TextView contact_frg_count;
    private Timer timer;
    private final int DELAY_SEARCH = 250;

    /*
    kéo để làm mới
     */
    private SwipeRefreshLayout refreshLayout;

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
        /*
        khi đồng ý kết bạn ở activity friend request thì contact fragment load lại danh sách bạn bè
         */
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateListFriends();
            }
        }, new IntentFilter("accept_friend"));
        /*
        enable menu trên action bar
         */
        setHasOptionsMenu(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        searchFriend = new ArrayList<>(0);
        gson = new Gson();
        timer = new Timer();
        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(getActivity().getApplicationContext());
        getNewAccessToken.sendGetNewTokenRequest();
        SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        RecyclerView rcv_contact_list = view.findViewById(R.id.rcv_contact_list);
        ConstraintLayout ctl_contact_phone_book_friend = view.findViewById(R.id.ctl_contact_phone_book_friend);
        ConstraintLayout ctl_contact_friend_request = view.findViewById(R.id.ctl_contact_friend_request);
        contact_frg_count = view.findViewById(R.id.contact_frg_count);
        Button btn_contact_refresh = view.findViewById(R.id.btn_contact_refresh);
        btnLoadMore = view.findViewById(R.id.contact_frg_btn_load_more);
        btnLoadMore.setVisibility(View.GONE);
        ctl_contact_phone_book_friend.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SyncContactActivity.class);
            startActivity(intent);
        });
        ctl_contact_friend_request.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendRequestActivity.class);
            startActivity(intent);
        });
        rcv_contact_list.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        this.adapter = new FriendListAdapter(list, getActivity());
        rcv_contact_list.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcv_contact_list.getContext(), DividerItemDecoration.VERTICAL);
        rcv_contact_list.addItemDecoration(dividerItemDecoration);

        btn_contact_refresh.setOnClickListener(v -> {
            refreshLayout.setRefreshing(true);
            page = 0;
            pageSearch = 0;
            timer.cancel();
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            updateListFriends();
                        }
                    },
                    1500
            );
        });

        /*
        sự kiện kéo để làm mới
         */
        refreshLayout = view.findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeColors(Color.RED);
        refreshLayout.setOnRefreshListener(() -> {
            page = 0;
            pageSearch = 0;
            updateListFriends();
        });

        scrollView = view.findViewById(R.id.nested_scroll_contact_fragment);
        updateListFriends();
        scrollView.post(() -> scrollView.scrollTo(0, 0));

        return view;
    }

    private void loadMoreData() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        int totalElements = (int) object.get("totalElements");
                        shouldShowButtonLoadMore(last, totalElements);
                        Type listType = new TypeToken<List<FriendDTO>>() {
                        }.getType();
                        List<FriendDTO> listMore = gson.fromJson(array.toString(), listType);

                        list.addAll(listMore);
                        adapter.notifyDataSetChanged();
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
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    private void shouldShowButtonLoadMore(boolean last, int totalElements) {
        contact_frg_count.setText(String.format("%s (%d)", getString(R.string.all_contact), totalElements));
        if (last)
            btnLoadMore.setVisibility(View.GONE);
        else
            btnLoadMore.setVisibility(View.VISIBLE);
    }

    private void updateListFriends() {
        btnLoadMore.setOnClickListener(v -> {
            page++;
            loadMoreData();
        });
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        int totalElements = (int) object.get("totalElements");
                        shouldShowButtonLoadMore(last, totalElements);
                        Type listType = new TypeToken<List<FriendDTO>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);

                        adapter.setList(list);
                        refreshLayout.setRefreshing(false);
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
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        requestQueue.add(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contact_fragment, menu);
        MenuItem menuItemSearch = menu.findItem(R.id.search_friend_contact_fragment);
        MenuItem menuItemAdd = menu.findItem(R.id.add_friend_contact_fragment);

        View actionView = menuItemSearch.getActionView();

        SearchView searchView = (SearchView) actionView;
        searchView.setQueryHint(getString(R.string.search_friends));
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

        menuItemAdd.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(getActivity().getApplicationContext(), AddFriendActivity.class));
            return true;
        });

        /*
        sự kiện click icon close trên search view
         */
        closeIcon.setOnClickListener(v -> {
            pageSearch = 0;
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
                /*
                nếu text không rỗng thì tìm, ngược lại xóa recyclerview
                 */
                if (!newText.trim().isEmpty()) {
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    search(newText);
                                }
                            },
                            DELAY_SEARCH
                    );
                } else {
                    pageSearch = 0;
                    btnLoadMore.setVisibility(View.GONE);
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        searchFriend.clear();
                                        adapter.setList(list);
                                    } catch (Exception e) {
                                    }
                                }
                            },
                            0
                    );
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
                    searchFriend.clear();
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
        btnLoadMore.setOnClickListener(v -> {
            pageSearch++;
            search(newText);
        });
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST + "?query=" + newText + "&size=" + size + "&page=" + pageSearch,
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
                        Type listType = new TypeToken<List<FriendDTO>>() {
                        }.getType();
                        if (pageSearch == 0) {
                            searchFriend = gson.fromJson(array.toString(), listType);
                            adapter.setList(searchFriend);
                        } else {
                            List<FriendDTO> searchList = gson.fromJson(array.toString(), listType);
                            searchFriend.addAll(searchList);
                            adapter.notifyDataSetChanged();
                        }
                        refreshLayout.setRefreshing(false);
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
    }

}