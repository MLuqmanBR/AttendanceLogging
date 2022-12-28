package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import java.util.Objects;

public class Attendance extends AppCompatActivity {

    Button button1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        Objects.requireNonNull(getSupportActionBar()).hide();
        button1 = findViewById(R.id.button);
        button1.setOnClickListener(view -> {
            Intent StudentDetailsIntent = new Intent(this,Attendance.class);
            startActivity(StudentDetailsIntent);
        });

    }
}