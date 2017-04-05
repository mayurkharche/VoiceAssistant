package com.mrcoders.www.voiceassistant.functionality;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohit on 5/4/17.
 */

public class CallToContact extends AppCompatActivity{

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private String[] mColumnProjection = new String[]{
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };

    private String mSelectionClause = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + "  = ?";
    private String[] mSelectionArguments = new String[]{"Rohit"};

    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            String contact = getContactPhone();
            Toast.makeText(this,contact,Toast.LENGTH_LONG).show();

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+contact));

            if (ActivityCompat.checkSelfPermission(CallToContact.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getContactPhone() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        List<String> contacts_list = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(uri, null,mSelectionClause,mSelectionArguments, null);

        if (cursor.moveToFirst()) {
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (phone == null) {
                cursor.close();
                return null;
            }
            else
                return phone;
        }
        cursor.close();
        return null;
    }
}
