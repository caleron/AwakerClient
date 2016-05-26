package com.awaker.client.connect;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

@SuppressWarnings("unused")
public class ServerStatus implements Serializable, ResponseListener {
    private static final long serialVersionUID = 1978521315878247101L;

    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ONE = 1;
    public static final int REPEAT_MODE_ALL = 2;

    private boolean shuffle = true;
    private int repeatMode = REPEAT_MODE_ALL;
    private boolean playing = false;

    private int playPosition = 0;
    private int trackLength = 100;

    private final ServerConnect serverConnect;
    private final StatusChangedListener listener;

    private String currentTitle = "", currentArtist = "", currentAlbum = "";
    private String colorMode;
    private int whiteBrightness;
    private Color currentColor;
    private int colorBrightness;
    private int volume;
    private int serverVolume;

    private Timer volumeApplyTimer;
    private long lastVolumeChange = 0L;

    public ServerStatus(StatusChangedListener listener, ServerConnect serverConnect) {
        this.serverConnect = serverConnect;
        this.listener = listener;

        volumeApplyTimer = new Timer(300, e -> {
            volume = serverVolume;
            listener.serverStatusChanged(false);
        });
        volumeApplyTimer.setRepeats(false);
    }

    /**
     * Fordert einen neuen Serverstatus an.
     */
    public void requestNewStatus() {
        serverConnect.executeAction(Action.getStatus(this));
    }

    /**
     * Wendet die neuen Statusdaten an
     *
     * @param str Der Status-String vom Server
     * @return true, wenn sich der Titel geändert hat.
     */
    private boolean parseNewState(String str) {
        if (str == null)
            return false;

        String[] strings = str.split(";");

        String oldTitle = currentTitle;
        String oldArtist = currentArtist;

        currentTitle = "";
        currentArtist = "";
        currentAlbum = "";
        playPosition = 0;
        trackLength = 100;

        for (String attr : strings) {
            String[] pair = attr.split(":");

            switch (pair[0]) {
                case "playing":
                    playing = Boolean.parseBoolean(pair[1]);
                    break;
                case "shuffle":
                    shuffle = Boolean.parseBoolean(pair[1]);
                    break;
                case "repeat":
                    repeatMode = Integer.parseInt(pair[1]);
                    break;
                case "currentTitle":
                    currentTitle = pair[1];
                    break;
                case "currentArtist":
                    currentArtist = pair[1];
                    break;
                case "currentAlbum":
                    currentAlbum = pair[1];
                    break;
                case "trackLength":
                    trackLength = Integer.parseInt(pair[1]);
                    break;
                case "playPosition":
                    playPosition = Integer.parseInt(pair[1]);
                    break;
                case "volume":
                    if (lastVolumeChange + 1000 > System.currentTimeMillis()) {
                        serverVolume = Integer.parseInt(pair[1]);
                        volumeApplyTimer.restart();
                    } else {
                        volume = Integer.parseInt(pair[1]);
                    }

                    break;
                case "colorMode":
                    colorMode = pair[1];
                    break;
                case "whiteBrightness":
                    whiteBrightness = Integer.parseInt(pair[1]);
                    break;
                case "colorBrightness":
                    colorBrightness = Integer.parseInt(pair[1]);
                    break;
                case "currentColor":
                    currentColor = new Color(Integer.parseInt(pair[1]));
                    break;
                default:
                    System.out.println("unknown attribute: " + pair[0]);
                    break;
            }
        }
        //System.out.println("new play position: " + playPosition);
        if (currentArtist.isEmpty() && oldArtist.isEmpty()) {
            //Falls der Dateiname als Titel herhalten musste, etwa wenn keine Tags vorhanden sind
            return currentTitle.equals(oldTitle);
        }
        return !(currentArtist.equals(oldArtist) && currentTitle.equals(oldTitle));
    }

