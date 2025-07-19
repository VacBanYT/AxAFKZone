package com.artillexstudios.axafkzone.tracking;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class used to modify the tracking range of players when they are
 * inside an AFK zone. This uses Paper specific API when available and falls
 * back to reflection otherwise.
 */
public class TrackingRangeManager {
    private static final Map<Player, Integer> originalRanges = new ConcurrentHashMap<>();

    private static Method setRangeMethod;
    private static Method getRangeMethod;

    static {
        try {
            setRangeMethod = Player.class.getMethod("setEntityTrackingRange", int.class);
            getRangeMethod = Player.class.getMethod("getEntityTrackingRange");
        } catch (NoSuchMethodException ignored) {
            setRangeMethod = null;
            getRangeMethod = null;
        }
    }

    /**
     * Set the player's tracking range to the supplied value while remembering
     * the previous range so that it can be restored later.
     *
     * @param player player to modify
     * @param range  new tracking range
     */
    public static void applyAfkRange(Player player, int range) {
        if (setRangeMethod == null) return;
        if (!originalRanges.containsKey(player)) {
            try {
                int current = (int) getRangeMethod.invoke(player);
                originalRanges.put(player, current);
            } catch (Exception ex) {
                // ignore if we cannot fetch current range
            }
        }

        try {
            setRangeMethod.invoke(player, range);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Restore the player's tracking range if previously modified by
     * {@link #applyAfkRange(Player, int)}.
     *
     * @param player player to restore
     */
    public static void restoreRange(Player player) {
        Integer original = originalRanges.remove(player);
        if (original == null || setRangeMethod == null) return;
        try {
            setRangeMethod.invoke(player, original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
