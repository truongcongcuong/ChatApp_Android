package com.example.chatapp.ui.main.frag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.ui.HomePageActivity;
import com.example.chatapp.ui.signup.SignUpStep2Activity;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InforFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InforFragment extends Fragment {
    TextView txt_info_error ;
    Button btn_info_signout;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public InforFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InforFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InforFragment newInstance(String param1, String param2) {
        InforFragment fragment = new InforFragment();
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
        View view = inflater.inflate(R.layout.fragment_infor, container, false);
        txt_info_error = view.findViewById(R.id.txt_info_error);
        txt_info_error.setTextColor(getActivity().getResources().getColor(R.color.red));
        btn_info_signout = view.findViewById(R.id.btn_info_signout);
        btn_info_signout.setOnClickListener(v->{
                callSignout();
            SharedPreferences sharedPreferencesIsLogin = getActivity().getApplicationContext().getSharedPreferences("is-login",getActivity().getApplicationContext().MODE_PRIVATE);
            SharedPreferences.Editor editorIsLogin = sharedPreferencesIsLogin.edit();
            editorIsLogin.putBoolean("status-login",false).apply();
            Intent i = new Intent(getActivity().getApplicationContext(), HomePageActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("EXIT", true);
            startActivity(i);
            getActivity().finish();
        });



        return view;
    }

    private void callSignout() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_AUTH + "signout",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response,"iso8859-1"),"UTF-8");
                        txt_info_error.setText(res.toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if(error instanceof ServerError && error != null){
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            txt_info_error.setText(object.getString("message"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token",getActivity().getApplicationContext().MODE_PRIVATE);
                String rfToken = sharedPreferencesToken.getString("refresh-token",null);
                map.put("Cookie",rfToken);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(request);
    }
}