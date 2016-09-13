package com.awaker.client.integration;

import com.awaker.client.connect.ServerConnect;
import com.awaker.client.connect.json.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Integrator {

    private ServerConnect connect;
    private Thread thread;

    private boolean dimmed = false;

    public Integrator(ServerConnect connect) {
        this.connect = connect;
    }

    public void startWatch() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this::watchIntegrator);
            thread.start();
        }
    }

    public void stopWatch() {
        thread.interrupt();
    }

    private void watchIntegrator() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("AwakerIntegrator.exe");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String output;
        while (process.isAlive()) {
            try {
                output = inputStream.readLine();
                processMessage(output);
                System.out.println(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String msg) {
        int index = msg.indexOf(":");
        String type, argument = "";
        if (index >= 0) {
            type = msg.substring(0, index);
            argument = msg.substring(index + 1);
        } else {
            type = msg;
        }

        switch (type) {
            case "FullScreenWindowFocused":
                if (argument.toLowerCase().contains("media player")) {
                    new Command().setWhiteBrightness(10, true).send(connect);
                    dimmed = true;
                }
                break;
            case "FullScreenWindowFocusLost":
                if (dimmed) {
                    new Command().setWhiteBrightness(70, true).send(connect);
                    dimmed = false;
                }
                break;
        }
    }
}
