package com.example.chatapp.ui.signup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.example.chatapp.ui.HomePageActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignupPhoneActivity2 extends AppCompatActivity {

    private UserSignUpDTO user;
    private TextInputLayout signup_phone2_txt1;
    private TextInputLayout signup_phone2_txt2;
    private TextInputLayout signup_phone2_txt3;
    private TextInputLayout signup_phone2_txt4;
    private TextInputLayout signup_phone2_txt5;
    private TextInputLayout signup_phone2_txt6;

    private TextView txt_dont_receiver_otp;
    private TextView txt_signup_phone2_message;

    private String phone;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mToken;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_phone2);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_activity_signup_phone2);
        toolbar.setTitle(getString(R.string.create_account22));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.please_wait));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            phone = bundle.getString("phone");
            mVerificationId = bundle.getString("verificationId");
            user = (UserSignUpDTO) bundle.getSerializable("user");

            System.out.println("user = " + user);
        }
        System.out.println(user.toString());

        mAuth = FirebaseAuth.getInstance();

        signup_phone2_txt1 = findViewById(R.id.signup_phone2_txt1);
        signup_phone2_txt2 = findViewById(R.id.signup_phone2_txt2);
        signup_phone2_txt3 = findViewById(R.id.signup_phone2_txt3);
        signup_phone2_txt4 = findViewById(R.id.signup_phone2_txt4);
        signup_phone2_txt5 = findViewById(R.id.signup_phone2_txt5);
        signup_phone2_txt6 = findViewById(R.id.signup_phone2_txt6);

        txt_dont_receiver_otp = findViewById(R.id.txt_dont_receiver_otp);
        txt_signup_phone2_message = findViewById(R.id.txt_signup_phone2_message);
        Button sign_up_phone2_btn_send = findViewById(R.id.sign_up_phone2_btn_send);

        signup_phone2_txt1.getEditText().addTextChangedListener(new GenericTextWatcher(signup_phone2_txt1.getEditText(), signup_phone2_txt2.getEditText()));
        signup_phone2_txt2.getEditText().addTextChangedListener(new GenericTextWatcher(signup_phone2_txt2.getEditText(), signup_phone2_txt3.getEditText()));
        signup_phone2_txt3.getEditText().addTextChangedListener(new GenericTextWatcher(signup_phone2_txt3.getEditText(), signup_phone2_txt4.getEditText()));
        signup_phone2_txt4.getEditText().addTextChangedListener(new GenericTextWatcher(signup_phone2_txt4.getEditText(), signup_phone2_txt5.getEditText()));
        signup_phone2_txt5.getEditText().addTextChangedListener(new GenericTextWatcher(signup_phone2_txt5.getEditText(), signup_phone2_txt6.getEditText()));
        signup_phone2_txt6.getEditText().addTextChangedListener(new GenericTextWatcher(signup_phone2_txt6.getEditText(), null));

        signup_phone2_txt1.getEditText().setOnKeyListener(new GenericKeyEvent(signup_phone2_txt1.getEditText(), null));
        signup_phone2_txt2.getEditText().setOnKeyListener(new GenericKeyEvent(signup_phone2_txt2.getEditText(), signup_phone2_txt1.getEditText()));
        signup_phone2_txt3.getEditText().setOnKeyListener(new GenericKeyEvent(signup_phone2_txt3.getEditText(), signup_phone2_txt2.getEditText()));
        signup_phone2_txt4.getEditText().setOnKeyListener(new GenericKeyEvent(signup_phone2_txt4.getEditText(), signup_phone2_txt3.getEditText()));
        signup_phone2_txt5.getEditText().setOnKeyListener(new GenericKeyEvent(signup_phone2_txt5.getEditText(), signup_phone2_txt4.getEditText()));
        signup_phone2_txt6.getEditText().setOnKeyListener(new GenericKeyEvent(signup_phone2_txt6.getEditText(), signup_phone2_txt5.getEditText()));

        sign_up_phone2_btn_send.setOnClickListener(v -> {
            progress.show();
            String otp1 = signup_phone2_txt1.getEditText().getText().toString().trim();
            String otp2 = signup_phone2_txt2.getEditText().getText().toString().trim();
            String otp3 = signup_phone2_txt3.getEditText().getText().toString().trim();
            String otp4 = signup_phone2_txt4.getEditText().getText().toString().trim();
            String otp5 = signup_phone2_txt5.getEditText().getText().toString().trim();
            String otp6 = signup_phone2_txt6.getEditText().getText().toString().trim();

            StringBuilder otp = new StringBuilder();
            otp.append(otp1);
            otp.append(otp2);
            otp.append(otp3);
            otp.append(otp4);
            otp.append(otp5);
            otp.append(otp6);

            System.out.println("otp = " + otp);

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp.toString());
            signInWithPhoneAuthCredential(credential);
        });

        txt_dont_receiver_otp.setOnClickListener(v -> {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setForceResendingToken(mToken)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                @Override
                                public void onVerificationCompleted(PhoneAuthCredential credential) {
                                    signInWithPhoneAuthCredential(credential);
                                }

                                @Override
                                public void onVerificationFailed(FirebaseException e) {
                                    System.out.println("e.getMessage() = " + e.getMessage());
                                    showDialogFail();
                                }

                                @Override
                                public void onCodeSent(@NonNull String verificationId,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                    mVerificationId = verificationId;
                                    mToken = token;
                                }
                            })
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUser(user);
                    } else {
                        showDialogFail();
                    }
                });
    }

    private void showDialogFail() {
        AlertDialog.Builder blockBuilder = new AlertDialog.Builder(this);
        blockBuilder.setMessage(getString(R.string.authen_fail_try_again))
                .setPositiveButton(getString(R.string.confirm_button), (dialog, id) -> dialog.cancel());
        blockBuilder.create().show();
    }

    private void saveUser(UserSignUpDTO user) {
        JSONObject objectRequest = new JSONObject();
        try {
            objectRequest.put("displayName", user.getDisplayName());
            objectRequest.put("password", user.getPassword());
            objectRequest.put("phoneNumber", user.getPhoneNumber());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                Constant.API_SIGNUP + "phone",
                objectRequest,
                response -> {
                    progress.cancel();
                    showDialogSignupSuccess(SignupPhoneActivity2.this);
                }, error -> {
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError) {
                try {
                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject object = new JSONObject(res);
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

        RequestQueue queue = Volley.newRequestQueue(this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    private void showDialogSignupSuccess(Context c) {
        AlertDialog alertDialog = new AlertDialog.Builder(c)
                .setTitle(R.string.success_text)
                .setMessage(R.string.sign_up_success)
                .setPositiveButton(R.string.continue_text, (dialog, which) -> {
                    Intent i = new Intent(SignupPhoneActivity2.this, HomePageActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("EXIT", true);
                    startActivity(i);
                    finish();

                })
                .create();
        alertDialog.show();
    }

    static class GenericKeyEvent implements View.OnKeyListener {
        private EditText currentView;
        private EditText previousView;

        GenericKeyEvent(EditText currentView, EditText previousView) {
            this.currentView = currentView;
            this.previousView = previousView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DEL && currentView.getId() != R.id.signup_email2_txt1
                    && currentView.getText().toString().isEmpty()) {
                if (previousView != null) {
                    previousView.setText(null);
                    previousView.requestFocus();
                }
                return true;
            }
            return false;
        }
    }

    static class GenericTextWatcher implements TextWatcher {
        private View currentView;
        private View nextView;

        public GenericTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            System.out.println("s.toString() = " + s.toString());
            if (text.length() == 1 && nextView != null)
                nextView.requestFocus();
        }
    }

}