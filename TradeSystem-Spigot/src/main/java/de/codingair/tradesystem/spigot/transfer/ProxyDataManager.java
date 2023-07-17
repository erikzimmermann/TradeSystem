package de.codingair.tradesystem.spigot.transfer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.blacklist.BlockedItem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ProxyDataManager {
    //abbreviation to case-sensitive name
    private final Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    /**
     * lower-case to case-sensitive
     */
    private final HashMap<String, String> players = new HashMap<>();

    /**
     * lower-case name to uuid
     */
    private final HashMap<String, UUID> uuids = new HashMap<>();

    /**
     * lower-case name to skinId
     */
    private final HashMap<String, String> skins = new HashMap<>();

    public void onDisable() {
        this.players.clear();
    }

    public void join(@NotNull String player, @NotNull UUID playerId) {
        this.players.put(player.toLowerCase(), player);
        this.uuids.put(player.toLowerCase(), playerId);
    }

    public void addSkin(@NotNull String player, @NotNull String skinId) {
        this.skins.put(player.toLowerCase(), skinId);
    }

    @Nullable
    public String getSkin(@NotNull String player) {
        return this.skins.get(player.toLowerCase());
    }

    public void quit(@NotNull String player) {
        this.players.remove(player.toLowerCase());
        this.uuids.remove(player.toLowerCase());
        this.skins.remove(player.toLowerCase());
    }

    public void clearPlayers() {
        this.cache.invalidateAll();
        this.players.clear();
    }

    public int getTradeHash() {
        Pattern pattern = TradeSystem.getInstance().getLayoutManager().getActive();

        int patternHash = pattern.hashCode();
        int cooldown = TradeSystem.man().getCountdownRepetitions() * TradeSystem.man().getCountdownInterval();

        int blacklist = 0;
        for (BlockedItem blockedItem : TradeSystem.man().getBlacklist()) {
            blacklist = Objects.hash(blacklist, blockedItem.hashCode());
        }

        return Objects.hash(patternHash, cooldown, TradeSystem.man().isRevokeReadyOnChange(), blacklist);
    }

    public Stream<String> getPlayers(@Nullable CommandSender sender) {
        if (sender == null) return players.values().stream();
        else return players.values().stream().filter(n -> !n.equals(sender.getName()));
    }

    @NotNull
    public String getCaseSensitive(@NotNull String player) {
        String name = getPlayerName(player);
        return name == null ? player : name;
    }

    @NotNull
    public UUID getUniqueId(@NotNull String player) {
        return uuids.get(player.toLowerCase());
    }

    public boolean isOnline(String player) {
        return getPlayerName(player) != null;
    }

    @Nullable
    private String getPlayerName(@NotNull String name) {
        String lowerName = name.toLowerCase(Locale.ENGLISH);

        String found = this.players.get(lowerName);
        if (found != null) return found;

        found = cache.getIfPresent(lowerName);
        if (found != null) return found;

        for (String player : this.players.values()) {
            if (player.equalsIgnoreCase(name)) {
                found = player;
                break;
            }
        }

        if (found != null) cache.put(lowerName, found);
        return found;
    }

    public @Nullable ProxyTrade getTrade(@NotNull String name, @NotNull String other) {
        Trade trade = TradeSystem.man().getTrade(name);

        if (trade instanceof ProxyTrade && other.equals(trade.getOther(name))) return (ProxyTrade) trade;
        return null;
    }
}
