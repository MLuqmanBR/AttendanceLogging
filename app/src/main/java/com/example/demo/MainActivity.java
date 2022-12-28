package com.example.demo;

import static android.nfc.NdefRecord.TNF_WELL_KNOWN;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Objects;
import android.os.Environment;


public class MainActivity extends AppCompatActivity {
    public static final String Error_Detected = "No NFC tag detected";
    public static final String Write_Success = "Text written Successfully";
    public static final String Write_Error = "Error during writing, Try Again";

    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    IntentFilter[] writingTagFilter;
    boolean writeMode;
    Tag myTag;
    Context context;
    TextView edit_message;

    TextView rollno;
    TextView branch;
    TextView blockname;
    TextView phone;
    Button ActivateButton;
    Button ActivateButton2;

    @SuppressLint({"MissingInflatedId", "UnspecifiedImmutableFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("AUN");
        setContentView(R.layout.activity_main);
        edit_message = (TextView) findViewById(R.id.edit_message);
        rollno = (TextView) findViewById(R.id.rollno);
        blockname = (TextView) findViewById(R.id.blockname);
        branch = (TextView) findViewById(R.id.branch);
        phone = (TextView) findViewById(R.id.phoneno);

        ActivateButton = findViewById(R.id.ActivateButton);
        ActivateButton2 = findViewById(R.id.ActivateButton2);
        ActivateButton2.setOnClickListener(view -> {
            Intent AttendanceActIntent = new Intent(this,Attendance.class);
            startActivity(AttendanceActIntent);
        });
        context = this;
        ActivateButton.setOnClickListener(view -> {
            try {
                if(myTag == null){
                    Toast.makeText(context, Error_Detected,Toast.LENGTH_LONG).show();
                }
                else{
                    write(edit_message.getText().toString() + "," + rollno.getText().toString() + "," + branch.getText().toString() + "," + blockname.getText().toString() + "," + phone.getText().toString(), myTag);

                    // Create a JSON object for the data
                    JSONObject dataObject = new JSONObject();
                    dataObject.put("name", edit_message.getText().toString());
                    dataObject.put("rollNo", rollno.getText().toString());
                    dataObject.put("branch", branch.getText().toString());
                    dataObject.put("blockName", blockname.getText().toString());
                    dataObject.put("phoneNo", phone.getText().toString());

                    // Create an array to hold the dates and times
                    JSONArray dateArray = new JSONArray();

                    // Add the current date and time to the array
                    Calendar currentTime = Calendar.getInstance();
                    dateArray.put(currentTime.getTime().toString());

                    // Add the array to the object
                    dataObject.put("dates", dateArray);

                    // Create a File object for the desired directory
                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                    // Create a new file in the desired directory
                    //File file = new File(downloadDir, "JSON_Data/student_data.json");

                    // Write the data to the file
                    //FileOutputStream outputStream = new FileOutputStream(file);
                    //outputStream.write(dataObject.toString().getBytes());
                    //outputStream.close();

                    // Read the file and parse the JSON array
                    //File file = new File(getFilesDir(), "student_data.json");

                    // Create a new file in the desired directory
                    File file = new File(downloadDir, "JSON_Data/student_data.json");

                    // Make sure the required directories exist
                    file.getParentFile().mkdirs();

                    JSONArray dataArray;

                    try {
                        FileInputStream inputStream = new FileInputStream(file);
                        String inputString = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                        inputStream.close();
                        dataArray = new JSONArray(inputString);
                    } catch (FileNotFoundException e) {
                        // Create a new file with an empty array if the file does not exist
                        dataArray = new JSONArray();
                    }

                    // Add the new data object to the array
                    dataArray.put(dataObject);

                    // Write the updated array back to the file
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(dataArray.toString().getBytes());
                    outputStream.close();

                    Toast.makeText(context,Write_Success,Toast.LENGTH_LONG).show();
                }

            }catch (Exception e){
                Toast.makeText(context, Write_Error, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mAdapter == null){
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_MUTABLE);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilter = new IntentFilter[] { tagDetected };
    }

    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createRecord(String text) {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes(StandardCharsets.US_ASCII);
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];

        // Set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // Copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        return new NdefRecord(TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        WriteModeOn();
    }

    private void WriteModeOn() {
        writeMode = true;
        mAdapter.enableForegroundDispatch(this, mPendingIntent, writingTagFilter, null);
    }

    private void WriteModeOff() {
        writeMode = false;
        mAdapter.disableForegroundDispatch(this);
    }


}