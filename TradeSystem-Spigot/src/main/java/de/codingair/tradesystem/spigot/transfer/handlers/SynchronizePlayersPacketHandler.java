package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.SynchronizePlayersPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SynchronizePlayersPacketHandler implements PacketHandler<SynchronizePlayersPacket> {
    @Override
    public void process(@NotNull SynchronizePlayersPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        TradeSystem.proxy().clearPlayers();
    }
}
