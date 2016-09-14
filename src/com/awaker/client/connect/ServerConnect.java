package com.awaker.client.connect;

import com.awaker.client.connect.json.Answer;
import com.awaker.client.connect.json.Command;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class ServerConnect extends WebSocketClient {
    private final StatusChangedListener listener;
    private Gson gson = new Gson();

    public ServerConnect(StatusChangedListener listener, URI serverURI) {
        super(serverURI);
        this.listener = listener;
        connect();
    }

    public void sendCommand(Command command) {
        String text = gson.toJson(command);
        send(text);
        System.out.println("Sending command " + command.action);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        listener.setStatus("Verbunden");
        listener.connectionStatusChanged(true);
        new Command(Command.GET_STATUS).send(this);
    }

    @Override
    public void onMessage(String message) {
        Answer answer = gson.fromJson(message, Answer.class);
        listener.answerReceived(answer);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        listener.setStatus("Verbindung getrennt: " + reason);
        listener.connectionStatusChanged(false);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        listener.showError(ex.getMessage());
    }
}
