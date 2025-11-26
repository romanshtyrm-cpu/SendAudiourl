package com.example.sendaudio;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioHttpServer {

    private static final String TAG = "AudioHttpServer";

    private ServerSocket serverSocket;
    private boolean isRunning = false;

    // Последний принятый аудиопоток
    private volatile InputStream lastClientStream;

    public void startServer(int port) {
        isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Log.d(TAG, "HTTP Server started at port " + port);

                while (isRunning) {
                    Socket client = serverSocket.accept();

                    String request = readHeaders(client.getInputStream());

                    if (request.contains("POST /stream")) {
                        handleIncomingAudio(client);
                    } else if (request.contains("GET /stream")) {
                        handleAudioRequest(client);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Server error: " + e.getMessage(), e);
            }
        }).start();
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (Exception ignored) {}
    }

    // Читаем HTTP заголовки
    private String readHeaders(InputStream in) throws Exception {
        StringBuilder builder = new StringBuilder();
        int prev = 0, cur;

        while ((cur = in.read()) != -1) {
            builder.append((char) cur);
            if (prev == '\r' && cur == '\n') {
                if (builder.toString().endsWith("\r\n\r\n")) break;
            }
            prev = cur;
        }

        return builder.toString();
    }

    // Поток приходит с первого телефона (POST)
    private void handleIncomingAudio(Socket client) {
        new Thread(() -> {
            try {
                lastClientStream = client.getInputStream(); // сохраняем входящий поток
                byte[] buffer = new byte[4096];

                // читаем, но не сохраняем — поток передаётся напрямую второму клиенту
                while (isRunning && lastClientStream.read(buffer) != -1) {}

            } catch (Exception ignored) {
            }
        }).start();
    }

    // Второй телефон запрашивает звук (GET)
    private void handleAudioRequest(Socket client) {
        new Thread(() -> {
            try {
                OutputStream output = client.getOutputStream();

                output.write((
                        "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: audio/pcm\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
                ).getBytes());

                byte[] buffer = new byte[4096];

                while (isRunning && lastClientStream != null) {
                    int read = lastClientStream.read(buffer);
                    if (read > 0) output.write(buffer, 0, read);
                }

                client.close();

            } catch (Exception e) {
                Log.e(TAG, "Error while streaming audio: " + e.getMessage(), e);
            }
        }).start();
    }
}