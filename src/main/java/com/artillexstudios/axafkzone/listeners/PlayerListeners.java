package com.artillexstudios.axafkzone.listeners;

import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        handleLeave(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        handleLeave(event.getPlayer());
    }

    private void handleLeave(Player player) {
        for (Zone zone : Zones.getZones().values()) {
            if (zone.hasPlayer(player)) {
                zone.forceLeave(player);
                break;
            }
        }
    }
}
