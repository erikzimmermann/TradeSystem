package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeStateUpdatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradeStateUpdatePacketHandler implements PacketHandler<TradeStateUpdatePacket> {

    @Override
    public void process(@NotNull TradeStateUpdatePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        ProxyTrade t = TradeSystem.proxy().getTrade(packet.getRecipient(), packet.getSender());
        if (t == null) return;
        t.receiveState(packet.getState(), packet.getExtra());
    }
}
