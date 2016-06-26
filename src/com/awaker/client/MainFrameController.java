package com.awaker.client;

import com.awaker.client.audio.AudioCapture;
import com.awaker.client.connect.ServerConnect;
import com.awaker.client.connect.ServerStatus;
import com.awaker.client.connect.StatusChangedListener;
import com.awaker.client.connect.UploadStatusListener;
import com.awaker.client.custom.SeekListener;
import com.awaker.client.util.Prefs;
import com.awaker.client.util.Util;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;
import com.melloware.jintellitype.JIntellitypeConstants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.List;

class MainFrameController implements ActionListener, StatusChangedListener, SeekListener, IntellitypeListener,
        ChangeListener, MouseWheelListener, DropTargetListener, UploadStatusListener {
    private MainFrame mainFrame;
    private ServerStatus serverStatus;
    private ServerConnect serverConnect;

    private Timer progressTimer;
    private Timer volumeChangeTimer;

    MainFrameController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    void init() {
        serverConnect = new ServerConnect(this);

        serverStatus = new ServerStatus(this, serverConnect);

        mainFrame.adressBox.setText(Prefs.getServer());
        mainFrame.adressBox.setEnabled(false);

        progressTimer = new Timer(1000, e -> updateProgress());
        volumeChangeTimer = new Timer(100, e -> serverStatus.setVolume(mainFrame.volumeSlider.getValue()));
        volumeChangeTimer.setRepeats(false);

        registerHotkeys();
        serverStatus.requestNewStatus();

        //AudioCapture ac = new AudioCapture();
        //ac.start();
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
        } else if (source.equals(mainFrame.shutdownBtn)) {
            if (JOptionPane.showConfirmDialog(mainFrame.contentPane, "Server wirklich killen?", "Bestätigung",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                serverStatus.shutdownServer();
            }
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

        mainFrame.volumeSlider.setValue(serverStatus.getVolume());
    }

    private void updateProgress() {
        JProgressBar progressBar = mainFrame.progressBar;

        if (progressBar.getValue() >= progressBar.getMaximum()) {
            serverStatus.requestNewStatus();
            progressTimer.stop();
        } else {
            progressBar.setValue(progressBar.getValue() + 1);
            progressBar.setString(Util.getTimeSpanString(progressBar.getValue()) + " - "
                    + Util.getTimeSpanString(serverStatus.getTrackLength()));
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
            case JIntellitypeConstants.APPCOMMAND_VOLUME_UP:
                serverStatus.increaseVolume();
                break;
            case JIntellitypeConstants.APPCOMMAND_VOLUME_DOWN:
                serverStatus.decreaseVolume();
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        volumeChangeTimer.restart();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        JSlider volumeSlider = mainFrame.volumeSlider;
        int notches = e.getWheelRotation();

        if (notches < 0) {
            if (volumeSlider.getValue() == 100) {
                return;
            }

            volumeSlider.setValue(Math.min(100, volumeSlider.getValue() + 2));
            serverStatus.increaseVolume();
        } else if (notches > 0) {
            if (volumeSlider.getValue() == 0) {
                return;
            }

            volumeSlider.setValue(Math.max(0, volumeSlider.getValue() - 2));
            serverStatus.decreaseVolume();
        }
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        event.acceptDrop(DnDConstants.ACTION_COPY);

        // Get the transfer which can provide the dropped item data
        Transferable transferable = event.getTransferable();

        // Get the data formats of the dropped item
        DataFlavor[] flavors = transferable.getTransferDataFlavors();

        // Loop through the flavors
        for (DataFlavor flavor : flavors) {
            try {
                // If the drop items are files
                if (flavor.isFlavorJavaFileListType()) {
                    // Get all of the dropped files
                    List<File> files = (List<File>) transferable.getTransferData(flavor);

                    boolean first = true;

                    // Loop them through
                    for (File file : files) {
                        if (first) {
                            first = false;
                            serverStatus.playFile(file.getPath(), this);
                        }
                        serverStatus.uploadFile(file.getPath(), this);
                        System.out.println(file);
                    }
                }
            } catch (Exception e) {
                // Print out the error stack
                e.printStackTrace();
            }
        }
        // Inform that the drop is complete
        event.dropComplete(true);
    }

    @Override
    public void updateUploadStatus(String text, int progressPercent) {
        mainFrame.uploadProgressBar.setValue(progressPercent);
    }

    /*
    Nicht benötigte Interface-Methoden ab hier
     */

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }
}
