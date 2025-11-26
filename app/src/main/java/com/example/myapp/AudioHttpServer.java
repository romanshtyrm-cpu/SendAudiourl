
package com.example.myapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioHttpServer {

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private volatile boolean running = false;
    private Thread serverThread;
    private int usedPort = 8080;

    private int findFreePort(int startPort) {
        int port = startPort;
        while (port < 9000) {
            try {
                ServerSocket socket = new ServerSocket(port);
                socket.close();
                return port;
            } catch (Exception ignored) {
                port++;
            }
        }
        return startPort;
    }

    public int startAutoPort() {
        usedPort = findFreePort(8080);
        startServer(usedPort);
        return usedPort;
    }

    public void startServer(int port) {
        if (running) return;
        running = true;

        serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (running) {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client)).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }

    public void stopServer() {
        running = false;
        if (serverThread != null) serverThread.interrupt();
    }

    private void handleClient(Socket client) {
        try {
            OutputStream out = client.getOutputStream();

            out.write(("HTTP/1.0 200 OK\r\n" +
                    "Content-Type: audio/wav\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Connection: close\r\n\r\n").getBytes());

            writeWavHeader(out);

            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE, CHANNEL, FORMAT, bufferSize);

            byte[] buffer = new byte[bufferSize];

            recorder.startRecording();

            while (running) {
                int read = recorder.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.write(buffer, 0, read);
                }
            }

            recorder.stop();
            recorder.release();

            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeWavHeader(OutputStream out) throws IOException {
        int byteRate = SAMPLE_RATE * 2;

        byte[] header = new byte[44];
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[4] = header[5] = header[6] = header[7] = (byte) 0xFF;
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[20] = 1; header[22] = 1;
        header[24] = (byte)(SAMPLE_RATE & 0xff);
        header[25] = (byte)((SAMPLE_RATE >> 8) & 0xff);
        header[28] = (byte)(byteRate & 0xff);
        header[29] = (byte)((byteRate >> 8) & 0xff);
        header[32] = 2; header[34] = 16;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = header[41] = header[42] = header[43] = (byte)0xFF;
        out.write(header);
    }
}
