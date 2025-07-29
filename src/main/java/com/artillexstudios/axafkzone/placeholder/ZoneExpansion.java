package com.artillexstudios.axafkzone.placeholder;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.utils.TimeUtils;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ZoneExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "axafkzone";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ArtillexStudios";
    }

    @Override
    public @NotNull String getVersion() {
        return AxAFKZone.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        int idx = params.lastIndexOf('_');
        if (idx == -1) return "";
        String zoneName = params.substring(0, idx);
        String key = params.substring(idx + 1);
        Zone zone = Zones.getZoneByName(zoneName);
        if (zone == null) return "";
        long time = zone.timeUntilNext(player);
        long total = zone.getSettings().getInt("reward-time-seconds") * 1000L;
        return switch (key.toLowerCase()) {
            case "time" -> TimeUtils.fancyTime(time, total);
            case "percent" -> TimeUtils.fancyTimePercentage(time, total);
            default -> "";
        };
    }
}
