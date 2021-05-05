package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.PlayerInventoryPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PlayerInventoryPacketHandler implements PacketHandler<PlayerInventoryPacket> {
    @Override
    public void process(@NotNull PlayerInventoryPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        Player p = Bukkit.getPlayer(packet.getRecipient());
        ProxyTrade t = TradeSystem.proxy().getTrade(p, packet.getRecipient(), packet.getSender());
        if (t == null) return;

        ItemStack[] items = new ItemStack[36];

        for (int i = 0; i < packet.getItems().length; i++) {
            if (packet.getItems()[i] == null) continue;

            Map<String, Object> item = (Map<String, Object>) packet.getItems()[i];
            items[i] = ItemStack.deserialize(item);
        }

        t.setOtherInventory(items);
        t.cancelOverflow(0);
    }
}
