package com.awaker.client.connect;

public interface StatusChangedListener {

    /**
     * Wird ausgelöst, wenn der Serverstatus aktualisiert wurde
     *
     * @param newSong True, wenn ein neuer Song gespielt wird
     */
    void serverStatusChanged(boolean newSong);

    /**
     * Wird ausgelöst, wenn eine Datei nicht gefunden wurde, die abgespielt werden sollte.
     */
    void fileNotFound();

    void showError(String error);
}
