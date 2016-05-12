package com.awaker.client.media;

public class TrackWrapper {
    private static final String TABLE_NAME = "music";

    public static final String ID = "id";
    public static final String ALBUM = "album";
    public static final String ARTIST = "artist";
    public static final String TITLE = "title";
    public static final String FILE_PATH = "file";
    public static final String TRACK_LENGTH = "length";

    public final String title;
    public final String artist;
    public String album;
    public String filePath;
    public int trackLength; //in Sekunden

    public TrackWrapper(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    TrackWrapper(String title, String artist, String album, String filePath, int trackLength) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.filePath = filePath;
        this.trackLength = trackLength;
    }
}
