package com.awaker.client.connect;

public interface UploadStatusListener {
    /**
     * Aktualisiert den Upload-Status
     *
     * @param text            Statustext
     * @param progressPercent Prozentualer Forschritt, über 100 wenn fertiggestellt.
     */
    void updateUploadStatus(String text, int progressPercent);
}
