package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.PublishSkinPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PublishSkinPacketHandler implements PacketHandler<PublishSkinPacket> {
    @Override
    public void process(@NotNull PublishSkinPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        TradeSystem.proxy().addSkin(packet.getPlayer(), packet.getSkinId());
    }
}
