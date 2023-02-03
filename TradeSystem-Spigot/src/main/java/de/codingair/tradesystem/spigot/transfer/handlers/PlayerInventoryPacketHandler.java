package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.PacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.PlayerInventoryPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.transfer.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class PlayerInventoryPacketHandler implements PacketHandler<PlayerInventoryPacket> {
    @Override
    public void process(@NotNull PlayerInventoryPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        Player p = Bukkit.getPlayer(packet.getRecipient());
        ProxyTrade t = TradeSystem.proxy().getTrade(packet.getRecipient(), packet.getSender());
        if (t == null) return;

        try {
            ItemStack item;
            if (packet.getItem() == null) item = null;
            else item = ItemStackUtils.deserializeItemStack(packet.getItem());

            t.applyOtherInventoryItem(packet.getSlot(), item);
            t.cancelOverflow(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
