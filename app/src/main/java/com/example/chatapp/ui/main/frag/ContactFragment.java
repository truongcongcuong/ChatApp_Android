package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.FriendListAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.FriendDTO;
import com.example.chatapp.dto.InboxDto;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactFragment extends Fragment {

    RecyclerView rcv_contact_list;
    FriendListAdapter adapter;
    SharedPreferences sharedPreferencesToken;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(getActivity().getApplicationContext());
        getNewAccessToken.sendGetNewTokenRequest();
        sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);

        rcv_contact_list = view.findViewById(R.id.rcv_contact_list);
        updateListFriends();
        rcv_contact_list.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        return view;
    }

    private void updateListFriends() {
        List<FriendDTO> list = new ArrayList<>();
        Gson gson = new Gson();
        String token = sharedPreferencesToken.getString("access-token",null);
        StringRequest request = new StringRequest(Request.Method.GET, Constant.API_FRIEND_LIST,
                response -> {
                    try {
                        Log.e("json : ", response.toString());
                        String res = URLDecoder.decode(URLEncoder.encode(response,"iso8859-1"),"UTF-8");
                        Log.e("json utf-8", res);
                        JSONObject object = new JSONObject(res);
                        JSONArray array = (JSONArray) object.get("content");
                        for(int i =0;i<array.length();i++){
                            JSONObject objectInbox = new JSONObject(String.valueOf(array.getJSONObject(i)));
                            FriendDTO dto = gson.fromJson(objectInbox.toString(),FriendDTO.class);
                            list.add(dto);
                            Log.e("fr",dto.toString());
                        }
                        this.adapter = new FriendListAdapter(list,getActivity().getApplicationContext());
                        this.rcv_contact_list.setAdapter(adapter);
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.i("error",error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        requestQueue.add(request);
    }
}