package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.managers.InvitationManager;
import de.codingair.tradesystem.spigot.trade.managers.RequestManager;
import de.codingair.tradesystem.spigot.trade.managers.RuleManager;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TradeInvitePacketHandler implements ResponsiblePacketHandler<TradeInvitePacket, TradeInvitePacket.ResultPacket> {
    @Override
    public @NotNull CompletableFuture<TradeInvitePacket.ResultPacket> response(@NotNull TradeInvitePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        System.out.println("TradeInvitePacketHandler");

        Player player = Bukkit.getPlayer(packet.getRecipient());

        TradeInvitePacket.Result result;

        //same trade options?
        int hash = TradeSystem.proxy().getTradeHash();
        if (hash == packet.getTradeHash()) {
            //compatible
            if (player == null) result = TradeInvitePacket.Result.NOT_ONLINE;
            else {
                result = RuleManager.isOtherViolatingRules(player);

                if (result == TradeInvitePacket.Result.INVITED) {
                    System.out.println("inviting");
                    InvitationManager.registerExpiration(null, packet.getInviter(), player, player.getName());
                    RequestManager.sendRequest(packet.getInviter(), player);
                }
            }
        } else result = TradeInvitePacket.Result.INCOMPATIBLE;

        System.out.println("Result: " + result.name());
        return CompletableFuture.completedFuture(new TradeInvitePacket.ResultPacket(result));
    }
}
