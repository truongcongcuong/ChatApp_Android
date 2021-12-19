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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.example.chatapp.adapter.MenuButtonAdapterVertical;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.FriendDTO;
import com.example.chatapp.dto.FriendDeleteDto;
import com.example.chatapp.dto.MyMenuItem;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.FriendRequest;
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
    private final int size = 10;
    private int page = 0;
    private int pageSearch = 0;
    private TextView contact_frg_count;
    private Timer timer;
    private final int DELAY_SEARCH = 250;
    private List<MyMenuItem> myMenuItems;
    private MenuButtonAdapterVertical menuAdapter;
    private Button btn_contact_refresh;
    private int totalElements = 0;
    private UserSummaryDTO currentUser;

    /*
    kéo để làm mới
     */
    private SwipeRefreshLayout refreshLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

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
                    initListMenuItems();
                    menuAdapter.setItems(myMenuItems);
                    menuAdapter.notifyDataSetChanged();
                    btn_contact_refresh.setText(getString(R.string.refresh));
                    contact_frg_count.setText(String.format("%s (%d)", getString(R.string.all_contact), totalElements));
                    btnLoadMore.setText(getString(R.string.load_more));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    /*
    sự kiện đồng ý kết bạn
     */
    private final BroadcastReceiver acceptFriend = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                FriendDTO friendDTO = null;
                if (currentUser.getId().equals(dto.getFrom().getId())) {
                    friendDTO = new FriendDTO();
                    friendDTO.setFriend(dto.getTo());
                    friendDTO.setCreateAt(dto.getCreateAt());
                } else if (currentUser.getId().equals(dto.getTo().getId())) {
                    friendDTO = new FriendDTO();
                    friendDTO.setFriend(dto.getFrom());
                    friendDTO.setCreateAt(dto.getCreateAt());
                }
                if (friendDTO != null) {
                    int oldSize = list.size();
                    list.add(0, friendDTO);
                    if (list.size() > oldSize) {
                        adapter.notifyItemInserted(0);
                        totalElements++;
                    }
                }
                contact_frg_count.setText(String.format("%s (%d)", getString(R.string.all_contact), totalElements));
            }
        }
    };

    /*
    sự kiện xóa bạn bè
     */
    private final BroadcastReceiver deleteFriend = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendDeleteDto dto = (FriendDeleteDto) bundle.getSerializable("dto");
                if (currentUser.getId().equals(dto.getUserId())) {
                    int index = -1;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getFriend().getId().equals(dto.getFriendId())) {
                            index = i;
                            break;
                        }
                    }
                    boolean remove = list.removeIf(x -> x.getFriend().getId().equals(dto.getFriendId()));
                    if (remove) {
                        adapter.notifyItemRemoved(index);
                        totalElements = totalElements > 0 ? totalElements - 1 : totalElements;
                    }
                } else if (currentUser.getId().equals(dto.getFriendId())) {
                    int index = -1;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getFriend().getId().equals(dto.getUserId())) {
                            index = i;
                            break;
                        }
                    }
                    boolean remove = list.removeIf(x -> x.getFriend().getId().equals(dto.getUserId()));
                    if (remove) {
                        adapter.notifyItemRemoved(index);
                        totalElements = totalElements > 0 ? totalElements - 1 : totalElements;
                    }
                }
                contact_frg_count.setText(String.format("%s (%d)", getString(R.string.all_contact), totalElements));
            }
        }
    };

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

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(changeLanguage, new IntentFilter("language/change"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(acceptFriend, new IntentFilter("friendRequest/accept"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(deleteFriend, new IntentFilter("friends/delete"));
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

        gson = new Gson();
        SharedPreferences sharedPreferencesUser = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        currentUser = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        RecyclerView rcv_contact_list = view.findViewById(R.id.rcv_contact_list);
        ListView lv_info_fragment_menu = view.findViewById(R.id.contact_frg_lv_menu);

        initListMenuItems();

        menuAdapter = new MenuButtonAdapterVertical(getActivity(), R.layout.line_item_menu_contact_fragment, myMenuItems);
        lv_info_fragment_menu.setAdapter(menuAdapter);
        lv_info_fragment_menu.setOnItemClickListener((parent, view1, position, itemId) -> {
            MyMenuItem item = myMenuItems.get(position);
            if (item.getKey().equals("friendRequest")) {
                Intent intent = new Intent(getActivity(), FriendRequestActivity.class);
                startActivity(intent);
            } else if (item.getKey().equals("syncContact")) {
                Intent intent = new Intent(getActivity(), SyncContactActivity.class);
                startActivity(intent);
            }
        });

        contact_frg_count = view.findViewById(R.id.contact_frg_count);
        contact_frg_count.setText(String.format("%s (%d)", getString(R.string.all_contact), totalElements));
        btn_contact_refresh = view.findViewById(R.id.btn_contact_refresh);
        btnLoadMore = view.findViewById(R.id.contact_frg_btn_load_more);
        btnLoadMore.setVisibility(View.GONE);

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

        setListViewHeightBasedOnChildren(lv_info_fragment_menu);

        return view;
    }

    private void initListMenuItems() {
        try {
            myMenuItems.clear();
        } catch (Exception e) {
            myMenuItems = new ArrayList<>();
        }
        myMenuItems.add(MyMenuItem.builder()
                .key("friendRequest")
                .imageResource(R.drawable.ic_baseline_profile_circle_24_orange)
                .name(getString(R.string.friend_request))
                .build());

        myMenuItems.add(MyMenuItem.builder()
                .key("syncContact")
                .imageResource(R.drawable.ic_baseline_contact_phone_24)
                .name(getString(R.string.phone_book_friend))
                .build());

    }

    private void loadMoreData() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST + "?size=" + size + "&page=" + page,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        boolean last = (boolean) object.get("last");
                        totalElements = (int) object.get("totalElements");
                        shouldShowButtonLoadMore(last);
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

    private void shouldShowButtonLoadMore(boolean last) {
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
                        totalElements = (int) object.get("totalElements");
                        shouldShowButtonLoadMore(last);
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

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(changeLanguage);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(acceptFriend);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(deleteFriend);
        super.onDestroy();
    }

    // set dynamic height for list view
    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}