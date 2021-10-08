package com.example.chatapp.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatapp.R;
import com.example.chatapp.adapter.SyncContactAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.GetNewAccessToken;
import com.example.chatapp.dto.PhoneBookFriendDTO;
import com.example.chatapp.entity.Contact;
import com.example.chatapp.entity.FriendRequest;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncContactActivity extends AppCompatActivity {
    ImageButton ibt_sync_contact_back,ibt_sync_contact_setting;
    RecyclerView rcv_sync_contact;
    List<Contact> listContact = new ArrayList<>();
    private static final int MY_PERMISSION_REQUEST_CODE = 123;
    Gson gson = new Gson();
    String token;
    List<PhoneBookFriendDTO> listPhoneBookFriend = new ArrayList<>();
    SyncContactAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_contact);

        ibt_sync_contact_back = findViewById(R.id.ibt_sync_contact_back);
        ibt_sync_contact_setting = findViewById(R.id.ibt_sync_contact_setting);
        rcv_sync_contact = findViewById(R.id.rcv_sync_contact);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkPermission();
        }else {
            getContacts();
        }

        ibt_sync_contact_back.setOnClickListener(v->finish());

        GetNewAccessToken getNewAccessToken = new GetNewAccessToken(this);
        getNewAccessToken.sendGetNewTokenRequest();

        SharedPreferences sharedPreferencesToken = getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

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
            Log.e("json object contact",jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, Constant.API_FRIEND_LIST+"/syncContact",
                jsonObject,
                response -> {
                    try {
                        Log.e("resp",response.toString());
                        JSONArray array = new JSONArray(response.toString());
                        Log.e("res : ",array.toString());
                        Type listType = new TypeToken<List<PhoneBookFriendDTO>>() {
                        }.getType();
                        listPhoneBookFriend = gson.fromJson(array.toString(), listType);
                        setDataToRCV();

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
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void setDataToRCV() {
        adapter = new SyncContactAdapter(listPhoneBookFriend,this,token);
        rcv_sync_contact.setAdapter(adapter);
        rcv_sync_contact.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void getContacts(){

        Cursor contacts = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        while (contacts.moveToNext())
        {
            // Get the current contact name
            String name = contacts.getString(
                    contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));

            // Get the current contact phone number
            String phoneNumber = contacts.getString(
                    contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Log.d("number ", phoneNumber);
            listContact.add(new Contact(name,phoneNumber));
        }
        contacts.close();
    }

    protected void checkPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){
                    // show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SyncContactActivity.this);
                    builder.setMessage("Read Contacts permission is required.");
                    builder.setTitle("Please grant permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    SyncContactActivity.this,
                                    new String[]{Manifest.permission.READ_CONTACTS},
                                    MY_PERMISSION_REQUEST_CODE
                            );
                        }
                    });
                    builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            SyncContactActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSION_REQUEST_CODE
                    );
                }
            }else {
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
}