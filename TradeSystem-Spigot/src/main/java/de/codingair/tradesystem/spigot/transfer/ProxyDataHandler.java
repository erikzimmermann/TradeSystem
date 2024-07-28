package de.codingair.tradesystem.spigot.transfer;

import de.codingair.codingapi.server.specification.Version;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.blacklist.BlockedItem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class ProxyDataHandler implements PluginMessageListener {
    //abbreviation to case-sensitive name
    private final Map<String, String> cache = new HashMap<>();

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
    private String tradeProxyVersion = null;
    private String serverName = null;
    private boolean noticedAboutTradeProxy = false;

    public void onEnable() {
        if (!TradeSystem.handler().tradeProxy()) {
            // fix: custom payload attacks
            // When TradeProxy is not enabled,
            // all packets must be ignored to prevent any communication with external clients.
            return;
        }

        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(TradeSystem.getInstance(), "BungeeCord", this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(TradeSystem.getInstance(), "BungeeCord");
        checkForServerName();
    }

    public void onDisable() {
        if (!TradeSystem.handler().tradeProxy()) return;

        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(TradeSystem.getInstance(), "BungeeCord", this);
        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(TradeSystem.getInstance(), "BungeeCord");
        this.players.clear();
    }

    public void sendTradeProxyNote() {
        if (TradeSystem.handler().tradeProxy()) return;
        if (noticedAboutTradeProxy) return;

        TradeSystem.getInstance().getLogger().warning("========================================");
        TradeSystem.getInstance().getLogger().warning("The 'TradeProxy' option is disabled but TradeProxy has been detected. If you are sure that TradeProxy is installed, you can activate the 'TradeProxy' option in your config.yml file or by running 'tradesystem activateTradeProxy confirm'. This check prevents custom payload attacks.");
        TradeSystem.getInstance().getLogger().warning("========================================");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(Permissions.PERMISSION_MODIFY)) continue;
            player.sendMessage(Lang.getPrefix() + "§cWarning! §7The 'TradeProxy' option is §cdisabled §7but §eTradeProxy §7has been detected. If you are sure that §eTradeProxy §7is installed, you can activate the 'TradeProxy' option in your config.yml file or by running §e/tradesystem activateTradeProxy confirm§7. This check prevents §ccustom payload attacks§7.");
        }

        noticedAboutTradeProxy = true;
    }

    public void join(@NotNull String player, @NotNull UUID playerId) {
        this.players.put(player.toLowerCase(), player);
        this.uuids.put(player.toLowerCase(), playerId);

        // change invokes cache update
        cache.clear();
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

        // change invokes cache update
        cache.clear();
    }

    public void clearPlayers() {
        this.cache.clear();
        this.players.clear();
    }

    public int getTradeHash() {
        Pattern pattern = TradeSystem.getInstance().getLayoutManager().getActive();

        int patternHash = pattern.hashCode();
        int cooldown = TradeSystem.handler().getCountdownRepetitions() * TradeSystem.handler().getCountdownInterval();

        int blacklist = 0;
        for (BlockedItem blockedItem : TradeSystem.handler().getBlacklist()) {
            blacklist = Objects.hash(blacklist, blockedItem.hashCode());
        }

        return Objects.hash(
                Version.get(),                                              // fix: java.lang.IllegalArgumentException: Newer version! Server downgrades are not supported!
                patternHash,
                cooldown,
                TradeSystem.handler().isRevokeReadyOnChange(),
                blacklist,
                TradeSystem.getInstance().getDescription().getVersion(),    // for packet compatibility
                TradeSystem.handler().isDropItems()                         // for item balancing
        );
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

        found = cache.get(lowerName);
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
        Trade trade = TradeSystem.handler().getTrade(name);

        if (trade instanceof ProxyTrade && other.equals(trade.getOther(name))) return (ProxyTrade) trade;
        return null;
    }

    @Nullable
    public String getTradeProxyVersion() {
        return tradeProxyVersion;
    }

    public void setTradeProxyVersion(@NotNull String tradeProxyVersion) {
        this.tradeProxyVersion = tradeProxyVersion;
    }

    public void checkForServerName() {
        if (serverName != null) return;

        Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
            if (serverName != null) return;

            Player player = Bukkit.getOnlinePlayers().stream().findAny().orElse(null);
            if (player == null) return;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            try {
                dos.writeUTF("GetServer");
            } catch (IOException e) {
                throw new RuntimeException("Could not write plugin message", e);
            }

            player.sendPluginMessage(TradeSystem.getInstance(), "BungeeCord", baos.toByteArray());
        }, 20);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals("BungeeCord") || serverName != null) return;

        ByteArrayInputStream bais = new ByteArrayInputStream(message);
        DataInputStream dis = new DataInputStream(bais);

        try {
            String subChannel = dis.readUTF();
            if (!subChannel.equals("GetServer")) return;

            serverName = dis.readUTF();
        } catch (IOException e) {
            throw new RuntimeException("Could not read plugin message", e);
        }
    }

    @Nullable
    public String getServerName() {
        return serverName;
    }
}
