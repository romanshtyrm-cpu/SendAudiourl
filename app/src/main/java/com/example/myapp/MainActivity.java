package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private TextView ipText;
    private boolean isRunning = false;

    private static final int PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        ipText = findViewById(R.id.ipText);

        showLocalIp();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRunning) {
                    stopAudioServer();
                } else {
                    tryStartAudioServer();
                }
            }
        });
    }

    private void tryStartAudioServer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST);
        } else {
            startAudioServer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAudioServer();
            }
        }
    }

    private void startAudioServer() {
        Intent intent = new Intent(this, AudioService.class);
        ContextCompat.startForegroundService(this, intent);

        isRunning = true;
        startButton.setText("STOP");
    }

    private void stopAudioServer() {
        Intent intent = new Intent(this, AudioService.class);
        stopService(intent);

        isRunning = false;
        startButton.setText("START");
    }

    private void showLocalIp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(WIFI_SERVICE);

        if (wifiManager != null) {
            String ip = Formatter.formatIpAddress(
                    wifiManager.getConnectionInfo().getIpAddress());
            ipText.setText("IP: " + ip + " :8080");
        } else {
            ipText.setText("IP: недоступно");
        }
    }
}