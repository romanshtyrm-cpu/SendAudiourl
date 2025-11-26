
package com.example.myapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private boolean serverRunning = false;
    private AudioHttpServer audioServer;

    private TextView serverStatus;
    private Button startStopBtn;
    private TextView urlText;

    private int currentPort = 8080;

    private static final int REQ_AUDIO = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioServer = new AudioHttpServer();

        serverStatus = findViewById(R.id.serverStatus);
        startStopBtn = findViewById(R.id.startStopBtn);
        urlText = findViewById(R.id.urlText);

        updateUI();

        startStopBtn.setOnClickListener(v -> {

            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        REQ_AUDIO
                );
                return;
            }

            serverRunning = !serverRunning;

            if (serverRunning) {
                currentPort = audioServer.startAutoPort();
            } else {
                audioServer.stopServer();
            }

            updateUI();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStopBtn.performClick();
            } else {
                serverRunning = false;
                updateUI();
            }
        }
    }

    private void updateUI() {
        if (serverRunning) {
            serverStatus.setText("ON");
            serverStatus.setTextColor(0xFF00FF00);
            startStopBtn.setText("STOP");
            urlText.setText("URL: http://" + getLocalIp() + ":" + currentPort);
        } else {
            serverStatus.setText("OFF");
            serverStatus.setTextColor(0xFFFF0000);
            startStopBtn.setText("START");
            urlText.setText("URL: —");
        }
    }

    private String getLocalIp() {
        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().contains(".")) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "0.0.0.0";
    }
}
