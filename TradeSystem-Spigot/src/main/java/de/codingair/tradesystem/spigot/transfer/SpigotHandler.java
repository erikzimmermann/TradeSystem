package de.codingair.tradesystem.spigot.transfer;

import de.codingair.packetmanagement.variants.bytestream.OneWayStreamDataHandler;
import de.codingair.tradesystem.proxy.packets.*;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.transfer.handlers.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SpigotHandler extends OneWayStreamDataHandler<Player> implements PluginMessageListener {
    public SpigotHandler(TradeSystem plugin) {
        super("tradesystem", plugin);
    }

    @Override
    public void registering() {
        for (PacketType value : PacketType.values()) {
            registerPacket(value.getPacketClass());
        }

        registerHandler(PlayerJoinPacket.class, new PlayerJoinPacketHandler());
        registerHandler(PlayerQuitPacket.class, new PlayerQuitPacketHandler());
        registerHandler(TradeInvitePacket.class, new TradeInvitePacketHandler());
        registerHandler(PlayerInventoryPacket.class, new PlayerInventoryPacketHandler());
        registerHandler(TradeItemUpdatePacket.class, new TradeItemUpdatePacketHandler());
        registerHandler(TradeStateUpdatePacket.class, new TradeStateUpdatePacketHandler());
        registerHandler(InviteResponsePacket.class, new InviteResponsePacketHandler());
        registerHandler(TradeCheckFinishPacket.class, new TradeCheckFinishPacketHandler());
        registerHandler(SynchronizePlayersPacket.class, new SynchronizePlayersPacketHandler());
        registerHandler(TradeIconUpdatePacket.class, new TradeIconUpdatePacketHandler());
        registerHandler(PlayerStatePacket.class, new PlayerStatePacketHandler());
        registerHandler(PublishSkinPacket.class, new PublishSkinPacketHandler());
    }

    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel((TradeSystem) proxy, channelProxy);
        Bukkit.getMessenger().registerIncomingPluginChannel((TradeSystem) proxy, channelBackend, this);

        if (!Bukkit.getOnlinePlayers().isEmpty()) send(new SynchronizePlayersPacket(), null);
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
}
