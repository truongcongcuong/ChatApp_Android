package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageFragment extends Fragment {
    private RecyclerView rcv_list_message;
    private ListMessageAdapter adapter;
    private List<InboxDto> list;
    private Gson gson;
    private UserSummaryDTO user;
    private String token;
    private int page = 0;
    private int size = 20;
    private SimpleDateFormat dateFormat;

    private SearchUserAdapter searchUserAdapter;
    private List<UserProfileDto> searchUserResult;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private MessageDto messageDto = null;

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
        enable manu trên action bar
         */
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        gson = new Gson();
        list = new ArrayList<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        boolean inboxIsExists = false;
        messageDto = newMessage;
        for (InboxDto inboxDto : list) {
            if (messageDto != null && inboxDto.getRoom().getId().equals(messageDto.getRoomId())) {
                inboxDto.setLastMessage(messageDto);
                /*
                nếu người gửi của message trùng với người dùng hiện tại thì không tăng số tin nhắn mới
                 */
                if (!user.getId().equals(messageDto.getSender().getId()))
                    inboxDto.setCountNewMessage(inboxDto.getCountNewMessage() + 1);
                inboxIsExists = true;
            }
        }
        /*
        tin nhắn mới đến nhưng mà trong list chưa có inbox của message này nên phải
        lấy inbox này từ server sau đó thêm vào list
         */
        if (!inboxIsExists) {
            StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "/ofRoomId/" + messageDto.getRoomId(),
                    response -> {
                        try {
                            String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                            InboxDto inboxDto = gson.fromJson(res, InboxDto.class);
                            list.add(inboxDto);
                            sortTimeLastMessage();
                            adapter.notifyDataSetChanged();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.i("", error.toString())) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Authorization", "Bearer " + token);
                    return map;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
            requestQueue.add(request);
        } else {
            sortTimeLastMessage();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        rcv_list_message = view.findViewById(R.id.rcv_list_message);
        rcv_list_message.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        updateListInbox();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateListInbox() {
        list = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_INBOX + "?page=" + page + "&size=" + size,
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");

                        Type listType = new TypeToken<List<InboxDto>>() {
                        }.getType();
                        list = gson.fromJson(array.toString(), listType);

                        sortTimeLastMessage();
                        this.adapter = new ListMessageAdapter(getActivity().getApplicationContext(), list);
                        this.rcv_list_message.setAdapter(adapter);

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
    private void sortTimeLastMessage() {
        if (list != null) {
            list.sort((x, y) -> {
                try {
                    Date d1 = dateFormat.parse(x.getLastMessage().getCreateAt());
                    Date d2 = dateFormat.parse(y.getLastMessage().getCreateAt());
                    if (d1 == null || d2 == null)
                        return 0;
                    return d2.compareTo(d1);
                } catch (ParseException | NullPointerException e) {
                    e.printStackTrace();
                    return 0;
                }
            });
        }
    }

    /**
     * menu search user
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_user_fragment_message, menu);
        MenuItem menuItem = menu.findItem(R.id.search_user_fragment_message);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search user...");
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.requestFocus();

        /*
        tìm icon close và edit text của search view
         */
        int closeIconId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        int editTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        View closeIcon = searchView.findViewById(closeIconId);
        EditText editText = searchView.findViewById(editTextId);

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
                    try {
                        searchUserResult.clear();
                        searchUserAdapter.setList(searchUserResult);
                    } catch (Exception e) {
                        searchUserAdapter = new SearchUserAdapter(getActivity().getApplicationContext(), null);
                    }
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
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
                if (rcv_list_message.getLayoutManager() == null)
                    rcv_list_message.setLayoutManager(layoutManager);
                rcv_list_message.setAdapter(searchUserAdapter);
                searchUserAdapter.setList(searchUserResult);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}