package com.example.chatapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.SyncContactAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.PhoneBookFriendDTO;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.entity.Contact;
import com.example.chatapp.entity.FriendRequest;
import com.example.chatapp.enumvalue.FriendStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncContactActivity extends AppCompatActivity {
    private RecyclerView rcv_sync_contact;
    private List<Contact> listContact;
    private static final int MY_PERMISSION_REQUEST_CODE = 123;
    private Gson gson;
    private String token;
    private List<PhoneBookFriendDTO> listPhoneBookFriend;
    private SyncContactAdapter adapter;

    private final BroadcastReceiver friendRequestReceived = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (dto != null) {
                    UserProfileDto from = dto.getFrom();
                    if (from != null) {
                        for (PhoneBookFriendDTO phoneBook : listPhoneBookFriend) {
                            UserProfileDto user = phoneBook.getUser();
                            if (user != null && user.getId().equals(from.getId())) {
                                user.setFriendStatus(FriendStatus.RECEIVED);
                                phoneBook.setUser(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private final BroadcastReceiver friendRequestAccept = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (dto != null) {
                    UserProfileDto to = dto.getTo();
                    if (to != null) {
                        for (PhoneBookFriendDTO phoneBook : listPhoneBookFriend) {
                            UserProfileDto user = phoneBook.getUser();
                            if (user.getId().equals(to.getId())) {
                                user.setFriendStatus(FriendStatus.FRIEND);
                                phoneBook.setUser(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private final BroadcastReceiver friendRequestRecall = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (dto != null) {
                    UserProfileDto from = dto.getFrom();
                    if (from != null) {
                        for (PhoneBookFriendDTO phoneBook : listPhoneBookFriend) {
                            UserProfileDto user = phoneBook.getUser();
                            if (user.getId().equals(from.getId())) {
                                user.setFriendStatus(FriendStatus.NONE);
                                phoneBook.setUser(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private final BroadcastReceiver friendRequestDelete = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                FriendRequest dto = (FriendRequest) bundle.getSerializable("dto");
                if (dto != null) {
                    UserProfileDto to = dto.getTo();
                    if (to != null) {
                        for (PhoneBookFriendDTO phoneBook : listPhoneBookFriend) {
                            UserProfileDto user = phoneBook.getUser();
                            if (user.getId().equals(to.getId())) {
                                user.setFriendStatus(FriendStatus.NONE);
                                phoneBook.setUser(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_contact);

        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestReceived, new IntentFilter("friendRequest/received"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestAccept, new IntentFilter("friendRequest/accept"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestRecall, new IntentFilter("friendRequest/recall"));
        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestDelete, new IntentFilter("friendRequest/delete"));

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Toolbar toolbar = findViewById(R.id.toolbar_sync_contact);
        toolbar.setTitle(R.string.phone_book_friend);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        rcv_sync_contact = findViewById(R.id.rcv_sync_contact);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcv_sync_contact.getContext(), DividerItemDecoration.VERTICAL);
        rcv_sync_contact.addItemDecoration(dividerItemDecoration);

        gson = new Gson();
        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        listContact = new ArrayList<>();
        listPhoneBookFriend = new ArrayList<>();
        adapter = new SyncContactAdapter(listPhoneBookFriend, this, token);
        rcv_sync_contact.setAdapter(adapter);
        rcv_sync_contact.setLayoutManager(new LinearLayoutManager(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            getContacts();
        }

        /*
        hiện nút mũi tên quay lại trên toolbar
        */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getContactUser();
    }

    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private void getContactUser() {
        JSONArray jsonObject = null;
        try {
            jsonObject = new JSONArray(gson.toJson(listContact));
            Log.e("json object contact", jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, Constant.API_FRIEND_LIST + "/syncContact",
                jsonObject,
                response -> {
                    try {
                        Log.e("resp", response.toString());
                        JSONArray array = new JSONArray(response.toString());
                        Log.e("res : ", array.toString());
                        Type listType = new TypeToken<List<PhoneBookFriendDTO>>() {
                        }.getType();
                        listPhoneBookFriend = gson.fromJson(array.toString(), listType);
                        adapter.setList(listPhoneBookFriend);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && error != null) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            Log.e("eror : ", res);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    /*private void setDataToRCV() {
        SyncContactAdapter adapter = new SyncContactAdapter(listPhoneBookFriend, this, token);
        rcv_sync_contact.setAdapter(adapter);
        rcv_sync_contact.setLayoutManager(new LinearLayoutManager(this));
    }*/

    protected void getContacts() {

        Cursor contacts = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        while (contacts.moveToNext()) {
            // Get the current contact name
            String name = contacts.getString(
                    contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));

            // Get the current contact phone number
            String phoneNumber = contacts.getString(
                    contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Log.d("number ", phoneNumber);
            listContact.add(new Contact(name, phoneNumber));
        }
        contacts.close();
    }

    protected void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    // show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SyncContactActivity.this);
                    builder.setMessage(getString(R.string.permission_read_contact_is_required));
                    builder.setTitle(getString(R.string.permission_needed_title));
                    builder.setPositiveButton(getString(R.string.confirm_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    SyncContactActivity.this,
                                    new String[]{Manifest.permission.READ_CONTACTS},
                                    MY_PERMISSION_REQUEST_CODE
                            );
                        }
                    });
                    builder.setNeutralButton(getString(R.string.cancel_button), null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            SyncContactActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSION_REQUEST_CODE
                    );
                }
            } else {
                // Permission already granted
                getContacts();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getContacts();
                } else {
                    // Permission denied
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync_contact_activity, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_setting_sync_contact_activity);
        menuItem.setOnMenuItemClickListener(item -> {
            Log.d("", "setting sync contact activity");
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestReceived);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestAccept);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestRecall);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(friendRequestDelete);
        super.onDestroy();
    }

}