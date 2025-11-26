package com.example.myapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class AudioService extends Service {

    private AudioHttpServer server;
    private static final String CHANNEL_ID = "audio_service_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Запускаем HTTP-аудиосервер
        server = new AudioHttpServer(8080);
        server.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Аудио-сервер активен")
                .setContentText("Передача звука работает")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();

        startForeground(1, notification);

        return START_STICKY; // Автовосстановление после убийства системой
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // привязка не нужна
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Streaming Background Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}