package com.example.myapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class AudioService extends Service {

    private AudioHttpServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        server = new AudioHttpServer();
        server.startServer(12345);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stopServer();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}