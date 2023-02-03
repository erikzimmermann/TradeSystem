package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeIconUpdatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TradeIconUpdatePacketHandler implements PacketHandler<TradeIconUpdatePacket> {
    @Override
    public void process(@NotNull TradeIconUpdatePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        Player player = Bukkit.getPlayer(packet.getRecipient());
        ProxyTrade t = TradeSystem.proxy().getTrade(packet.getRecipient(), packet.getSender());

        if (t != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
            DataInputStream in = new DataInputStream(bais);

            TradeIcon icon = t.getLayout()[1].getIcons()[packet.getSlot()];
            try {
                icon.deserialize(in);
            } catch (IOException e) {
                e.printStackTrace();
            }

            t.receiveTradeIconUpdate(icon);
        }
    }
}
