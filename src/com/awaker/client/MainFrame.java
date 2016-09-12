package com.awaker.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class MainFrame {
    JPanel contentPane;

    private MainFrameController controller;
    JTextField adressBox;
    JButton adressBtn;
    private JLabel statusLabel;
    JButton openBrowserBtn;

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
        openBrowserBtn = new JButton("Steuerung öffnen");
        openBrowserBtn.addActionListener(controller);
        openBrowserBtn.setBorder(new EmptyBorder(10, 10,10,10));
        contentPane.add(openBrowserBtn, BorderLayout.CENTER);
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
