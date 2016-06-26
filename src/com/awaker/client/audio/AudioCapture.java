package com.awaker.client.audio;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioCapture {

    private boolean interrupt = false;

    public void start() {
        interrupt = false;
        new Thread(this::capture).start();
    }

    private void capture() {

        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        TargetDataLine line;
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);

            line.start();

            int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
            byte byteBuffer[] = new byte[bufferSize];

            short shortBuffer[] = new short[bufferSize / 2];
            short shortBuffer1[] = new short[bufferSize / 2];

            while (!interrupt) {
                int count = line.read(byteBuffer, 0, byteBuffer.length);
                if (count > 0) {
                    for (int i = 0; i < shortBuffer.length; i++) {
                        //shortBuffer[i] = (short) ((byteBuffer[2 * i] & 0xff) | (byteBuffer[2 * i + 1] << 8));
                        shortBuffer[i] = (short) ((byteBuffer[2 * i] << 8) | (byteBuffer[2 * i + 1] & 0xff));

                        shortBuffer1[i] = ByteBuffer.wrap(byteBuffer).order(ByteOrder.LITTLE_ENDIAN).getShort(i);
                    }
                    System.out.println(count);
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        interrupt = true;
    }
}
