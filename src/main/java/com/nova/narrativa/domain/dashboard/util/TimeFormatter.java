package com.nova.narrativa.domain.dashboard.util;

public class TimeFormatter {
    public static String formatSeconds(Double seconds) {
        if (seconds == null) return "0분 0초";
        long totalSeconds = seconds.longValue();
        long minutes = totalSeconds / 60;
        long remainingSeconds = totalSeconds % 60;
        return String.format("%d분 %d초", minutes, remainingSeconds);
    }
}
