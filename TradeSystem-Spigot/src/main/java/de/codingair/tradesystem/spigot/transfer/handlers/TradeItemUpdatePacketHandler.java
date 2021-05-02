package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.proxy.packets.TradeItemUpdatePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TradeItemUpdatePacketHandler implements PacketHandler<TradeItemUpdatePacket> {

    @Override
    public void process(@NotNull TradeItemUpdatePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        Player player = Bukkit.getPlayer(packet.getRecipient());
        ProxyTrade t = TradeSystem.proxy().getTrade(player, packet.getRecipient(), packet.getSender());
        if (t == null) return;

        Map<String, Object> data = packet.getItem();
        t.receiveItemData(packet.getSlotId(), data == null ? null : ItemStack.deserialize(data));
    }
}
