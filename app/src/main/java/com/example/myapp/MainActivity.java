package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button startStopBtn;
    private TextView serverStatus;
    private TextView urlText;

    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startStopBtn = findViewById(R.id.startStopBtn);
        serverStatus = findViewById(R.id.serverStatus);
        urlText = findViewById(R.id.urlText);

        startStopBtn.setOnClickListener(v -> {
            if (!isRunning) {
                startService(new Intent(this, AudioService.class));
                isRunning = true;
                updateUI();
            } else {
                stopService(new Intent(this, AudioService.class));
                isRunning = false;
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (isRunning) {
            serverStatus.setText("ON");
            serverStatus.setTextColor(0xFF00AA00);
            startStopBtn.setText("STOP");

            String ip = NetworkUtils.getLocalIpAddress();
            urlText.setText("URL: http://" + ip + ":8080");

        } else {
            serverStatus.setText("OFF");
            serverStatus.setTextColor(0xFFFF0000);
            startStopBtn.setText("START");
            urlText.setText("URL: —");
        }
    }
}