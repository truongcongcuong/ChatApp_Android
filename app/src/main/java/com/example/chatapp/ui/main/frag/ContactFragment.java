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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.FriendListAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.FriendDTO;
import com.example.chatapp.ui.FriendRequestActivity;
import com.example.chatapp.ui.TestNewFeatureActivity;
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

public class ContactFragment extends Fragment {

    private RecyclerView rcv_contact_list;
    private FriendListAdapter adapter;
    private String token;
    private Gson gson;
    private List<FriendDTO> list;
    private ConstraintLayout ctl_contact_friend_request;
    private ConstraintLayout ctl_contact_phone_book_friend;
    private Button btn_contact_refresh;
    private List<FriendDTO> searchFriend;

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
        enable menu trên action bar
         */
        setHasOptionsMenu(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        searchFriend = new ArrayList<>(0);
        gson = new Gson();
        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(getActivity().getApplicationContext());
        getNewAccessToken.sendGetNewTokenRequest();
        SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        rcv_contact_list = view.findViewById(R.id.rcv_contact_list);
        ctl_contact_phone_book_friend = view.findViewById(R.id.ctl_contact_phone_book_friend);
        ctl_contact_friend_request = view.findViewById(R.id.ctl_contact_friend_request);
        btn_contact_refresh = view.findViewById(R.id.btn_contact_refresh);
        ctl_contact_phone_book_friend.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TestNewFeatureActivity.class);
            startActivity(intent);
        });
        ctl_contact_friend_request.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendRequestActivity.class);
            startActivity(intent);
        });
        rcv_contact_list.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        this.adapter = new FriendListAdapter(list, getActivity().getApplicationContext());
        this.rcv_contact_list.setAdapter(adapter);

        btn_contact_refresh.setOnClickListener(v -> updateListFriends());

        updateListFriends();
        return view;
    }

    private void updateListFriends() {
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");

                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        Type listType = new TypeToken<List<FriendDTO>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);

                        adapter.setList(list);
//                        this.adapter = new FriendListAdapter(list, getActivity().getApplicationContext());
//                        this.rcv_contact_list.setAdapter(adapter);
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
        requestQueue.add(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contact_fragment, menu);
        MenuItem menuItemSearch = menu.findItem(R.id.search_friend_contact_fragment);
        MenuItem menuItemAdd = menu.findItem(R.id.add_friend_contact_fragment);

        View actionView = menuItemSearch.getActionView();

        SearchView searchView = (SearchView) actionView;
        searchView.setQueryHint("Search friend...");
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

        menuItemAdd.setOnMenuItemClickListener(item -> {
            Toast.makeText(getActivity(), "add friend", Toast.LENGTH_SHORT).show();
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
        searchFriend = list.stream()
                .filter(x -> x.getFriend().getDisplayName().toLowerCase()
                        .contains(newText.toLowerCase()))
                .collect(Collectors.toList());
        Log.d("--", searchFriend.toString());
        adapter.setList(searchFriend);
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