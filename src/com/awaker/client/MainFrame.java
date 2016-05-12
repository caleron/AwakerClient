package com.awaker.client;

import com.awaker.client.custom.PlayProgressBar;
import com.awaker.client.util.GbcBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class MainFrame {
    JPanel contentPane;

    private MainFrameController controller;
    JTextField adressBox;
    JButton adressBtn;
    JButton prevBtn;
    JButton playBtn;
    JButton nextBtn;
    PlayProgressBar progressBar;
    JLabel titleLabel;
    private JLabel statusLabel;
    JButton shutdownBtn;
    JSlider volumeSlider;

    private MainFrame() {
        controller = new MainFrameController(this);

        //Windows Look-and-Feel setzen
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        contentPane = new JPanel(new BorderLayout());

        initTopPanel();
        initCenterPanel();
        initStatusBar();
        initSideBar();

        controller.init();
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        JLabel adressLabel = new JLabel("Adresse:");
        topPanel.add(adressLabel);

        adressBox = new JTextField(50);
        topPanel.add(adressBox);

        adressBtn = new JButton("Bearbeiten");
        adressBtn.addActionListener(controller);
        topPanel.add(adressBtn);

        contentPane.add(topPanel, BorderLayout.PAGE_START);
    }

    private void initCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        prevBtn = new JButton("<<");
        prevBtn.addActionListener(controller);
        buttonPanel.add(prevBtn);

        playBtn = new JButton("Play");
        playBtn.addActionListener(controller);
        buttonPanel.add(playBtn);

        nextBtn = new JButton(">>");
        nextBtn.addActionListener(controller);
        buttonPanel.add(nextBtn);

        centerPanel.add(buttonPanel, GbcBuilder.build(0, 0));

        JPanel bottomPanel = new JPanel(new GridBagLayout());

        titleLabel = new JLabel("Titel - Künstler", SwingConstants.CENTER);
        bottomPanel.add(titleLabel, GbcBuilder.build(0, 0).fillBoth().center());

        progressBar = new PlayProgressBar();
        progressBar.setSeekListener(controller);
        progressBar.setStringPainted(true);
        bottomPanel.add(progressBar, GbcBuilder.build(0, 1, 1, 1, 1, 0).fillBoth());

        centerPanel.add(bottomPanel, GbcBuilder.build(0, 1, 1, 1, 1, 0).fillBoth());

        shutdownBtn = new JButton("Server killen");
        shutdownBtn.addActionListener(controller);
        centerPanel.add(shutdownBtn, GbcBuilder.build(0, 2));

        contentPane.add(centerPanel, BorderLayout.CENTER);
    }

    private void initSideBar() {
        JPanel sideBar = new JPanel();
        sideBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        volumeSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, 100);
        volumeSlider.setMajorTickSpacing(50);
        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintTrack(true);
        volumeSlider.setFocusable(false);
        volumeSlider.addMouseWheelListener(controller);
        volumeSlider.addChangeListener(controller);

        sideBar.add(volumeSlider);
        contentPane.add(sideBar, BorderLayout.LINE_END);
    }

    private void initStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        statusBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.gray));

        statusLabel = new JLabel("Status", SwingConstants.LEFT);
        statusBar.add(statusLabel);

        contentPane.add(statusBar, BorderLayout.PAGE_END);
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();

        JFrame window = new JFrame("AwakerClient");
        window.setContentPane(frame.contentPane);

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public void setStatusText(String status) {
        statusLabel.setText(status);
    }
}
