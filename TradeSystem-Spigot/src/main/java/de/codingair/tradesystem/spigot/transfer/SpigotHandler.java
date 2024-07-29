package de.codingair.tradesystem.spigot.transfer;

import de.codingair.packetmanagement.packets.Packet;
import de.codingair.packetmanagement.packets.RequestPacket;
import de.codingair.packetmanagement.packets.ResponsePacket;
import de.codingair.packetmanagement.variants.bytestream.OneWayStreamDataHandler;
import de.codingair.tradesystem.proxy.packets.*;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.transfer.handlers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpigotHandler extends OneWayStreamDataHandler<Player> implements PluginMessageListener {

    public SpigotHandler(TradeSystem plugin) {
        super("tradesystem", plugin);

        // ignore unregistered packets to make data handling more robust
        setIgnoreUnregistered(true);
    }

    @Override
    public void registering() {
        for (PacketType value : PacketType.values()) {
            registerPacket(value.getPacketClass());
        }

        if (!TradeSystem.handler().tradeProxy()) {
            // fix: custom payload attacks
            // When TradeProxy is not enabled,
            // all packets must be ignored to prevent any communication with external clients.

            registerHandler(VersionPacket.class, new UnauthorizedVersionPacketHandler());
            return;
        }

        registerHandler(PlayerJoinPacket.class, new PlayerJoinPacketHandler());
        registerHandler(PlayerQuitPacket.class, new PlayerQuitPacketHandler());
        registerHandler(PlayerStatePacket.class, new PlayerStatePacketHandler());
        registerHandler(PublishSkinPacket.class, new PublishSkinPacketHandler());
        registerHandler(VersionPacket.class, new VersionPacketHandler());

        registerHandler(TradeInvitePacket.class, new TradeInvitePacketHandler());
        registerHandler(TradeInitializedPacket.class, new TradeInitializedPacketHandler());
        registerHandler(PlayerInventoryPacket.class, new PlayerInventoryPacketHandler());
        registerHandler(TradeItemUpdatePacket.class, new TradeItemUpdatePacketHandler());
        registerHandler(TradeStateUpdatePacket.class, new TradeStateUpdatePacketHandler());
        registerHandler(InviteResponsePacket.class, new InviteResponsePacketHandler());
        registerHandler(TradeCheckFinishPacket.class, new TradeCheckFinishPacketHandler());
        registerHandler(SynchronizePlayersPacket.class, new SynchronizePlayersPacketHandler());
        registerHandler(TradeIconUpdatePacket.class, new TradeIconUpdatePacketHandler());
    }

    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel((TradeSystem) proxy, channelProxy);
        Bukkit.getMessenger().registerIncomingPluginChannel((TradeSystem) proxy, channelBackend, this);

        if (TradeSystem.handler().tradeProxy() && !Bukkit.getOnlinePlayers().isEmpty())
            send(new SynchronizePlayersPacket(), null);
    }

    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel((TradeSystem) proxy, channelProxy);
        Bukkit.getMessenger().unregisterIncomingPluginChannel((TradeSystem) proxy, channelBackend, this);
    }

    @Override
    protected void send(byte[] data, Player p) {
        if (p == null) p = getAny();
        if (p == null) return; //nobody online

        p.sendPluginMessage(getProxy(), channelProxy, data);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String tag, @NotNull Player player, byte[] bytes) {
        if (tag.equals(getChannelBackend())) receive(bytes, player);
    }

    private Player getAny() {
        Optional<? extends Player> opt = Bukkit.getOnlinePlayers().stream().findFirst();
        return opt.orElse(null);
    }

    @Override
    public void send(@NotNull Packet packet, @Nullable Player connection) {
        if (!TradeSystem.handler().tradeProxy()) return;
        super.send(packet, connection);
    }

    @Override
    public <A extends ResponsePacket> CompletableFuture<A> send(@NotNull RequestPacket<A> packet, @Nullable Player connection) {
        if (!TradeSystem.handler().tradeProxy()) {
            CompletableFuture<A> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("TradeProxy not connected."));
            return future;
        }
        return super.send(packet, connection);
    }

    @Override
    public <A extends ResponsePacket> CompletableFuture<A> send(@NotNull RequestPacket<A> packet, @Nullable Player connection, long timeOut) {
        if (!TradeSystem.handler().tradeProxy()) {
            CompletableFuture<A> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("TradeProxy not connected."));
            return future;
        }
        return super.send(packet, connection, timeOut);
    }
}