    /**
     * Wird ausgelöst, wenn eine Antwort vom Server kommt
     *
     * @param sourceAction Die Ursprungsaktion
     * @param response     Die Antwort
     */
    @Override
    public void responseReceived(Action sourceAction, String response) {
        boolean newSong = parseNewState(response);

        listener.serverStatusChanged(newSong);
        System.out.println(response);
    }

    @Override
    public void fileNotFound() {
        listener.fileNotFound();
    }

    /**
     * Startet die Wiedergabe einer Datei
     *
     * @param path                 Uri zur Datei
     * @param uploadStatusListener UploadStatusListener, für den Fall, dass die Datei noch nicht auf dem Server ist.
     */
    public void playFile(String path, UploadStatusListener uploadStatusListener) {
        serverConnect.executeAction(Action.playFile(path, this, uploadStatusListener));
    }

    /**
     * Lädt eine Datei hoch.
     *
     * @param path                 Der Pfad zur Datei
     * @param uploadStatusListener der {@link UploadStatusListener}
     */
    public void uploadFile(String path, UploadStatusListener uploadStatusListener) {
        serverConnect.executeAction(Action.uploadFile(path, this, uploadStatusListener));
    }

    /**
     * Spielt den vorigen Track.
     */
    public void playPrevious() {
        serverConnect.executeAction(Action.playPrevious(this));
    }

    /**
     * Spielt den nächsten Track.
     */
    public void playNext() {
        serverConnect.executeAction(Action.playNext(this));
    }

    /**
     * Setzt die Abspielposition
     *
     * @param position Die neue Position. Muss kleiner als {@link ServerStatus#getTrackLength()} sein
     */
    public void playFromPosition(int position) {
        serverConnect.executeAction(Action.playFromPosition(position, this));
    }

    /**
     * Setzt den Farbmodus. Mögliche Werte: music, custom, colorCircle. Hat keine Auswirkung, falls der Modus dadurch
     * nicht geändert wird.
     *
     * @param mode Der neue Farbmodus
     */
    public void setColorMode(String mode) {
        if (mode != null && mode.length() > 0 && !mode.equals(colorMode)) {
            colorMode = mode;
            serverConnect.executeAction(Action.setColorMode(mode, this));
        }
    }

    /**
     * Gibt den Farbmodus zurück.
     *
     * @return Der aktuelle Farbmodus
     */
    public String getColorMode() {
        return colorMode;
    }

    /**
     * Setzt die Helligkeit der weißen LED
     *
     * @param brightness Zahl zwischen 0 und 100
     */
    public void setWhiteBrightness(int brightness) {
        whiteBrightness = brightness;
        serverConnect.executeAction(Action.setWhiteBrightness(brightness, this));
    }

    /**
     * Setzt die Lichtfarbe der RGB-LEDs.
     *
     * @param color Die Farbe als int in ARGB-Kodierung.
     */
    public void setRGBColor(Color color) {
        currentColor = color;
        serverConnect.executeAction(Action.setColor(color, this));
    }

    /**
     * Setzt die Lichtfarbe der RGB-LEDs mit einzelnen Kanälen. Mögliche Werte zwischen 0 und 255
     *
     * @param red   Die rote Farbkomponente
     * @param green Die grüne Farbkomponente
     * @param blue  Die blaue Farbkomponente
     */
    public void setRGBColor(int red, int green, int blue) {
        currentColor = new Color(red, green, blue);
        serverConnect.executeAction(Action.setRGBColor(red, green, blue, this));
    }

    /**
     * Gibt die letzte Wiedergabeposition zurück
     *
     * @return die letzte Wiedergabeposition
     */
    public int getPlayPosition() {
        return playPosition;
    }

    /**
     * Gibt die Länge des aktuellen Tracks in Sekunden zurück.
     *
     * @return Länge des aktuellen Tracks in Sekunden
     */
    public int getTrackLength() {
        return trackLength;
    }

