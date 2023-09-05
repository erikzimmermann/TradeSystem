package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.PlayerStatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerStatePacketHandler implements PacketHandler<PlayerStatePacket> {
    @Override
    public void process(@NotNull PlayerStatePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        TradeSystem.handler().setState(packet.getPlayerId(), packet.getPlayerName(), packet.isOnline());
    }
}
