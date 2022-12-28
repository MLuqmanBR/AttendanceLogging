package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Student_Details extends AppCompatActivity {

    TextView name;
    TextView branch;
    TextView rollno;
    TextView blockname;
    TextView phoneno;


    NfcAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Details of Student");

        name = (TextView) findViewById(R.id.nfc_contents_name);
        branch = (TextView) findViewById(R.id.nfc_contents_branch);
        rollno = (TextView) findViewById(R.id.nfc_contents_rollno);
        blockname = (TextView) findViewById(R.id.nfc_contents_blockname);
        phoneno = (TextView) findViewById(R.id.nfc_contents_phoneno);


        readfromIntent(getIntent());
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mAdapter == null){
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    private void readfromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ){
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }


    @SuppressLint("SetTextI18n")
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        // String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; //Get the Text Encoding
        int languageCodeLength = payload[0] & 51; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength -1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding",e.toString());
        }
        String[] student_list = text.split(",");
        List<String> fixedLenghtList = Arrays.asList(student_list);
        ArrayList<String> listOfString;
        listOfString = new ArrayList<String>(fixedLenghtList);
        name.setText(listOfString.get(0));
        branch.setText(listOfString.get(2));
        blockname.setText(listOfString.get(3));
        rollno.setText(listOfString.get(1));
        phoneno.setText(listOfString.get(4));

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readfromIntent(intent);
    }
}