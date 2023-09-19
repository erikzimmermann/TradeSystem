package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.packets.impl.SuccessPacket;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeCheckFinishPacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.ProxyTrade;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TradeCheckFinishPacketHandler implements ResponsiblePacketHandler<TradeCheckFinishPacket, SuccessPacket> {

    @Override
    public @NotNull CompletableFuture<SuccessPacket> response(@NotNull TradeCheckFinishPacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        ProxyTrade t = TradeSystem.proxy().getTrade(packet.getRecipient(), packet.getSender());
        Player recipient = Bukkit.getPlayerExact(packet.getRecipient());

        boolean success;
        if (recipient == null || t == null) success = false;
        else if (!recipient.equals(t.getPlayer(Perspective.PRIMARY))) success = false;
        else success = t.receiveFinishCheck();

        return CompletableFuture.completedFuture(new SuccessPacket(success));
    }
}
