package com.artillexstudios.axafkzone.listeners;

import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;
import com.artillexstudios.axapi.scheduler.Scheduler;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

public class WorldListeners implements Listener {

    @EventHandler
    public void onLoad(@NotNull WorldLoadEvent event) {
        Scheduler.get().run(scheduledTask -> {
            for (Zone zone : Zones.getZones().values()) {
                if (zone.getRegion().getWorld() != null) continue;
                zone.reload();
            }
        });
    }

    @EventHandler
    public void onUnload(@NotNull WorldUnloadEvent event) {
        for (Zone zone : Zones.getZones().values()) {
            World world = zone.getRegion().getWorld();
            if (world != null && world.equals(event.getWorld())) {
                zone.getRegion().setWorld(null);
            }
        }
    }
}
