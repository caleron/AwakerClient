package com.awaker.client;

import com.awaker.client.connect.ServerConnect;
import com.awaker.client.connect.ServerStatus;
import com.awaker.client.connect.StatusChangedListener;
import com.awaker.client.connect.json.Answer;
import com.awaker.client.connect.json.Command;
import com.awaker.client.integration.Integrator;
import com.awaker.client.util.Prefs;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import com.melloware.jintellitype.JIntellitypeConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

class MainFrameController implements ActionListener, IntellitypeListener, StatusChangedListener {
    private MainFrame mainFrame;
    private ServerStatus serverStatus;
    private ServerConnect serverConnect;

    MainFrameController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    void init() {
        establishConnection();
        new Integrator(serverConnect).startWatch();

        mainFrame.adressBox.setText(Prefs.getServer());
        mainFrame.adressBox.setEnabled(false);

        registerHotkeys();
    }

    private void registerHotkeys() {
        JIntellitype.setLibraryLocation(System.getProperty("user.dir") + "\\JIntellitype64.dll");

        if (!JIntellitype.isJIntellitypeSupported()) {
            System.out.println("Global hotkeys not supported");
            return;
        }

        JIntellitype jIntellitype = JIntellitype.getInstance();
        jIntellitype.addIntellitypeListener(this);

        //Beim beenden aufräumen
        Runtime.getRuntime().addShutdownHook(new Thread(jIntellitype::cleanUp));
    }

    private void establishConnection() {
        if (serverConnect != null) {
            serverConnect.close();
        }
        setStatus("Verbinde...");
        serverConnect = new ServerConnect(this, URI.create("ws://" + Prefs.getServer()));
        serverStatus = new ServerStatus(this, serverConnect);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();

        if (source.equals(mainFrame.adressBtn)) {
            if (mainFrame.adressBox.isEnabled()) {
                String adress = mainFrame.adressBox.getText();

                if (adress.matches("((1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}((1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])):[0-6]?([0-9]){1,4}")) {
                    mainFrame.adressBox.setEnabled(false);
                    mainFrame.adressBtn.setText("Bearbeiten");

                    Prefs.setServer(adress);
                    establishConnection();
                } else {
                    JOptionPane.showMessageDialog(mainFrame.contentPane, "Text ist keine Adresse");
                }

            } else {
                mainFrame.adressBox.setEnabled(true);
                mainFrame.adressBtn.setText("Setzen");
            }
        } else if (source.equals(mainFrame.openBrowserBtn)) {
            URI uri = URI.create("http://" + Prefs.getServer());
            try {
                Desktop.getDesktop().browse(URI.create(uri.getScheme() + "://" + uri.getHost()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void serverStatusChanged(boolean newSong) {

    }

    @Override
    public void answerReceived(Answer answer) {
        if (serverStatus.newAnswer(answer)) {
            //neuer song
        }
    }

    public void showError(String error) {
        JOptionPane.showMessageDialog(mainFrame.contentPane, error, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void setStatus(String status) {
        mainFrame.setStatusText(status);
    }

    @Override
    public void onIntellitype(int command) {
        switch (command) {
            case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
                new Command(Command.TOGGLE_PLAY_PAUSE).send(serverConnect);
                break;
            case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                new Command(Command.PLAY_NEXT).send(serverConnect);
                break;
            case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                new Command(Command.PLAY_PREVIOUS).send(serverConnect);
                break;
            case JIntellitypeConstants.APPCOMMAND_VOLUME_UP:
                break;
            case JIntellitypeConstants.APPCOMMAND_VOLUME_DOWN:
                break;
        }
    }
}
