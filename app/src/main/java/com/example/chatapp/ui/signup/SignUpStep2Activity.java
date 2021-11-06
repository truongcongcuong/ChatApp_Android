package com.example.chatapp.ui.signup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatapp.R;
import com.example.chatapp.cons.SendingData;
import com.example.chatapp.dto.UserSignUpDTO;
import com.example.chatapp.ui.HomePageActivity;
import com.example.chatapp.ui.signup.frag.SignupEnterEmailFragment;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

public class SignUpStep2Activity extends AppCompatActivity implements SendingData {
    private int screen = 1;
    private UserSignUpDTO user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step2);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_signup2_activity);
        toolbar.setTitle(getString(R.string.create_account));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Fragment fragment = new SignupEnterEmailFragment();

        Bundle bundle = getIntent().getExtras();
        user = (UserSignUpDTO) bundle.getSerializable("user");
        System.out.println(user.toString());

        loadFragment(fragment);

    }

    private void loadFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        getIntent().putExtras(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flo_sign_up, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void sendString(String o) {
        String s = (String) o;
        if (s.equals("sign-up-success"))
            showDialogSignupSuccess(SignUpStep2Activity.this);

    }


    private void showDialogSignupSuccess(Context c) {
        AlertDialog alertDialog = new AlertDialog.Builder(c)
                .setTitle(R.string.success_text)
                .setMessage(R.string.sign_up_success)
                .setPositiveButton(R.string.continue_text, (dialog, which) -> {
                    Intent i = new Intent(SignUpStep2Activity.this, HomePageActivity.class);
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

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}