    /**
     * Gibt den aktuellen Titel zurück.
     *
     * @return der aktuelle Titel
     */
    public String getCurrentTitle() {
        return currentTitle;
    }

    /**
     * Gibt den Künstler des aktuellen Tracks zurück.
     *
     * @return Künstler des aktuellen Tracks
     */
    public String getCurrentArtist() {
        return currentArtist;
    }

    /**
     * Gibt das Album des aktuellen Tracks zurück.
     *
     * @return Album des aktuellen Tracks
     */
    public String getCurrentAlbum() {
        return currentAlbum;
    }

    /**
     * Gibt die verbleibenden Sekunden zurück.
     *
     * @return entspricht getTrackLength - getPlayPosition
     */
    public int getRemainingSeconds() {
        return trackLength - playPosition;
    }

    /**
     * Gibt den Shufflemodus zurück.
     *
     * @return True, wenn Shufflemodus aktiv
     */
    public boolean isShuffle() {
        return shuffle;
    }

    /**
     * Wechselt in den anderen Shufflemodus (an/aus)
     */
    public void toggleShuffle() {
        shuffle = !shuffle;
        serverConnect.executeAction(Action.setShuffle(shuffle, this));
    }

    /**
     * Gibt den aktuellen Wiederholungsmodus wieder
     *
     * @return aktueller Wiederholungsmethode als REPEAT_MODE-Konstante
     */
    public int getRepeatMode() {
        return repeatMode;
    }

    /**
     * Wechselt in den nächsten Wiederholungsmodus
     */
    public void nextRepeatMode() {
        repeatMode = (repeatMode + 1) % 3;
        serverConnect.executeAction(Action.setRepeatMode(repeatMode, this));
    }

    public void setVolume(int volume) {
        if (volume >= 100) {
            volume = 100;
        } else if (volume <= 0) {
            volume = 0;
        }

        if (this.volume != volume) {
            this.volume = volume;
            serverConnect.executeAction(Action.setVolume(volume, this));
            lastVolumeChange = System.currentTimeMillis();
        }
    }

    public void increaseVolume() {
        setVolume(volume + 2);
    }

    public void decreaseVolume() {
        setVolume(volume - 2);
    }

    public int getVolume() {
        return volume;
    }

    /**
     * Gibt den Playbackstatus zurück.
     *
     * @return True, wenn aktuell ein Song gespielt wird.
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Wechselt zwischen Play/Pause
     */
    public void togglePlaying() {
        playing = !playing;
        serverConnect.executeAction(Action.togglePlayPause(this));
    }

    /**
     * Gibt die Helligkeit des weißen Farbkanals zurück
     *
     * @return Helligkeit des weißen Farbkanals als Zahl zwischen 0 und 100
     */
    public int getWhiteBrightness() {
        return whiteBrightness;
    }

    /**
     * Gibt die Helligkeit der RGB-LEDs zurück
     *
     * @return Die Helligkeit als Zahl zwischen 0 und 100
     */
    public int getColorBrightness() {
        return colorBrightness;
    }

    /**
     * Setzt die Helligkeit der RGB-LEDs.
     *
     * @param colorBrightness Zahl zwischen 0 und 100
     */
    public void setColorBrightness(int colorBrightness) {
        this.colorBrightness = colorBrightness;
        serverConnect.executeAction(Action.setColorBrightness(colorBrightness, this));
    }

    /**
     * Gibt die aktuelle Farbe zurück
     *
     * @return Farbwerd als int in ARGB-Kodierung
     */
    public Color getCurrentColor() {
        return currentColor;
    }

    /**
     * Sendet einen String an den Server. Wird in einem Fenster ausgegeben, falls der Server auf Windows ausgeführt
     * wird.
     *
     * @param str Der zu sendende String
     */
    public void sendString(String str) {
        serverConnect.executeAction(Action.sendString(str));
    }

    public void shutdownServer() {
        serverConnect.executeAction(Action.shutdown());
    }
}
