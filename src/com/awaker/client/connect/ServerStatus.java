package com.awaker.client.connect;

import com.awaker.client.connect.json.Answer;
import com.awaker.client.connect.json.Command;

import java.util.Objects;

@SuppressWarnings("unused")
public class ServerStatus {
    private final ServerConnect serverConnect;
    private final StatusChangedListener listener;

    private Answer lastAnswer;

    public ServerStatus(StatusChangedListener listener, ServerConnect serverConnect) {
        this.serverConnect = serverConnect;
        this.listener = listener;
    }

    /**
     * Fordert einen neuen Serverstatus an.
     */
    public void requestNewStatus() {
        new Command(Command.GET_STATUS).send(serverConnect);
    }

    /**
     * Wendet die neuen Statusdaten an
     *
     * @return true, wenn sich der Titel geändert hat.
     */
    public boolean newAnswer(Answer answer) {
        if (answer == null)
            return false;

        if (Objects.equals(answer.type, Answer.TYPE_FILE_STATUS)) {
            listener.showError("file status received");
            return false;
        }

        boolean ret;

        //System.out.println("new play position: " + playPosition);
        if (lastAnswer == null) {
            ret = true;
        } else {
            if (Objects.equals(lastAnswer.currentArtist, "") && Objects.equals(answer.currentArtist, "")) {
                //Falls der Dateiname als Titel herhalten musste, etwa wenn keine Tags vorhanden sind
                ret = Objects.equals(lastAnswer.currentTitle, answer.currentTitle);
            } else {
                ret = !(Objects.equals(answer.currentArtist, lastAnswer.currentArtist) && Objects.equals(answer.currentTitle, lastAnswer.currentTitle));
            }
        }
        lastAnswer = answer;
        return ret;
    }
}
