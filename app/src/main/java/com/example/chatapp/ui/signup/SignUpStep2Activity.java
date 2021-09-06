package com.example.chatapp.ui.signup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.R;
import com.example.chatapp.cons.SendData;
import com.example.chatapp.dto.UserSignUpDTO;
import com.example.chatapp.ui.HomePageActivity;
import com.example.chatapp.ui.signup.frag.SignupEnterEmailFragment;

import java.util.HashMap;
import java.util.Map;

public class SignUpStep2Activity extends AppCompatActivity implements SendData {
    ImageButton ibt_sign_up_back_step2,ibt_sign_up_next_step2;
    int screen =1;
    UserSignUpDTO user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up_step2);
        Fragment fragment = new SignupEnterEmailFragment();

        ibt_sign_up_next_step2 = findViewById(R.id.ibt_sign_up_next_step2);
        ibt_sign_up_back_step2 = findViewById(R.id.ibt_sign_up_back_step2);

        Bundle bundle = getIntent().getExtras();
        user = (UserSignUpDTO) bundle.getSerializable("user");
        System.out.println(user.toString());

        loadFragment(fragment);

        ibt_sign_up_back_step2.setOnClickListener(v->{
                finish();
        });
    }

    private void loadFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",user);
        getIntent().putExtras(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flo_sign_up, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void SendingData(String o) {
       String s = (String) o;
        if(s.equals("sign-up-success"))
            showDialogSignupSuccess(SignUpStep2Activity.this);

    }


    private void showDialogSignupSuccess(Context c){
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


}