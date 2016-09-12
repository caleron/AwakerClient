package com.awaker.client.connect.json;

import java.util.List;

@SuppressWarnings("unused")
public class Answer {
    public static final String TYPE_FILE_STATUS = "file_status";
    public static final String TYPE_STATUS = "status";
    public static final String TYPE_LIBRARY = "library";

    public String type;

    public String colorMode;
    public int currentColor;
    public int whiteBrightness;
    public int animationBrightness;

    public String currentTitle;
    public String currentAlbum;
    public String currentArtist;
    public int currentTrackId;
    public int repeatMode;
    public int volume;
    public int trackLength;
    public int playPosition;
    public boolean playing;
    public boolean shuffle;

    public List<Track> tracks;
    public List<Playlist> playLists;

    public boolean fileNotFound;

    public Answer() {
    }
}
