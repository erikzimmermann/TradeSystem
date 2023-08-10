package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeItemUpdatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.transfer.utils.ItemStackUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class TradeItemUpdatePacketHandler implements PacketHandler<TradeItemUpdatePacket> {

    @Override
    public void process(@NotNull TradeItemUpdatePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        ProxyTrade t = TradeSystem.proxy().getTrade(packet.getRecipient(), packet.getSender());
        if (t == null) return;

        try {
            byte[] data = packet.getItem();
            t.receiveItemData(packet.getSlotId(), ItemStackUtils.deserialize(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
