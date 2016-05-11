package com.awaker.client.util;

import com.awaker.client.MainFrame;

import java.util.prefs.Preferences;

public class Prefs {
    private static final Preferences preferences;
    public static final String PREF_SERVER = "server";

    static {
        //Einstellungen laden
        preferences = Preferences.userNodeForPackage(MainFrame.class);
    }

    public static String getServer() {
        return preferences.get(PREF_SERVER, "192.168.1.2:4732");
    }

    public static void setServer(String server) {
        preferences.put(PREF_SERVER, server);
    }
}
