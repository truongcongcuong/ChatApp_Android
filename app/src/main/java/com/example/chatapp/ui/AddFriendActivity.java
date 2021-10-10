package com.example.chatapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.example.chatapp.R;

public class AddFriendActivity extends AppCompatActivity {
    ImageButton ibt_add_friend_back;
    EditText edt_add_friend_enter_phone_number;
    Button btn_add_friend_search;
    RelativeLayout rll_add_friend_manage_friend_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        ibt_add_friend_back = findViewById(R.id.ibt_add_friend_back);
        edt_add_friend_enter_phone_number = findViewById(R.id.edt_add_friend_enter_phone_number);
        btn_add_friend_search = findViewById(R.id.btn_add_friend_search);
        rll_add_friend_manage_friend_request = findViewById(R.id.rll_add_friend_manage_friend_request);

        ibt_add_friend_back.setOnClickListener(v->finish());

        btn_add_friend_search.setOnClickListener(v->searchUserWithPhoneNumber());
        rll_add_friend_manage_friend_request.setOnClickListener(v->{
            Intent intent = new Intent(this,ManageFriendRequestActivity.class);
            startActivity(intent);
        });
    }

    private void searchUserWithPhoneNumber() {
    }
}