package com.awaker.client.connect;

import com.awaker.client.connect.json.Answer;

public interface StatusChangedListener {

    /**
     * Wird ausgelöst, wenn der Serverstatus aktualisiert wurde
     *
     * @param newSong True, wenn ein neuer Song gespielt wird
     */
    void serverStatusChanged(boolean newSong);

    void answerReceived(Answer answer);

    void showError(String error);

    void setStatus(String status);
}
