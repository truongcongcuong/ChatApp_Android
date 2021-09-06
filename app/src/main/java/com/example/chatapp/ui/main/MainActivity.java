package com.example.chatapp.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.chatapp.R;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.ui.HomePageActivity;
import com.example.chatapp.ui.main.frag.ContactFragment;
import com.example.chatapp.ui.main.frag.InforFragment;
import com.example.chatapp.ui.main.frag.MessageFragment;
import com.example.chatapp.ui.main.frag.RecentFragment;
import com.example.chatapp.ui.main.frag.GroupFragment;
import com.example.chatapp.ui.signin.SigninActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bnv_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        bnv_menu = findViewById(R.id.bnv_bot);
        bnv_menu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(new MessageFragment());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()){
                case R.id.navigation_message :
                    fragment = new MessageFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_contact :
                    fragment = new ContactFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_group :
                    fragment = new GroupFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_recent :
                    fragment = new RecentFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_info:
                    fragment = new InforFragment();
                    loadFragment(fragment);
                    return true;

            }
            return false;
        }
    };


    private void loadFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fla_content,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}