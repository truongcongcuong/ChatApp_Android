package com.example.chatapp.ui.signup.frag;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.R;
import com.example.chatapp.cons.SendData;
import com.example.chatapp.dto.UserSignUpDTO;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupEnterEmailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupEnterEmailFragment extends Fragment {
EditText edt_sign_up_email, edt_sign_up_verify;
Button btn_sign_up_confirm_email, btn_sign_up_confirm_vetify;
TextView txt_sign_up_vetify ,txt_sign_up_check_vetification_code,txt_sign_up_check_email;
SendData sendData;
UserSignUpDTO user;


    @Override
    public void onAttach(@NonNull Context context) {
        sendData = (SendData) context;
        super.onAttach(context);
    }

    public SignupEnterEmailFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static SignupEnterEmailFragment newInstance(UserSignUpDTO dto) {
        SignupEnterEmailFragment fragment = new SignupEnterEmailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getActivity().getIntent().getExtras();
        user = (UserSignUpDTO) bundle.getSerializable("user");
        System.out.println("------------------------------ test frag : "+user);

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_signup_enter_email, container, false);
        edt_sign_up_email = view.findViewById(R.id.edt_sign_up_email);
        edt_sign_up_verify = view.findViewById(R.id.edt_sign_up_verify);
        btn_sign_up_confirm_email = view.findViewById(R.id.btn_sign_up_confirm_email);
        btn_sign_up_confirm_vetify = view.findViewById(R.id.btn_sign_up_confirm_vetify);
        txt_sign_up_vetify = view.findViewById(R.id.txt_sign_up_vetify);
        txt_sign_up_check_email = view.findViewById(R.id.txt_sign_up_check_email);
        txt_sign_up_check_vetification_code = view.findViewById(R.id.txt_sign_up_check_vetification_code);


        edt_sign_up_verify.setVisibility(View.INVISIBLE);
        btn_sign_up_confirm_vetify.setVisibility(View.INVISIBLE);
        txt_sign_up_vetify.setVisibility(View.INVISIBLE);

        btn_sign_up_confirm_email.setOnClickListener(v->{
            String email = edt_sign_up_email.getText().toString();
            if(!TextUtils.isEmpty(email))
               sendVetificationCode(email);
            else {
                txt_sign_up_check_email.setText(R.string.check_email_empty);
                txt_sign_up_check_email.setTextColor(getResources().getColor(R.color.error));
            }

        });

        btn_sign_up_confirm_vetify.setOnClickListener(v->{
            String code= edt_sign_up_verify.getText().toString();
            if(!TextUtils.isEmpty(code)){
                vetify(code);
            } else{
                txt_sign_up_check_vetification_code.setText(R.string.enter_confirmation);
                txt_sign_up_check_vetification_code.setTextColor(getResources().getColor(R.color.error));
            }
        });




        return view;
    }

    private void vetify(String code) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_SIGNUP + "verify",
                response -> {

                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response,"iso8859-1"),"UTF-8");
                        JSONObject object = new JSONObject(res);
                        txt_sign_up_check_vetification_code.setText(object.getString("message"));
                        txt_sign_up_check_vetification_code.setTextColor(getResources().getColor(R.color.susscess));
                        sendData.SendingData("sign-up-success");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if(error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            txt_sign_up_check_vetification_code.setText(object.getString("message"));
                            txt_sign_up_check_vetification_code.setTextColor(getResources().getColor(R.color.error));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("email",user.getEmail());
                map.put("verificationCode",code);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(request);
    }

    private void sendVetificationCode(String email) {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_SIGNUP + "send_vetification_code ",
                response -> {
                    edt_sign_up_verify.setVisibility(View.VISIBLE);
                    btn_sign_up_confirm_vetify.setVisibility(View.VISIBLE);
                    txt_sign_up_vetify.setVisibility(View.VISIBLE);
                    user.setEmail(email);
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response,"iso8859-1"),"UTF-8");
                        JSONObject object = new JSONObject(res);
                        txt_sign_up_check_email.setText(object.getString("message"));
                        txt_sign_up_check_email.setTextColor(getResources().getColor(R.color.susscess));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if(error instanceof ServerError && error != null){
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            txt_sign_up_check_email.setText(object.getString("message"));
                            txt_sign_up_check_email.setTextColor(getResources().getColor(R.color.error));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("id",user.getId());
                map.put("email",email);

                return map;

            }
        };
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        queue.add(request);


    }
}