package com.artillexstudios.axafkzone.listeners;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingRangeListener extends PacketAdapter {
    private static final Set<Player> limited = ConcurrentHashMap.newKeySet();

    public static void init(AxAFKZone plugin) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new TrackingRangeListener(plugin));
    }

    public static void add(Player player) {
        limited.add(player);
    }

    public static void remove(Player player) {
        limited.remove(player);
    }

    private TrackingRangeListener(AxAFKZone plugin) {
        super(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                PacketType.Play.Server.SPAWN_ENTITY,
                PacketType.Play.Server.ENTITY_TELEPORT,
                PacketType.Play.Server.REL_ENTITY_MOVE,
                PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
                PacketType.Play.Server.ENTITY_METADATA,
                PacketType.Play.Server.ENTITY_HEAD_ROTATION);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player receiver = event.getPlayer();
        if (!limited.contains(receiver)) return;

        PacketContainer packet = event.getPacket();
        Entity entity = packet.getEntityModifier(receiver.getWorld()).readSafely(0);
        if (entity == null || !(entity instanceof Player) || entity.equals(receiver)) {
            return;
        }

        if (receiver.getLocation().distanceSquared(entity.getLocation()) > 1.0) {
            event.setCancelled(true);
        }
    }
}
