package com.example.siddimore.contactdbtojson;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.example.siddimore.contactdbtojson.writetofile.*;


public class MainActivity extends Activity {

    String[] permissions = {
            "android.permission.READ_CONTACTS"
    };

    final int MY_PERMISSIONS_REQUEST_READ_CONTACT = 1;
    public TextView outputText;
    JSONArray resultSet;
    JSONObject returnObj;
    List<String> phoneNumbers;
    List<String> emailAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = (TextView)findViewById(R.id.JsonText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACT);

                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                //return TODO;
            }
            else {

                // Good to Fetch Contacts Since Permission is Granted
                fetchContacts();
            }
        } else {
            fetchContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Toast.makeText(MainActivity.this,"ThankYou for Granting Access",Toast.LENGTH_LONG).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this,"No CalendarAccess",Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    // Method fetches each contact and converts to JSON
    private void fetchContacts() {

        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;
        String ORGANIZATION = ContactsContract.CommonDataKinds.Organization.DATA;

        StringBuffer output = new StringBuffer();

        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        resultSet = new JSONArray();


        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            // Iterate over each row of the dB
            while (cursor.moveToNext()) {

                returnObj = new JSONObject();
                try {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                    String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                returnObj.put("givenName", name);

                // Get Phone number cursor and add to output file
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                    if (hasPhoneNumber > 0) {
                        //System.out.println("FirstName: " + name);
                        output.append("\n First Name:" + name);

                        // Query and loop for every phone number of the contact
                        Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                        phoneNumbers = new ArrayList<>();
                        while (phoneCursor.moveToNext()) {

                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            output.append("\n Phone number:" + phoneNumber);
                            phoneNumbers.add(phoneNumber);
                            //System.out.println("phoneNumber: " + phoneNumber);
                        }

                        phoneCursor.close();
                        returnObj.put("phoneNumbers", phoneNumbers);
                        // Query and loop for every email of the contact
                        Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);
                        emailAddresses = new ArrayList<>();
                        while (emailCursor.moveToNext()) {

                            email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                            output.append("\nEmail:" + email);
                            emailAddresses.add(email);
                            //System.out.println("emailAddress: " + email);
                        }

                        emailCursor.close();
                        returnObj.put("emailAddresses:", emailAddresses);
                    }


                    Cursor orgCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{contact_id,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}, null);

                    if (orgCur.moveToFirst()) {
                        String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                        String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                        if (orgName.length() > 0) {
                            returnObj.put("organizationName", orgName);
                            //System.out.println("OrgName: " + orgName);
                        }
                    }
                    orgCur.close();
                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                }
                output.append("\n");
                resultSet.put(returnObj);
            }

            outputText.setText(output);

            // Iterate over JSON Array

            try {
                for (int i = 0; i < resultSet.length(); i++) {
                    JSONObject item = resultSet.getJSONObject(i);
                    System.out.println(item.toString(4));
                }
            } catch (JSONException ex) {

            }

            String filePath = fileWriteToSDCard.writeToSDCard(resultSet);

            File file = new File(filePath);

            if(file.exists()) {
                outputText.setText("File Copied To SD Card");
                Toast.makeText(this,"File Copied to SD card", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,"File Not Copied to SD card", Toast.LENGTH_LONG).show();
            }
        }
    }
}
