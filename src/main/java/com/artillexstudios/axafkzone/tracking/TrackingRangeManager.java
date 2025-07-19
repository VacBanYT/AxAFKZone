package com.artillexstudios.axafkzone.tracking;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple manager that limits entity update packets for players that should have
 * reduced tracking range. Packets about other players farther than one block
 * from the receiver are cancelled.
 */
public final class TrackingRangeManager {
    private static final Set<UUID> limited = ConcurrentHashMap.newKeySet();
    private static ProtocolManager manager;

    private TrackingRangeManager() {
    }

    public static void init(Plugin plugin) {
        manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                PacketType.Play.Server.REL_ENTITY_MOVE,
                PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
                PacketType.Play.Server.ENTITY_TELEPORT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player receiver = event.getPlayer();
                if (!limited.contains(receiver.getUniqueId())) return;

                int entityId = event.getPacket().getIntegers().read(0);
                Entity entity = manager.getEntityFromID(receiver.getWorld(), entityId);
                if (!(entity instanceof Player other)) return;
                if (other.equals(receiver)) return;

                if (other.getLocation().distanceSquared(receiver.getLocation()) > 1) {
                    event.setCancelled(true);
                }
            }
        });
    }

    public static void apply(Player player) {
        limited.add(player.getUniqueId());
    }

    public static void reset(Player player) {
        limited.remove(player.getUniqueId());
    }

    public static boolean isLimited(Player player) {
        return limited.contains(player.getUniqueId());
    }
}
