package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.PlayerQuitPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerQuitPacketHandler implements PacketHandler<PlayerQuitPacket> {

    @Override
    public void process(@NotNull PlayerQuitPacket playerQuitPacket, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        TradeSystem.proxy().quit(playerQuitPacket.getPlayer());
    }
}
