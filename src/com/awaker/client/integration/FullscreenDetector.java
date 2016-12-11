package com.awaker.client.integration;

import com.awaker.client.connect.ServerConnect;
import com.awaker.client.connect.json.Command;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.*;
import java.util.Objects;

public class FullscreenDetector {

    private Thread thread;
    private ServerConnect connect;

    private boolean dimmed = false;
    private String[] dimFullScreenWindows = new String[]{"media player", "mozilla firefox"};

    public FullscreenDetector(ServerConnect connect) {
        this.connect = connect;
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this::watch);
            thread.start();
        }
    }

    private void watch() {
        String currentFullscreenWindow = "";

        while (!thread.isInterrupted()) {
            String title = getFullscreenWindowTitle();

            if (currentFullscreenWindow.length() > 0 && !Objects.equals(currentFullscreenWindow, title)) {
                //aktuelles Fenster hat Fokus verloren
                currentFullscreenWindow = "";
                if (dimmed) {
                    new Command().setWhiteBrightness(70, true).send(connect);
                    dimmed = false;
                }
            }
            if (title.length() > 0 && !Objects.equals(currentFullscreenWindow, title)) {
                //neues Fenster ist fokussiert und im Vollbildmodus
                currentFullscreenWindow = title;

                for (String name : dimFullScreenWindows) {
                    if (title.toLowerCase().contains(name)) {
                        new Command().setWhiteBrightness(10, true).send(connect);
                        dimmed = true;
                        break;
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        HWND GetForegroundWindow();  // add this

        HWND GetDesktopWindow();

        HWND GetShellWindow();

        int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);

        boolean GetWindowRect(HWND hWnd, WinDef.RECT rect);
    }

    private static String getFullscreenWindowTitle() {
        try {
            HWND win = User32.INSTANCE.GetForegroundWindow();
            HWND desktopWin = User32.INSTANCE.GetDesktopWindow();
            HWND shellWin = User32.INSTANCE.GetShellWindow();
            WinDef.RECT winBounds = new WinDef.RECT();

            if (win != null && !win.equals(desktopWin) && !win.equals(shellWin)) {
                if (User32.INSTANCE.GetWindowRect(win, winBounds)) {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                    if (winBounds.top <= 0 && winBounds.left <= 0 && winBounds.right >= screenSize.width && winBounds.bottom >= screenSize.height) {
                        byte[] windowText = new byte[256];
                        User32.INSTANCE.GetWindowTextA(win, windowText, 256);

                        return Native.toString(windowText);
                    }
                }
            }

        } catch (Exception ignored) {
        }
        return "";
    }
}
