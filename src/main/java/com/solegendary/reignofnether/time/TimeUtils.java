package com.solegendary.reignofnether.time;

import com.solegendary.reignofnether.survival.WaveDifficulty;

public class TimeUtils {

    public static final long DAWN = 500;
    public static final long DUSK = 12500;

    // Ensures a time value is between 0 and 24000
    public static long normaliseTime(long time) {
        return ((time % 24000) + 24000) % 24000;
    }

    public static String get12HourTimeStr(long time) {
        long hours = (time / 1000 + 6) % 24;
        String ampm = hours >= 12 ? "pm" : "am";
        hours = (hours % 12 == 0) ? 12 : hours % 12;
        long minutes = (time % 1000) * 60 / 1000;

        return String.format("%d:%02d%s", hours, minutes, ampm);
    }

    // Returns a string representing real time in min/sec until the given time
    public static String getTimeUntilStr(long currentTime, long targetTime) {
        if (currentTime > targetTime) currentTime -= 24000;
        long timeDiff = targetTime - currentTime;
        return formatTimeFromTicks(timeDiff);
    }

    // Returns a string representing real time in min/sec until the given time
    public static String getTimeUntilStrWithOffset(long currentTime, long targetTime, long offset) {
        if (currentTime > targetTime) currentTime -= 24000;
        long timeDiff = targetTime - currentTime + offset;
        return formatTimeFromTicks(timeDiff);
    }

    // standard vanilla length is 20mins for a full day/night cycle (24000)
    // 1min == 1200, but is applied twice per cycle (dawn and dusk), so effectively 1min == 600
    public static long getWaveSurvivalTimeModifier(WaveDifficulty difficulty) {
        return switch (difficulty) {
            default -> 0; // 20mins per day
            case EASY -> 3000; // 15mins per day
            case MEDIUM -> 4800; // 12mins per day
            case HARD -> 6600; // 9mins per day
            case EXTREME -> 8400; // 6mins per day
        };
    }

    // Returns a string representing real time in min/sec from ticks
    public static String getTimeStrFromTicks(long ticks) {
        return formatTimeFromTicks(ticks);
    }

    // Use instead of level.isDay() as it's stricter for undead burning checks
    public static boolean isDay(long time) {
        long normTime = normaliseTime(time);
        return normTime > DAWN && normTime <= DUSK;
    }

    // Helper to format ticks into min/sec string
    private static String formatTimeFromTicks(long ticks) {
        int sec = (int) (ticks / 20);
        int min = sec / 60;
        sec %= 60;

        return min == 0 ? sec + "s" : min + "m" + sec + "s";
    }
}
