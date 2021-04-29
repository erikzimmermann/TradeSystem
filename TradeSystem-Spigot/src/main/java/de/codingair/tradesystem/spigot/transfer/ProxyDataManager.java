package de.codingair.tradesystem.spigot.transfer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.layout.utils.Pattern;
import de.codingair.tradesystem.spigot.utils.money.AdapterType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ProxyDataManager {
    //abbreviation to case-sensitive name
    private final Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    /**
     * lower-case to case-sensitive
     */
    private final HashMap<String, String> players = new HashMap<>();

    public void onDisable() {
        this.players.clear();
    }

    public void join(String player) {
        this.players.put(player.toLowerCase(), player);
    }

    public void clearPlayers() {
        this.cache.invalidateAll();
        this.players.clear();
    }

    public void quit(String player) {
        this.players.remove(player.toLowerCase());
    }

    public int getTradeHash() {
        Pattern layout = TradeSystem.getInstance().getLayoutManager().getActive();

        boolean money = AdapterType.canEnable() && TradeSystem.getInstance().getTradeManager().isTradeMoney();
        int type = AdapterType.getActiveType().ordinal();
        int tradeSlots = layout.getTradeSlotCount();
        int cooldown = TradeSystem.man().getCountdownRepetitions() * TradeSystem.man().getCountdownInterval();

        return Objects.hash(money, type, tradeSlots, cooldown);
    }

    public Stream<String> getPlayers(@Nullable CommandSender sender) {
        if (sender == null) return players.values().stream();
        else return players.values().stream().filter(n -> !n.equals(sender.getName()));
    }

    public String getCaseSensitive(String player) {
        return getPlayer(player);
    }

    public boolean isOnline(String player) {
        return getPlayer(player) != null;
    }

    private String getPlayer(String name) {
        String lowerName = name.toLowerCase(Locale.ENGLISH);

        String found = this.players.get(lowerName);
        if (found != null) return found;

        found = cache.getIfPresent(lowerName);
        if (found != null) return found;

        int delta = 2147483647;
        for (String player : this.players.values()) {
            if (player.toLowerCase().startsWith(lowerName)) {
                int curDelta = Math.abs(player.length() - lowerName.length());
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }

                if (curDelta == 0) {
                    break;
                }
            }
        }


        if (found != null) cache.put(lowerName, found);
        return found;
    }

    public @Nullable ProxyTrade getTrade(@Nullable Player player, @NotNull String name, @NotNull String other) {
        Trade trade = TradeSystem.man().getTrade(name);

        if (trade instanceof ProxyTrade && other.equals(trade.getOther(name))) return (ProxyTrade) trade;
        return null;
    }
}
