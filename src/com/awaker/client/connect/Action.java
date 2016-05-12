package com.awaker.client.connect;

import com.awaker.client.media.MediaManager;
import com.awaker.client.media.TrackWrapper;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@SuppressWarnings({"WeakerAccess"})
public class Action {
    private static final int BUFFER_SIZE = 8192;
    private String filePath;
    private long fileLength = 0;

    private final String command;
    private String[] args;

    private final ResponseListener listener;
    private UploadStatusListener uploadStatusListener;


    private Action(String filePath, String command, ResponseListener listener, UploadStatusListener uploadStatusListener) {
        this.filePath = filePath;
        this.command = command;
        this.listener = listener;
        this.uploadStatusListener = uploadStatusListener;
    }

    private Action(String command, String[] args, ResponseListener listener) {
        this.command = command;
        this.args = args;
        this.listener = listener;
    }

    public static Action play(ResponseListener listener) {
        return new Action("play", null, listener);
    }

    public static Action pause(ResponseListener listener) {
        return new Action("pause", null, listener);
    }

    public static Action playFile(String uri, final ResponseListener listener, UploadStatusListener uploadStatusListener) {
        return new Action(uri, "playFile", listener, uploadStatusListener);
    }

    public static Action togglePlayPause(ResponseListener listener) {
        return new Action("togglePlayPause", null, listener);
    }

    public static Action playFiles(ArrayList<String> uris, ResponseListener listener, UploadStatusListener uploadStatusListener) {
        return new Action("", null, listener);//TODO mach das
    }

    public static Action playFromPosition(int position, ResponseListener listener) {
        return new Action("playFromPosition", new String[]{String.valueOf(position)}, listener);
    }

    public static Action setShuffle(boolean shuffle, ResponseListener listener) {
        return new Action("setShuffle", new String[]{String.valueOf(shuffle)}, listener);
    }

    public static Action setRepeatMode(int repeatMode, ResponseListener listener) {
        return new Action("setRepeatMode", new String[]{String.valueOf(repeatMode)}, listener);
    }

    public static Action setVolume(int volume, ResponseListener listener) {
        return new Action("setVolume", new String[]{String.valueOf(volume)}, listener);
    }

    public static Action playNext(ResponseListener listener) {
        return new Action("playNext", null, listener);
    }

    public static Action playPrevious(ResponseListener listener) {
        return new Action("playPrevious", null, listener);
    }

    public static Action setRGBColor(int red, int green, int blue, ResponseListener listener) {
        return new Action("setRGBColor",
                new String[]{String.valueOf(red), String.valueOf(green), String.valueOf(blue)},
                listener);
    }

    public static Action setColor(Color color, ResponseListener listener) {
        return new Action("setColor", new String[]{String.valueOf(color.getRGB())}, listener);
    }

    /**
     * Setzt den Farbmodus. Mögliche Werte: music, custom, colorCircle
     *
     * @param musicMode Der Farbmodus
     * @param listener  Der Listener für die Antwort vom Server
     * @return Action
     */
    public static Action setColorMode(String musicMode, ResponseListener listener) {
        return new Action("setColorMode", new String[]{musicMode}, listener);
    }

    public static Action setWhiteBrightness(int brightness, ResponseListener listener) {
        return new Action("setWhiteBrightness", new String[]{String.valueOf(brightness)}, listener);
    }

    public static Action setColorBrightness(int brightness, ResponseListener listener) {
        return new Action("setColorBrightness", new String[]{String.valueOf(brightness)}, listener);
    }

    public static Action changeVisualisation(ResponseListener listener) {
        return new Action("changeVisualization", null, listener);
    }

    public static Action getStatus(ResponseListener listener) {
        return new Action("getStatus", null, listener);
    }

    public static Action sendString(String contents) {
        return new Action("sendString", new String[]{contents}, null);
    }

    public static Action shutdown() {
        return new Action("shutdown", null, null);
    }

    public void execute(Socket socket) throws IOException {
        //Befehlsstring basteln

        String action = getCommandString();
        if (action == null)
            return;

        //In bytes kodieren
        byte[] commandBytes = action.getBytes(StandardCharsets.UTF_8);

        //abschicken
        OutputStream os = socket.getOutputStream();
        os.write(commandBytes);

        //Datei hochladen, falls notwendig
        if (command.equals("uploadAndPlayFile")) {
            uploadFile(os);
        } else if (command.equals("sendString")) {
            sendString(args[0], os);
        }

        //Antwort lesen
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        String response = in.readLine();

        if (command.equals("playFile") && response != null && response.equals("file not found")) {
            new Action(filePath, "uploadAndPlayFile", listener, uploadStatusListener).execute(socket);
        } else if (listener != null) {
            listener.responseReceived(this, response);
        }
    }

    private String getCommandString() {
        String action = command;
        switch (command) {
            case "playFile":
                TrackWrapper trackWrapper = MediaManager.readFile(new File(filePath));

                if (trackWrapper == null) {
                    listener.fileNotFound();
                } else {
                    action += ";" + trackWrapper.title;
                    action += ";" + trackWrapper.artist;
                }

                break;
            case "uploadAndPlayFile":
                File fileToSend = new File(filePath);

                if (fileToSend.exists() && fileToSend.isFile()) {

                    fileLength = fileToSend.length();
                    String fileName = fileToSend.getName();

                    action += ";" + fileName;
                    action += ";" + fileLength;
                    System.out.println("File upload size: " + fileLength);
                } else {
                    System.out.println("invalid file, cant upload");
                    return null;
                }
                break;
            case "sendString":
                //Muss extra behandelt werden, damit auch Zeilenumbrüche korrekt behandelt werden
                //Zeichenkettenlänge wird mitgesendet
                action += ";" + args[0].length();
                break;
            default:
                if (args != null) {
                    for (String arg : args) {
                        action += ";" + arg;
                    }
                }
                break;
        }
        action += '\n';
        return action;
    }

    private static void sendString(String str, OutputStream os) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        //abschicken
        os.write(bytes);
    }

    private void uploadFile(OutputStream os) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[BUFFER_SIZE];

        int totalUploadedBytes = 0;

        int readByteCount = fis.read(buffer);
        int iterationCount = 0;
        while (readByteCount > 0) {
            totalUploadedBytes += readByteCount;
            os.write(buffer, 0, readByteCount);

            readByteCount = fis.read(buffer);

            iterationCount++;

            if (uploadStatusListener != null && iterationCount > 20) {
                int statusPercent = Math.min((int) (((totalUploadedBytes * 1.0) / fileLength) * 100), 100);

                String status = String.format("%s hochgeladen (%s/%s)", statusPercent + "%", totalUploadedBytes, fileLength);

                uploadStatusListener.updateUploadStatus(status, statusPercent);
                iterationCount = 0;
            }
        }
        fis.close();
        if (uploadStatusListener != null) {
            uploadStatusListener.updateUploadStatus("finished", 101);
        }
        System.out.println("bytes sent: " + totalUploadedBytes);
    }

}
