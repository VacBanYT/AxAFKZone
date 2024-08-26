package com.artillexstudios.axafkzone.zones;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Zones {
    private static final HashMap<String, Zone> zones = new HashMap<>();

    @NotNull
    public static HashMap<String, Zone> getZones() {
        return zones;
    }

    public static void addZone(@NotNull Zone zone) {
        zones.put(zone.getName(), zone);
    }

    @Nullable
    public static Zone getZoneByName(@NotNull String name) {
        if (!zones.containsKey(name)) return null;
        return zones.get(name);
    }

    public static void removeZone(@NotNull Zone zone) {
        zone.delete();
        zones.remove(zone.getName());
    }
}
