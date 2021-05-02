package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.proxy.packets.PlayerJoinPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerJoinPacketHandler implements PacketHandler<PlayerJoinPacket> {

    @Override
    public void process(@NotNull PlayerJoinPacket playerJoinPacket, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        TradeSystem.proxy().join(playerJoinPacket.getPlayer());
    }
}
