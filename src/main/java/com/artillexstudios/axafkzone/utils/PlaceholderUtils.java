package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderUtils {
    private static final boolean HAS_PAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    public static String applyPlaceholders(Player player, String text) {
        if (text == null) return null;
        for (Zone zone : Zones.getZones().values()) {
            long time = zone.timeUntilNext(player);
            long total = zone.getSettings().getInt("reward-time-seconds") * 1000L;
            text = text.replace("%" + zone.getName() + "-time%", TimeUtils.fancyTime(time, total));
            text = text.replace("%" + zone.getName() + "-time-percent%", TimeUtils.fancyTimePercentage(time, total));
        }
        if (HAS_PAPI) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}
