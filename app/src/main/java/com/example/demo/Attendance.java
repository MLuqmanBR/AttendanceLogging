package com.example.demo;

import static android.nfc.NdefRecord.TNF_WELL_KNOWN;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;






public class Attendance extends AppCompatActivity {
    public static final String Error_Detected = "No NFC tag detected";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public class Student {
        private String name;
        private String rollNo;
        private String blockName;
        private String branch;
        private String phoneNo;
        private String email;
        private String project_title;
        private String domain;
        private String progress;
        private List<String> dates;

        public Student(String name, String rollNo, String branch, String blockName, String phoneNo, String email, String project_title, String domain, String progress, List<String> dates) {
            this.name = name;
            this.rollNo = rollNo;
            this.branch = branch;
            this.blockName = blockName;
            this.phoneNo = phoneNo;
            this.email = email;
            this.project_title = project_title;
            this.domain = domain;
            this.progress = progress;
            this.dates = dates;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRollNo() {
            return rollNo;
        }

        public void setRollNo(String rollNo) {
            this.rollNo = rollNo;
        }

        public String getBlockName() {
            return blockName;
        }

        public void setBlockName(String blockName) {
            this.blockName = blockName;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getProjectTitle() {
            return project_title;
        }

        public void setProjectTitle(String project_title) {
            this.project_title = project_title;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String progress) {
            this.progress = progress;
        }

        public List<String> getDates() {
            return this.dates;
        }

        public void setDates(List<String> dates) {
            this.dates = dates;
        }

        public Student fromJson(JSONObject jsonObject) throws JSONException {
            String name = jsonObject.getString("Name");
            String rollNo = jsonObject.getString("Roll No.");
            String blockName = jsonObject.getString("Degree");
            String branch = jsonObject.getString("Branch");
            String phoneNo = jsonObject.getString("Phone No.");
            String email = jsonObject.getString("Email");
            String project_title = jsonObject.getString("Project Title");
            String domain = jsonObject.getString("Domain");
            String progress = jsonObject.getString("Progress");
            JSONArray datesJsonArray = jsonObject.getJSONArray("dates");
            List<String> dates = new ArrayList<>();
            for (int i = 0; i < datesJsonArray.length(); i++) {
                dates.add(datesJsonArray.getString(i));
            }

            return new Student(name, rollNo, blockName, branch, phoneNo, email, project_title, domain, progress, dates);
        }

        public List<Student> fromJson(JSONArray jsonArray) throws JSONException {
            List<Student> students = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                students.add(fromJson(jsonArray.getJSONObject(i)));
            }
            return students;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Name", name);
            jsonObject.put("Roll No.", rollNo);
            jsonObject.put("Degree", blockName);
            jsonObject.put("Branch", branch);
            jsonObject.put("Phone No.", phoneNo);
            jsonObject.put("Email", email);
            jsonObject.put("Project Title", project_title);
            jsonObject.put("Domain", domain);
            jsonObject.put("Progress", progress);
            JSONArray datesJsonArray = new JSONArray(dates);
            jsonObject.put("dates", datesJsonArray);

            return jsonObject;
        }

        public String toJson(List<Student> students) throws JSONException {
            JSONArray jsonArray = new JSONArray();
            for (Student student : students) {
                jsonArray.put(student.toJson());
            }
            return jsonArray.toString();
        }
    }

    List<Student> students;
    Button button1;
    Button button2;
    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    IntentFilter[] writingTagFilter;
    Tag myTag;
    Context context;

    @SuppressLint({"MissingInflatedId", "UnspecifiedImmutableFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Student Attendance");
        button1 = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button1.setOnClickListener(view -> {
            Intent StudentDetailsIntent = new Intent(this, Student_Details.class);
            startActivity(StudentDetailsIntent);
        });
        button2.setOnClickListener(view -> {
            try {

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                String formattedDate = dateFormat.format(calendar.getTime());

                //String localFilePath = context.getFilesDir().getAbsolutePath() + "/student_data.json";
                //File localFile = new File(localFilePath);
                File localFile = new File(getFilesDir(), "student_data.json");

                //String externalFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/JSON_Data/json_file.json";
                //File externalFile = new File(externalFilePath);
                File externalDir = new File(Environment.getExternalStorageDirectory(), "Download/Students Logs");
                if (!externalDir.exists()) {
                    externalDir.mkdirs();
                }
                File externalFile = new File(Environment.getExternalStorageDirectory(), "Download/Students Logs/" + formattedDate + ".json");

                    FileInputStream inputStream = new FileInputStream(localFile);
                        FileOutputStream outputStream = new FileOutputStream(externalFile);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                Toast.makeText(this , "Downloaded File to Download/Student Logs/" + formattedDate + ".json", Toast.LENGTH_LONG).show();
                //Toast.makeText(context, "Downloaded Successfull", Toast.LENGTH_LONG).show();
            }
            catch (Exception e){
                Toast.makeText(this, "Download Failed", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        });

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }
        try {
            readfromIntent(getIntent());
        } catch (JSONException | IOException e) {
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
        String[] studentData = text.split("#");

        String name = studentData[0];
        String rollNo = studentData[1];

        /* Read the file and parse the JSON array
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/JSON_Data", "student_data.json");
        FileInputStream inputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputString = reader.readLine();
        inputStream.close();
        reader.close();*/

        // Read the file and parse the JSON array
        File file = new File(getFilesDir(), "student_data.json");
        //File file = new File(Environment.getExternalStorageDirectory(), "Download/student_data.json");
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
            if (obj.getString("Name").equals(name) && obj.getString("Roll No.").equals(rollNo)) {
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
        } catch (JSONException | IOException e) {
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