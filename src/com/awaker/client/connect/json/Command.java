package com.awaker.client.connect.json;

import com.awaker.client.connect.ServerConnect;

import java.awt.*;

@SuppressWarnings({"WeakerAccess", "unused", "FieldCanBeLocal"})
public class Command {
    public static final String PLAY = "play";
    public static final String PLAY_ID = "play_id";
    public static final String PLAY_FROM_POSITION = "playFromPosition";
    public static final String PAUSE = "pause";
    public static final String STOP = "stop";
    public static final String TOGGLE_PLAY_PAUSE = "togglePlayPause";
    public static final String PLAY_FILE = "playFile";
    public static final String UPLOAD_AND_PLAY_FILE = "uploadAndPlayFile";
    public static final String CHECK_FILE = "checkFile";
    public static final String UPLOAD_FILE = "uploadFile";
    public static final String PLAY_NEXT = "playNext";
    public static final String PLAY_PREVIOUS = "playPrevious";
    public static final String SET_SHUFFLE = "setShuffle";
    public static final String SET_REPEAT_MODE = "setRepeatMode";
    public static final String SET_VOLUME = "setVolume";
    public static final String SET_WHITE_BRIGHTNESS = "setWhiteBrightness";
    public static final String SET_ANIMATION_BRIGHTNESS = "setAnimationBrightness";
    public static final String SET_COLOR_MODE = "setColorMode";
    public static final String SET_COLOR = "setColor";
    public static final String SET_RGBCOLOR = "setRGBColor";
    public static final String CHANGE_VISUALIZATION = "changeVisualization";
    public static final String CREATE_PLAYLIST = "createPlaylist";
    public static final String REMOVE_PLAYLIST = "removePlaylist";
    public static final String ADD_TRACK_TO_PLAYLIST = "addTrackToPlaylist";
    public static final String REMOVE_TRACK_FROM_PLAYLIST = "removeTrackFromPlaylist";
    public static final String PLAY_PLAYLIST = "playPlaylist";
    public static final String PLAY_TRACK_OF_PLAYLIST = "playTrackOfPlaylist";
    public static final String GET_STATUS = "getStatus";
    public static final String GET_LIBRARY = "getLibrary";
    public static final String SEND_STRING = "sendString";
    public static final String SHUTDOWN_SERVER = "shutdownServer";
    public static final String SHUTDOWN_RASPI = "shutdownRaspi";
    public static final String REBOOT_RASPI = "rebootRaspi";
    public static final String REBOOT_SERVER = "rebootServer";

    public String action;

    private String name;
    private int playlistId;
    private int trackId;

    private String title;
    private String artist;
    private String fileName;

    private int position;
    private int length;
    private int repeatMode;

    private int red;
    private int green;
    private int blue;
    private boolean smooth;

    private boolean shuffle;
    private int volume;
    private int brightness;
    private int color;

    private String colorMode;
    private String visualisation;
    private String text;

    private static final int BUFFER_SIZE = 8192;
    private long fileLength = 0;

    private String[] args;

    public Command(String command) {
        this.action = command;
    }

    public Command() {
    }

    public Command setRGBColor(int red, int green, int blue) {
        action = SET_RGBCOLOR;
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public Command setColor(Color color) {
        action = SET_COLOR;
        this.color = color.getRGB();
        return this;
    }

    /**
     * Setzt den Farbmodus. Mögliche Werte: music, custom, colorCircle
     *
     * @param musicMode Der Farbmodus
     * @return Command
     */
    public Command setColorMode(String musicMode) {
        action = SET_COLOR_MODE;
        this.colorMode = musicMode;
        return this;
    }

    public Command setWhiteBrightness(int brightness, boolean smooth) {
        action = SET_WHITE_BRIGHTNESS;
        this.brightness = brightness;
        this.smooth = smooth;
        return this;
    }

    public Command setAnimationBrightness(int brightness, boolean smooth) {
        action = SET_ANIMATION_BRIGHTNESS;
        this.brightness = brightness;
        this.smooth = smooth;
        return this;
    }

    public Command sendString(String contents) {
        action = SEND_STRING;
        text = contents;
        return this;
    }

    public void send(ServerConnect connect) {
        connect.sendCommand(this);
    }
}
