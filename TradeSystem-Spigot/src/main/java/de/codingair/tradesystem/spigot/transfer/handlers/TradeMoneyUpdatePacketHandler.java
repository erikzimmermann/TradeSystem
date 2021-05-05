package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeMoneyUpdatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradeMoneyUpdatePacketHandler implements PacketHandler<TradeMoneyUpdatePacket> {

    @Override
    public void process(@NotNull TradeMoneyUpdatePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        Player player = Bukkit.getPlayer(packet.getRecipient());
        ProxyTrade t = TradeSystem.proxy().getTrade(player, packet.getRecipient(), packet.getSender());
        if (t == null) return;
        t.receiveMoneyData(packet.getMoney());
    }
}
