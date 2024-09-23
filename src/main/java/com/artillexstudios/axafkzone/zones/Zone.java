package com.artillexstudios.axafkzone.zones;

import com.artillexstudios.axafkzone.reward.Reward;
import com.artillexstudios.axafkzone.selection.Region;
import com.artillexstudios.axafkzone.utils.RandomUtils;
import com.artillexstudios.axafkzone.utils.TimeUtils;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.ActionBar;
import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.Title;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.artillexstudios.axafkzone.AxAFKZone.CONFIG;
import static com.artillexstudios.axafkzone.AxAFKZone.MESSAGEUTILS;

public class Zone {
    private final String name;
    private final Config settings;
    private Region region;
    private int ticks = 0;
    private final HashMap<Player, Integer> zonePlayers = new HashMap<>();
    private final MessageUtils msg;
    private int rewardSeconds;
    private int rollAmount;
    private final LinkedList<Reward> rewards = new LinkedList<>();
    private final Cooldown<Player> cooldown = new Cooldown<>();

    public Zone(String name, Config settings) {
        this.name = name;
        this.settings = settings;
        this.msg = new MessageUtils(settings.getBackingDocument(), "prefix", CONFIG.getBackingDocument());
        this.region = new Region(Serializers.LOCATION.deserialize(settings.getString("zone.location1")), Serializers.LOCATION.deserialize(settings.getString("zone.location2")), this);
        reload();
    }

    public void tick() {
        boolean runChecks = ++ticks % 20 == 0;

        final HashSet<Player> players = region.getPlayersInZone();
        for (Player player : zonePlayers.keySet()) {
            if (player.isDead() || !player.isOnline()) {
                Scheduler.get().run(t -> zonePlayers.remove(player));
                continue;
            }

            // player left
            if (!players.contains(player)) {
                msg.sendLang(player, "messages.left", Map.of("%time%", TimeUtils.fancyTime(zonePlayers.get(player) * 1_000L)));
                Scheduler.get().run(t -> zonePlayers.remove(player));
                continue;
            }

            if (runChecks) {
                int newTime = zonePlayers.get(player) + 1;
                zonePlayers.put(player, newTime);

                if (newTime != 0 && newTime % rewardSeconds == 0) {
                    final List<Reward> rewardList = giveRewards(player);

                    if (!settings.getStringList("messages.reward").isEmpty()) {
                        final String prefix = CONFIG.getString("prefix");
                        boolean first = true;
                        for (String string : settings.getStringList("messages.reward")) {
                            if (first) {
                                string = prefix + string;
                                first = false;
                            }

                            if (string.contains("%reward%")) {
                                for (Reward reward : rewardList) {
                                    player.sendMessage(StringUtils.formatToString(string, Map.of("%reward%", reward.getDisplay(), "%time%", TimeUtils.fancyTime(newTime * 1_000L))));
                                }
                                continue;
                            }
                            player.sendMessage(StringUtils.formatToString(string, Map.of("%time%", TimeUtils.fancyTime(newTime * 1_000L))));
                        }
                    }

                    if (CONFIG.getBoolean("reset-after-reward", false)) zonePlayers.put(player, 0);
                }
            }
            players.remove(player);

            String zoneTitle = settings.getString("in-zone.title", null);
            String zoneSubTitle = settings.getString("in-zone.subtitle", null);
            if (zoneTitle != null && !zoneTitle.isBlank() || zoneSubTitle != null && !zoneSubTitle.isBlank()) {
                Title title = NMSHandlers.getNmsHandler()
                        .newTitle(
                                zoneTitle == null ? Component.empty() : StringUtils.format(zoneTitle.replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))),
                                zoneSubTitle == null ? Component.empty() : StringUtils.format(zoneSubTitle.replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))),
                                0,
                                10,
                                0
                        );
                title.send(player);
            }

            String zoneActionbar = settings.getString("in-zone.actionbar", null);
            if (zoneActionbar != null && !zoneActionbar.isBlank()) {
                ActionBar actionBar = NMSHandlers.getNmsHandler()
                        .newActionBar(StringUtils.format(zoneActionbar.replace("%time%", TimeUtils.fancyTime(timeUntilNext(player)))));
                actionBar.send(player);
            }
        }

        int ipLimit = CONFIG.getInt("zone-per-ip-limit", -1);
        // player entered
        for (Player player : players) {
            if (cooldown.hasCooldown(player)) continue;
            if (ipLimit != -1 && zonePlayers.keySet().stream().filter(p1 -> p1.getAddress().getAddress() == player.getAddress().getAddress()).count() >= ipLimit) {
                MESSAGEUTILS.sendLang(player, "zone.ip-limit");
                cooldown.addCooldown(player, 3_000L);
                continue;
            }
            msg.sendLang(player, "messages.entered", Map.of("%time%", TimeUtils.fancyTime(rewardSeconds * 1_000L)));
            zonePlayers.put(player, 0);
        }
    }

    public long timeUntilNext(Player player) {
        Integer time = zonePlayers.get(player);
        if (time == null) return -1;
        return rewardSeconds * 1_000L - (time % rewardSeconds) * 1_000L;
    }

    public List<Reward> giveRewards(Player player) {
        final List<Reward> rewardList = new ArrayList<>();
        if (rewards.isEmpty()) return rewardList;
        final HashMap<Reward, Double> chances = new HashMap<>();
        for (Reward reward : rewards) {
            chances.put(reward, reward.getChance());
        }

        for (int i = 0; i < rollAmount; i++) {
            Reward sel = RandomUtils.randomValue(chances);
            rewardList.add(sel);
            sel.run(player);
        }

        return rewardList;
    }

    public boolean reload() {
        if (!settings.reload()) return false;

        this.rewardSeconds = settings.getInt("reward-time-seconds", 180);
        this.rollAmount = settings.getInt("roll-amount", 1);

        rewards.clear();
        for (Map<Object, Object> map : settings.getMapList("rewards")) {
            final Reward reward = new Reward(map);
            rewards.add(reward);
        }

        return true;
    }

    public void setRegion(Region region) {
        this.region = region;
        settings.set("zone.location1", Serializers.LOCATION.serialize(region.getCorner1()));
        settings.set("zone.location2", Serializers.LOCATION.serialize(region.getCorner2()));
        settings.save();
    }

    public String getName() {
        return name;
    }

    public Config getSettings() {
        return settings;
    }

    public Region getRegion() {
        return region;
    }

    public int getTicks() {
        return ticks;
    }

    public void delete() {
    }
}
