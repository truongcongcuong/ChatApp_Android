package com.example.chatapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.entity.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TestNewFeatureActivity extends AppCompatActivity {
    TextView txt_test_feature;
    List<Contact> contacts = new ArrayList<>();
    private static final int MY_PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_new_feature);

        txt_test_feature = findViewById(R.id.txt_test_feature);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkPermission();
        }else {
            getContacts();
        }
    }
    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

//    private void showAllContact() {
//        ContentResolver contentResolver = getContentResolver();
//
//        Cursor cursor = contentResolver
//                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+"ASC");
//        if (cursor!=null){
//            HashSet<String> set = new HashSet<String>();
//            try {
//                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
//                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//
//                String name, number;
//                while(cursor.moveToNext()){
//                    name = cursor.getString(nameIndex);
//                    number = cursor.getString(numberIndex);
//                    Log.d("number : ", number);
//                    number = number.replace(" ","");
//                    if (!set.contains(number)){
//                        contacts.add(new Contact(name,number));
//                        set.add(number);
//                    }
//                }
//
//            }finally {
//                cursor.close();
//            }
//        }
//    }
protected void getContacts(){
        /*
            Cursor
                This interface provides random read-write access to the result
                set returned by a database query.
        */
        /*
            ContactsContract
                The contract between the contacts provider and applications. Contains definitions
                for the supported URIs and columns. These APIs supersede ContactsContract.Contacts.
        */
        /*
            ContactsContract.CommonDataKinds
                Container for definitions of common data types stored in the ContactsContract.Data table.
        */
        /*
            ContactsContract.CommonDataKinds.Phone
                A data kind representing a telephone number.
        */
        /*
            CONTENT_URI
                The content:// style URI for all data records of the CONTENT_ITEM_TYPE MIME type,
                combined with the associated raw contact and aggregate contact data.
        */
    Cursor contacts = getContentResolver().query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
    );
    // Empty text view
    txt_test_feature.setText("");

    // Loop through the contacts
    while (contacts.moveToNext())
    {
        // Get the current contact name
        String name = contacts.getString(
                contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));

        // Get the current contact phone number
        String phoneNumber = contacts.getString(
                contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

        // Display the contact to text view
        Log.d("number ", phoneNumber);
        txt_test_feature.append(name);
        txt_test_feature.append("\n" + phoneNumber + "\n\n");
    }
    contacts.close();
}

    protected void checkPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){
                    // show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(TestNewFeatureActivity.this);
                    builder.setMessage("Read Contacts permission is required.");
                    builder.setTitle("Please grant permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    TestNewFeatureActivity.this,
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
                            TestNewFeatureActivity.this,
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