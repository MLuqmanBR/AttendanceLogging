package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static android.nfc.NdefRecord.TNF_WELL_KNOWN;

import android.annotation.SuppressLint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import android.os.Environment;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.os.Parcelable;
import android.util.Log;
import java.io.UnsupportedEncodingException;

public class Attendance extends AppCompatActivity {
    public static final String Error_Detected = "No NFC tag detected";

    Button button1;
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    IntentFilter[] writingTagFilter;
    Tag myTag;

    @SuppressLint({"MissingInflatedId", "UnspecifiedImmutableFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Student Attendance");
        button1 = findViewById(R.id.button);
        button1.setOnClickListener(view -> {
            Intent StudentDetailsIntent = new Intent(this,Student_Details.class);
            startActivity(StudentDetailsIntent);
        });
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }
        try {
            readfromIntent(getIntent());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilter = new IntentFilter[]{tagDetected};

    }
    private void buildTagViews(NdefMessage[] msgs) throws IOException, JSONException {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        // String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; //Get the Text Encoding
        int languageCodeLength = payload[0] & 51; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }
        String[] studentData = text.split(",");

        String name = studentData[0];
        String rollNo = studentData[1];

        // Read the file and parse the JSON array
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/JSON_Data", "student_data.json");
        FileInputStream inputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputString = reader.readLine();
        inputStream.close();
        reader.close();

        JSONArray dataArray = new JSONArray(inputString);

        // Search for the object with the matching name
        JSONObject dataObject = null;
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject obj = dataArray.getJSONObject(i);
            if (obj.getString("name").equals(name) && obj.getString("rollNo").equals(rollNo)) {
                dataObject = obj;
                break;
            }
        }

        // Update the date and time array
        if (dataObject != null) {
            JSONArray dateArray = dataObject.getJSONArray("dates");
            Calendar currentTime = Calendar.getInstance();
            dateArray.put(currentTime.getTime().toString());

        // Write the updated array back to the file
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(dataArray.toString().getBytes());
        outputStream.close();
    }
}
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            readfromIntent(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readfromIntent(Intent intent) throws JSONException, IOException {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage[] messages = getNdefMessages(intent);
            buildTagViews(messages);
        }
    }

    private NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(TNF_WELL_KNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{
                        record
                });
                msgs = new NdefMessage[]{
                        msg
                };
            }
        } else {
            Log.d("Unknown intent.", action);
            finish();
        }
        return msgs;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, writingTagFilter, null);
        }
    }
}