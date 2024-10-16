package com.artillexstudios.axafkzone.schedulers;

import com.artillexstudios.axafkzone.listeners.WandListeners;
import com.artillexstudios.axafkzone.zones.Zone;
import com.artillexstudios.axafkzone.zones.Zones;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickZones {
    private static ScheduledExecutorService service = null;

    public static void start() {
        if (service != null) service.shutdown();

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                List<Zone> zonesSnapshot = new ArrayList<>(Zones.getZones().values());
                for (Zone zone : zonesSnapshot) {
                    zone.tick();
                }

                WandListeners.getSelections().forEach((player, selection) -> {
                    selection.show(player);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        if (service == null) return;
        service.shutdown();
    }
}
