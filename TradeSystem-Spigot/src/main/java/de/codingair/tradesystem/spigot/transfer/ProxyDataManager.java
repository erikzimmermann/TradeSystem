package de.codingair.tradesystem.spigot.transfer;

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
import java.util.Objects;
import java.util.stream.Stream;

public class ProxyDataManager {
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

    public void quit(String player) {
        this.players.remove(player.toLowerCase());
    }

    public int getTradeHash() {
        Pattern layout = TradeSystem.getInstance().getLayoutManager().getActive();

        boolean money = AdapterType.canEnable() && TradeSystem.getInstance().getTradeManager().isTradeMoney();
        int type = AdapterType.getActiveType().ordinal();
        int tradeSlots = layout.getTradeSlotCount();

        return Objects.hash(money, type, tradeSlots);
    }

    public Stream<String> getPlayers(@Nullable CommandSender sender) {
        if (sender == null) return players.values().stream();
        else return players.values().stream().filter(n -> !n.equals(sender.getName()));
    }

    public String getCaseSensitive(String player) {
        return this.players.get(player.toLowerCase());
    }

    public boolean isOnline(String player) {
        return this.players.containsKey(player.toLowerCase());
    }

    public @Nullable ProxyTrade getTrade(@Nullable Player player, @NotNull String name, @NotNull String other) {
        Trade trade = TradeSystem.man().getTrade(name);

        if (trade instanceof ProxyTrade && other.equals(trade.getOther(name))) return (ProxyTrade) trade;
        return null;
    }
}
