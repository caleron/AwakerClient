package com.awaker.client;

import com.awaker.client.connect.ServerConnect;
import com.awaker.client.connect.ServerStatus;
import com.awaker.client.connect.StatusChangedListener;
import com.awaker.client.custom.SeekListener;
import com.awaker.client.util.Prefs;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MainFrameController implements ActionListener, StatusChangedListener, SeekListener, IntellitypeListener {
    private MainFrame mainFrame;
    private ServerStatus serverStatus;
    private ServerConnect serverConnect;

    private Timer progressTimer;

    MainFrameController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    void init() {
        serverConnect = new ServerConnect(this);

        serverStatus = new ServerStatus(this, serverConnect);


        mainFrame.adressBox.setText(Prefs.getServer());
        mainFrame.adressBox.setEnabled(false);

        progressTimer = new Timer(1000, e -> updateProgress());

        registerHotkeys();
        serverStatus.requestNewStatus();
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
                    serverConnect.disconnect();
                    ServerConnect.refreshPrefs();
                } else {
                    JOptionPane.showMessageDialog(mainFrame.contentPane, "Text ist keine Adresse");
                }

            } else {
                mainFrame.adressBox.setEnabled(true);
                mainFrame.adressBtn.setText("Setzen");
            }
        } else if (source.equals(mainFrame.nextBtn)) {
            serverStatus.playNext();
        } else if (source.equals(mainFrame.playBtn)) {
            serverStatus.togglePlaying();
        } else if (source.equals(mainFrame.prevBtn)) {
            serverStatus.playPrevious();
        }
    }

    @Override
    public void serverStatusChanged(boolean newSong) {
        String text = serverStatus.getCurrentTitle() + " - " + serverStatus.getCurrentArtist();
        mainFrame.titleLabel.setText(text);

        JProgressBar progressBar = mainFrame.progressBar;
        if (newSong) {
            if (progressBar.getMaximum() != serverStatus.getTrackLength()) {

                if (progressBar.getValue() > serverStatus.getTrackLength()) {
                    progressBar.setValue(0);
                }

                progressBar.setMaximum(serverStatus.getTrackLength());
            }
        }

        progressBar.setValue(serverStatus.getPlayPosition());

        if (serverStatus.isPlaying()) {
            mainFrame.playBtn.setText("Pause");
            progressTimer.start();
        } else {
            mainFrame.playBtn.setText("Play");
            progressTimer.stop();
        }
    }

    private void updateProgress() {
        JProgressBar progressBar = mainFrame.progressBar;

        if (progressBar.getValue() >= progressBar.getMaximum()) {
            serverStatus.requestNewStatus();
            progressTimer.stop();
        } else {
            progressBar.setValue(progressBar.getValue() + 1);
        }
    }

    @Override
    public void fileNotFound() {
        System.out.println("file not found");
    }

    @Override
    public void showError(String error) {
        JOptionPane.showMessageDialog(mainFrame.contentPane, error, "Fehler", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void progressChanged(int progress) {
        serverStatus.playFromPosition(progress);
    }

    @Override
    public void onIntellitype(int command) {
        switch (command) {
            case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
                serverStatus.togglePlaying();
                break;
            case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                serverStatus.playNext();
                break;
            case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                serverStatus.playPrevious();
                break;
        }
    }
}
