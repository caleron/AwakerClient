package com.awaker.client.connect;

import com.awaker.client.connect.json.Answer;

public interface StatusChangedListener {

    void answerReceived(Answer answer);

    void showError(String error);

    void setStatus(String status);

    void connectionStatusChanged(boolean connected);
}
