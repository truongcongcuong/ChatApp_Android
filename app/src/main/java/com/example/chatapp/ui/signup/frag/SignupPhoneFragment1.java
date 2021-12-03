package com.example.chatapp.ui.signup.frag;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.example.chatapp.ui.signup.SignupPhoneActivity2;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignupPhoneFragment1 extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextInputLayout edt_sign_up_name;
    private TextInputLayout edt_sign_up_phone_number;
    private TextInputLayout edt_sign_up_enter_password;
    private TextInputLayout edt_sign_up_re_enter_password;

    private FirebaseAuth mAuth;
    private UserSignUpDTO user;
    private String phone;
    private String mVerificationId;
    private ProgressDialog progress;
    private CountryCodePicker country_code_picker;
    public static final Gson GSON = new Gson();

    public SignupPhoneFragment1() {
    }

    public static SignupPhoneFragment1 newInstance(String param1, String param2) {
        SignupPhoneFragment1 fragment = new SignupPhoneFragment1();
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
        View view = inflater.inflate(R.layout.fragment_signup_phone1, container, false);

        edt_sign_up_name = view.findViewById(R.id.edt_sign_up_name);
        edt_sign_up_phone_number = view.findViewById(R.id.edt_sign_up_phone_number);
        edt_sign_up_enter_password = view.findViewById(R.id.edt_sign_up_enter_password);
        edt_sign_up_re_enter_password = view.findViewById(R.id.edt_sign_up_re_enter_password);
        country_code_picker = view.findViewById(R.id.country_code_picker);

        mAuth = FirebaseAuth.getInstance();
        ImageButton ibt_sign_up_next_step = view.findViewById(R.id.ibt_sign_up_next_step);

        progress = new ProgressDialog(getActivity());
        progress.setMessage(getResources().getString(R.string.please_wait));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);

        ibt_sign_up_next_step.setOnClickListener(v -> {
            if (valid()) {
                user = new UserSignUpDTO();
                phone = country_code_picker.getSelectedCountryCodeWithPlus() + edt_sign_up_phone_number.getEditText().getText().toString().trim();

                user.setPassword(edt_sign_up_enter_password.getEditText().getText().toString().trim());
                user.setDisplayName(edt_sign_up_name.getEditText().getText().toString().trim());
                user.setPhoneNumber(phone);

                sendSignUpUserToServer(user);
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

        phone = edt_sign_up_phone_number.getEditText().getText().toString().trim();
        if (phone.isEmpty()) {
            edt_sign_up_phone_number.setError(getString(R.string.check_phone_empty));
            edt_sign_up_phone_number.requestFocus();
            return false;
        }
        edt_sign_up_phone_number.setError(null);

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

        if (!edt_sign_up_enter_password.getEditText().getText().toString().trim()
                .equals(edt_sign_up_re_enter_password.getEditText().getText().toString())) {
            edt_sign_up_re_enter_password.setError(getString(R.string.check_password));
            edt_sign_up_re_enter_password.requestFocus();
            return false;
        }
        edt_sign_up_re_enter_password.setError(null);

        return true;
    }

    private void sendSignUpUserToServer(UserSignUpDTO user) {
        JSONObject objectRequest = new JSONObject();
        try {
            objectRequest = new JSONObject(GSON.toJson(user));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                Constant.API_SIGNUP + "valid",
                objectRequest,
                response -> {
                    System.out.println("response = " + response);
                    progress.show();
                    authen(phone);
                }, error -> {
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject object = new JSONObject(res);
                    String fieldName = object.getString("field");
                    if ("phoneNumber".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_phone_number.setError(object.getString("message"));
                        edt_sign_up_phone_number.requestFocus();
                    } else if ("displayName".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_name.setError(object.getString("message"));
                        edt_sign_up_name.requestFocus();
                    } else if ("password".equalsIgnoreCase(fieldName)) {
                        edt_sign_up_enter_password.setError(object.getString("message"));
                        edt_sign_up_enter_password.requestFocus();
                    }
                } catch (Exception e) {
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

    private void authen(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(getActivity())
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                showDialogFail();
                                Log.e("---", e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                mVerificationId = verificationId;
                                signupStep2(phone, mVerificationId);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        signupStep2(phone, mVerificationId);
                    } else {
                        showDialogFail();
                    }
                });
    }

    private void signupStep2(String phone, String verificationId) {
        progress.cancel();
        Intent intent = new Intent(getActivity(), SignupPhoneActivity2.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        bundle.putString("phone", phone);
        bundle.putString("verificationId", verificationId);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void showDialogFail() {
        progress.cancel();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(getString(R.string.authen_fail_try_again))
                .setPositiveButton(getString(R.string.confirm_button), (dialog, id) -> dialog.cancel());
        dialogBuilder.create().show();
    }

}