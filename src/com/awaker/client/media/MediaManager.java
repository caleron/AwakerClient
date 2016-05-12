package com.awaker.client.media;

import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;

public class MediaManager {

    public static TrackWrapper readFile(File file) {
        try {
            Mp3File mp3File = new Mp3File(file);

            String title = null, artist = null, album = null;
            int lengthInSeconds = (int) mp3File.getLengthInSeconds();

            if (mp3File.hasId3v2Tag()) {
                ID3v2 tag = mp3File.getId3v2Tag();
                title = tag.getTitle();
                artist = tag.getArtist();
                album = tag.getAlbum();
            } else if (mp3File.hasId3v1Tag()) {
                ID3v1 tag = mp3File.getId3v1Tag();
                title = tag.getTitle();
                artist = tag.getArtist();
                album = tag.getAlbum();
            } else {
                System.out.println("Track " + file.getPath() + " hat keine Tags");
            }

            if (title == null || title.length() == 0) {
                //etwa wenn keine Tags, dann den Dateinamen ohne Erweiterung als Titel nehmen
                title = file.getName().replace(".mp3", "");
            }

            if (artist == null)
                artist = "";

            if (album == null)
                album = "";

            return new TrackWrapper(title, artist, album, file.getPath(), lengthInSeconds);

        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            System.out.println(e);
        }
        return null;
    }
}
