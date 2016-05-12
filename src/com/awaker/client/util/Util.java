package com.awaker.client.util;

public class Util {

    public static String getTimeSpanString(Integer seconds) {
        if (seconds < 60) {
            return "0:" + get2DigitString(seconds);
        } else {
            Integer minutes = seconds / 60;

            seconds -= minutes * 60;
            return minutes.toString() + ":" + get2DigitString(seconds);
        }
    }

    private static String get2DigitString(int number) {
        if (number >= 10) {
            return String.valueOf(number);
        } else {
            return "0" + String.valueOf(number);
        }
    }
}
