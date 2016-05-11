package com.awaker.client;

import com.awaker.client.custom.PlayProgressBar;
import com.awaker.client.util.GbcBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    private MainFrame() {
        controller = new MainFrameController(this);

        //Windows Look-and-Feel setzen
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        initTopPanel();
        initCenterPanel();
        initBottomPanel();

        controller.init();
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        prevBtn = new JButton("<<");
        prevBtn.addActionListener(controller);
        centerPanel.add(prevBtn);

        playBtn = new JButton("Play");
        playBtn.addActionListener(controller);
        centerPanel.add(playBtn);

        nextBtn = new JButton(">>");
        nextBtn.addActionListener(controller);
        centerPanel.add(nextBtn);

        contentPane.add(centerPanel, BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());

        titleLabel = new JLabel("Titel - Künstler", SwingConstants.CENTER);
        bottomPanel.add(titleLabel, GbcBuilder.build(0, 0).fillBoth().center());

        progressBar = new PlayProgressBar();
        progressBar.setSeekListener(controller);
        bottomPanel.add(progressBar, GbcBuilder.build(0, 1, 1, 1, 1, 0).fillBoth());

        contentPane.add(bottomPanel, BorderLayout.PAGE_END);
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
}
