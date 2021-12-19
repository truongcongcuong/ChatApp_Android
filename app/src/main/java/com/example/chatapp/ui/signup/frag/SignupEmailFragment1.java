package com.example.chatapp.ui.signup.frag;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.UserSignUpDTO;
import com.example.chatapp.ui.signup.SignUpEmailActivity2;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupEmailFragment1 extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextInputLayout edt_sign_up_name;
    private TextInputLayout edt_sign_up_email;
    private TextInputLayout edt_sign_up_enter_password;
    private TextInputLayout edt_sign_up_re_enter_password;
    private ProgressDialog progress;
    private static final Gson GSON = new Gson();

    public SignupEmailFragment1() {
    }

    public static SignupEmailFragment1 newInstance(String param1, String param2) {
        SignupEmailFragment1 fragment = new SignupEmailFragment1();
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
        View view = inflater.inflate(R.layout.fragment_singup_email1, container, false);

        edt_sign_up_name = view.findViewById(R.id.edt_sign_up_name);
        edt_sign_up_email = view.findViewById(R.id.edt_sign_up_email);
        edt_sign_up_enter_password = view.findViewById(R.id.edt_sign_up_enter_password);
        edt_sign_up_re_enter_password = view.findViewById(R.id.edt_sign_up_re_enter_password);

        ImageButton ibt_sign_up_next_step = view.findViewById(R.id.ibt_sign_up_next_step);

        progress = new ProgressDialog(getActivity());
        progress.setMessage(getResources().getString(R.string.please_wait));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);

        ibt_sign_up_next_step.setOnClickListener(v -> {
            if (valid()) {
                UserSignUpDTO user = new UserSignUpDTO();
                user.setPassword(edt_sign_up_enter_password.getEditText().getText().toString().trim());
                user.setDisplayName(edt_sign_up_name.getEditText().getText().toString().trim());
                user.setEmail(edt_sign_up_email.getEditText().getText().toString().trim());

                validOnServer(user);
            }
        });

        return view;
    }

    private boolean valid() {
        if (edt_sign_up_name.getEditText().getText().toString().trim().isEmpty()) {
            edt_sign_up_name.setError(getString(R.string.check_name_empty));
            edt_sign_up_name.requestFocus();
            return false;
        }
        edt_sign_up_name.setError(null);

        if (edt_sign_up_email.getEditText().getText().toString().trim().isEmpty()) {
            edt_sign_up_email.setError(getString(R.string.check_email_empty));
            edt_sign_up_email.requestFocus();
            return false;
        }
        edt_sign_up_email.setError(null);

        if (edt_sign_up_enter_password.getEditText().getText().toString().trim().isEmpty()) {
            edt_sign_up_enter_password.setError(getString(R.string.check_password_empty));
            edt_sign_up_enter_password.requestFocus();
            return false;
        }
        if (!edt_sign_up_enter_password.getEditText().getText().toString().trim().matches("[\\w]{8,}")) {
            edt_sign_up_enter_password.setError(getString(R.string.change_password_detail_8_char));
            edt_sign_up_enter_password.requestFocus();
            return false;
        }
        edt_sign_up_enter_password.setError(null);

        if (!edt_sign_up_enter_password.getEditText().getText().toString().trim().equals(edt_sign_up_re_enter_password.getEditText().getText().toString())) {
            edt_sign_up_re_enter_password.setError(getString(R.string.check_password));
            edt_sign_up_re_enter_password.requestFocus();
            return false;
        }
        edt_sign_up_re_enter_password.setError(null);

        return true;
    }

    private void validOnServer(UserSignUpDTO user) {
        progress.show();
        JSONObject objectRequest = new JSONObject();
        try {
            objectRequest = new JSONObject(GSON.toJson(user));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                Constant.API_SIGNUP + "email/valid",
                objectRequest,
                response -> {
                    save(user);
                }, error -> {
            progress.cancel();
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject object = new JSONObject(res);
                    String fieldName = object.getString("field");
                    if ("email".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_email.setError(object.getString("message"));
                        edt_sign_up_email.requestFocus();
                    } else if ("displayName".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_name.setError(object.getString("message"));
                        edt_sign_up_name.requestFocus();
                    } else if ("password".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_enter_password.setError(object.getString("message"));
                        edt_sign_up_enter_password.requestFocus();
                    }
                } catch (Exception e) {
                    progress.cancel();
                    e.printStackTrace();
                }
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    private void save(UserSignUpDTO user) {
        JSONObject objectRequest = new JSONObject();
        try {
            objectRequest = new JSONObject(GSON.toJson(user));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                Constant.API_SIGNUP + "email",
                objectRequest,
                response -> {
                    progress.cancel();
                    UserSignUpDTO userSignUpDTO = GSON.fromJson(response.toString(), UserSignUpDTO.class);
                    System.out.println("userSignUpDTO = " + userSignUpDTO);
                    Intent intent = new Intent(getActivity(), SignUpEmailActivity2.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", userSignUpDTO);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }, error -> {
            progress.cancel();
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject object = new JSONObject(res);
                    String fieldName = object.getString("field");
                    if ("email".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_email.setError(object.getString("message"));
                        edt_sign_up_email.requestFocus();
                    } else if ("displayName".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_name.setError(object.getString("message"));
                        edt_sign_up_name.requestFocus();
                    } else if ("password".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_enter_password.setError(object.getString("message"));
                        edt_sign_up_enter_password.requestFocus();
                    }
                } catch (Exception e) {
                    progress.cancel();
                    e.printStackTrace();
                }
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> map = new HashMap<>();
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

}