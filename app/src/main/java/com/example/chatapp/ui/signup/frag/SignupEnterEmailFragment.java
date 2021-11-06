package com.example.chatapp.ui.signup.frag;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.SendingData;
import com.example.chatapp.dto.UserSignUpDTO;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class SignupEnterEmailFragment extends Fragment {
    private EditText edt_sign_up_email;
    private EditText edt_sign_up_verify;
    private Button btn_sign_up_confirm_verify;
    private TextView txt_sign_up_verify;
    private TextView txt_sign_up_check_verification_code;
    private TextView txt_sign_up_check_email;
    private SendingData sendingData;
    private UserSignUpDTO user;

    @Override
    public void onAttach(@NonNull Context context) {
        sendingData = (SendingData) context;
        super.onAttach(context);
    }

    public SignupEnterEmailFragment() {
        // Required empty public constructor
    }

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
        System.out.println("------------------------------ test frag : " + user);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup_enter_email, container, false);
        edt_sign_up_email = view.findViewById(R.id.edt_sign_up_email);
        edt_sign_up_verify = view.findViewById(R.id.edt_sign_up_verify);
        Button btn_sign_up_confirm_email = view.findViewById(R.id.btn_sign_up_confirm_email);
        btn_sign_up_confirm_verify = view.findViewById(R.id.btn_sign_up_confirm_vetify);
        txt_sign_up_verify = view.findViewById(R.id.txt_sign_up_vetify);
        txt_sign_up_check_email = view.findViewById(R.id.txt_sign_up_check_email);
        txt_sign_up_check_verification_code = view.findViewById(R.id.txt_sign_up_check_vetification_code);


        edt_sign_up_verify.setVisibility(View.INVISIBLE);
        btn_sign_up_confirm_verify.setVisibility(View.INVISIBLE);
        txt_sign_up_verify.setVisibility(View.INVISIBLE);

        btn_sign_up_confirm_email.setOnClickListener(v -> {
            String email = edt_sign_up_email.getText().toString();
            if (!TextUtils.isEmpty(email))
                sendVerificationCode(email);
            else {
                txt_sign_up_check_email.setText(R.string.check_email_empty);
                txt_sign_up_check_email.setTextColor(getResources().getColor(R.color.error));
            }

        });

        btn_sign_up_confirm_verify.setOnClickListener(v -> {
            String code = edt_sign_up_verify.getText().toString();
            if (!TextUtils.isEmpty(code)) {
                verify(code);
            } else {
                txt_sign_up_check_verification_code.setText(R.string.enter_confirmation);
                txt_sign_up_check_verification_code.setTextColor(getResources().getColor(R.color.error));
            }
        });

        return view;
    }

    private void verify(String code) {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_SIGNUP + "verify",
                response -> {

                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        txt_sign_up_check_verification_code.setText(object.getString("message"));
                        txt_sign_up_check_verification_code.setTextColor(getResources().getColor(R.color.susscess));
                        sendingData.sendString("sign-up-success");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            txt_sign_up_check_verification_code.setText(object.getString("message"));
                            txt_sign_up_check_verification_code.setTextColor(getResources().getColor(R.color.error));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", user.getEmail());
                map.put("verificationCode", code);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    private void sendVerificationCode(String email) {
        StringRequest request = new StringRequest(Request.Method.PUT, Constant.API_SIGNUP + "send_vetification_code ",
                response -> {
                    edt_sign_up_verify.setVisibility(View.VISIBLE);
                    btn_sign_up_confirm_verify.setVisibility(View.VISIBLE);
                    txt_sign_up_verify.setVisibility(View.VISIBLE);
                    user.setEmail(email);
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        JSONObject object = new JSONObject(res);
                        txt_sign_up_check_email.setText(object.getString("message"));
                        txt_sign_up_check_email.setTextColor(getResources().getColor(R.color.susscess));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject object = new JSONObject(res);
                            txt_sign_up_check_email.setText(object.getString("message"));
                            txt_sign_up_check_email.setTextColor(getResources().getColor(R.color.error));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("email", email);

                return map;

            }
        };
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);

    }
}