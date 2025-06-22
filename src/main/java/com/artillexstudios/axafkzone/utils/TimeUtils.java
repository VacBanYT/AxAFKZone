package com.artillexstudios.axafkzone.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.LANG;

public class TimeUtils {
    public static @NotNull String fancyTime(long time, long totalTime) {
        if (time < 0) return "---";

        final Duration remainingTime = Duration.ofMillis(time);
        long total = remainingTime.getSeconds();
        long days = total / 86400;
        long hours = (total % 86400) / 3600;
        long minutes = (total % 3600) / 60;
        long seconds = total % 60;

        if (CONFIG.getInt("timer-format", 1) == 1) {
            if (days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            if (hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            return String.format("%02d:%02d", minutes, seconds);
        } else if (CONFIG.getInt("timer-format", 1) == 2) {
            if (days > 0) return days + LANG.getString("time.day", "d");
            if (hours > 0) return hours + LANG.getString("time.hour", "h");
            if (minutes > 0) return minutes + LANG.getString("time.minute", "m");
            return seconds + LANG.getString("time.second", "s");
        } else if (CONFIG.getInt("timer-format", 1) == 3) {
            if (days > 0)
                return String.format("%02d" + LANG.getString("time.day", "d") + " %02d" + LANG.getString("time.hour", "h") + " %02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), days, hours, minutes, seconds);
            if (hours > 0)
                return String.format("%02d" + LANG.getString("time.hour", "h") + " %02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), hours, minutes, seconds);
            return String.format("%02d" + LANG.getString("time.minute", "m") + " %02d" + LANG.getString("time.second", "s"), minutes, seconds);
        } else if (CONFIG.getInt("timer-format", 1) == 4) {
            return fancyTimePercentage(time, totalTime);
        }

        return "---";
    }

    public static @NotNull String fancyTimePercentage(long time, long totalTime) {
        if (totalTime > 0) {
            double percentage = ((double) (totalTime - time) / totalTime) * 100;
            return String.format("%.2f%%", percentage);
        }
        return "---";
    }
}
