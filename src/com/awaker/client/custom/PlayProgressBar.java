package com.awaker.client.custom;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PlayProgressBar extends JProgressBar implements MouseListener {
    private static final long serialVersionUID = -4291357474794751270L;
    private SeekListener listener;

    public PlayProgressBar() {
        addMouseListener(this);
    }

    public void setSeekListener(SeekListener listener) {
        this.listener = listener;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int newProgress = (int) (((e.getX() * 1.0) / getWidth()) * getMaximum());

        listener.progressChanged(newProgress);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
