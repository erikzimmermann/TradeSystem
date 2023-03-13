package de.codingair.tradesystem.spigot.extras.external.worldguard;

import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeRequestEvent;
import de.codingair.tradesystem.spigot.events.TradeRequestPreResponseEvent;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

public class WorldGuardDependency implements PluginDependency, Listener {
    private WorldGuardAdapter adapter;
    private Set<String> regions;
    private RegionMode mode;

    @Override
    public void onEnable() {
        prepareAdapter();
        if (!loadRegions()) HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPreResponse(TradeRequestPreResponseEvent e) {
        if (e.isAccepted()) {
            if (e.getReceivingPlayer() != null) {
                if (invalidRegion(e.getReceivingPlayer())) {
                    e.sendMessage(e.getReceivingPlayer(), e.getReceiver(), Lang.getPrefix() + Lang.get("Cannot_trade_in_region", e.getReceivingPlayer()));
                    e.setCancelled(true);
                    return;
                }
            }

            if (e.getSendingPlayer() != null) {
                if (invalidRegion(e.getSendingPlayer())) {
                    e.sendMessage(e.getReceivingPlayer(), e.getReceiver(), Lang.getPrefix() + Lang.get("Other_cannot_trade_in_region", e.getReceivingPlayer()));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onRequest(TradeRequestEvent e) {
        if (e.getSendingPlayer() != null) {
            if (invalidRegion(e.getSendingPlayer())) {
                e.sendMessage(e.getSendingPlayer(), e.getSender(), Lang.getPrefix() + Lang.get("Cannot_trade_in_region"));
                e.setCancelled(true);
                return;
            }
        }

        if (e.getReceivingPlayer() != null) {
            if (invalidRegion(e.getReceivingPlayer())) {
                e.sendMessage(e.getSendingPlayer(), e.getSender(), Lang.getPrefix() + Lang.get("Other_cannot_trade_in_region"));
                e.setCancelled(true);
            }
        }
    }

    private boolean invalidRegion(@NotNull Player player) {
        if (mode == RegionMode.BLACKLIST) return !allowedInBlackList(player);
        else return !checkWhiteList(player);
    }

    /**
     * @param player The player to check.
     * @return True if the player is in a region that is allowed.
     */
    private boolean allowedInBlackList(@NotNull Player player) {
        return getRegions(player.getLocation()).noneMatch(regions::contains);
    }

    /**
     * @param player The player to check.
     * @return True if the player is in a region that is allowed.
     */
    private boolean checkWhiteList(@NotNull Player player) {
        return getRegions(player.getLocation()).anyMatch(regions::contains);
    }

    @NotNull
    private Stream<String> getRegions(@NotNull Location location) {
        try {
            return adapter.getRegion(location);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }

    private void prepareAdapter() {
        try {
            WorldGuardAdapter.test();
            adapter = new WorldGuardAdapter();
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            try {
                WorldGuardAdapter_12.test();
                adapter = new WorldGuardAdapter_12();
            } catch (ClassNotFoundException | NoClassDefFoundError e1) {
                TradeSystem.getInstance().getLogger().log(Level.WARNING, "Could not hook into WorldGuard. Please contact the author!");
            }
        }
    }

    private boolean loadRegions() {
        FileConfiguration config = TradeSystem.getInstance().getFileManager().getFile("Config").getConfig();
        ConfigurationSection configRegions = config.getConfigurationSection("TradeSystem.Allowed_Regions");
        if (configRegions == null) return false;

        String modeName = configRegions.getString("Mode");
        mode = RegionMode.byName(modeName);
        if (mode == null) {
            TradeSystem.getInstance().getLogger().log(Level.WARNING, String.format("Invalid region mode '%s'. Using BLACKLIST.", mode));
            mode = RegionMode.BLACKLIST;
        }

        this.regions = new HashSet<>(configRegions.getStringList("Regions"));
        return true;
    }

    @Override
    public @NotNull String getPluginName() {
        return "WorldGuard";
    }

    private enum RegionMode {
        BLACKLIST, WHITELIST;

        @Nullable
        public static RegionMode byName(@Nullable String name) {
            for (RegionMode value : values()) {
                if (value.name().equalsIgnoreCase(name)) return value;
            }

            return null;
        }
    }
}
