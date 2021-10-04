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
import android.widget.PopupMenu;
import android.widget.SearchView;

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
import com.example.chatapp.adapter.SearchUserAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.InboxDto;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.dto.UserSummaryDTO;
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

public class MessageFragment extends Fragment {
    private RecyclerView rcv_list_message;
    private ListMessageAdapter adapter;
    private Gson gson;
    private UserSummaryDTO user;
    private String token;
    private int page = 0;
    private int size = 20;

    private SearchUserAdapter searchUserAdapter;
    private List<UserProfileDto> searchUserResult;

    /*
    kéo để làm mới
     */
    private SwipeRefreshLayout refreshLayout;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

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
        /*
        enable menu trên action bar
         */
        setHasOptionsMenu(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        gson = new Gson();

        SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        SharedPreferences sharedPreferencesUser = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String userJson = sharedPreferencesUser.getString("user-info", null);
        user = gson.fromJson(userJson, UserSummaryDTO.class);

        searchUserResult = new ArrayList<>(0);
        searchUserAdapter = new SearchUserAdapter(getActivity().getApplicationContext(), searchUserResult);
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
        /*
        chỉ khi nào fragment được hiển thị thì mới hiện menu
         */
        setHasOptionsMenu(isVisible());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        rcv_list_message = view.findViewById(R.id.rcv_list_message);
        rcv_list_message.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        this.adapter = new ListMessageAdapter(getActivity().getApplicationContext(), new ArrayList<>());
        this.rcv_list_message.setAdapter(adapter);
        updateListInbox();

        /*
        sự kiện kéo để làm mới
         */
        refreshLayout = view.findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeColors(Color.RED);
        refreshLayout.setOnRefreshListener(() -> {
            page = 0;
            refreshListInbox();
        });

        rcv_list_message.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                /*LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null) {
                    *//*
                    chỉ enable khi ở đầu của recyclerview
                     *//*
                    refreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() > 0);
                }*/
                /*
                nếu cuộn xuống cuối recyclerview thì load thêm data
                 */
                if (!recyclerView.canScrollVertically(1)) {
                    loadMoreData();
                }
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // scroll to top
                }
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // scroll to bottom
                    loadMoreData();
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // scrolling
                }
            }
        });
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void refreshListInbox() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");

                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        List<InboxDto> list = gson.fromJson(array.toString(), listType);
                        if (!list.isEmpty()) {
                            adapter = new ListMessageAdapter(getActivity().getApplicationContext(), list);
                            this.rcv_list_message.setAdapter(adapter);

                        }
                        refreshLayout.setRefreshing(false);
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
    private void loadMoreData() {
        page++;
        updateListInbox();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateListInbox() {
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");

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

    /**
     * menu search user
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.search_user_fragment_message);

        View actionView = menuItem.getActionView();

        SearchView searchView = (SearchView) actionView;
        searchView.setQueryHint("Search user...");
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

        menu.findItem(R.id.submenu_fragment_message).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                View view = getActivity().findViewById(item.getItemId());

                final PopupMenu popupMenu = new PopupMenu(getActivity().getApplicationContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_message_fragment, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_create_group:
                                Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
                                getActivity().startActivity(intent);
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();

                return true;
            }
        });

        /*
        sự kiện click icon close trên search view
         */
        closeIcon.setOnClickListener(v -> {
            try {
                searchUserResult.clear();
                searchUserAdapter.setList(searchUserResult);
            } catch (Exception e) {
                searchUserAdapter = new SearchUserAdapter(getActivity().getApplicationContext(), null);
            }
            editText.setText("");
        });

        /*
        sự kiện gõ trên search view
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("---submit", query);
                onQueryTextChange(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("---change", newText);

                if (!newText.isEmpty()) {
                    search(newText);
                } else {
//                    try {
//                        searchUserResult.clear();
//                        searchUserAdapter.setList(searchUserResult);
//                    } catch (Exception e) {
//                        searchUserAdapter = new SearchUserAdapter(getActivity().getApplicationContext(), null);
//                    }
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
                    rcv_list_message.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d("----expand", "ok");
                /*
                sự kiện mở rộng search view
                 */
//                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
//                if (rcv_list_message.getLayoutManager() == null)
//                    rcv_list_message.setLayoutManager(layoutManager);
//                rcv_list_message.setAdapter(searchUserAdapter);
//                searchUserAdapter.setList(searchUserResult);
                return true;
            }
        };

        MenuItemCompat.setOnActionExpandListener(menuItem, expandListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void search(String newText) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_USER + "search",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        Type listType = new TypeToken<List<UserProfileDto>>() {
                        }.getType();
                        searchUserResult = new Gson().fromJson(res, listType);
                        Log.d("", searchUserResult.toString());

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
                        if (rcv_list_message.getLayoutManager() == null)
                            rcv_list_message.setLayoutManager(layoutManager);
                        rcv_list_message.setAdapter(searchUserAdapter);
                        searchUserAdapter.setList(searchUserResult);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("search friend error", error.toString())) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("textToSearch", newText);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.getCache().clear();
        requestQueue.add(request);
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }
}