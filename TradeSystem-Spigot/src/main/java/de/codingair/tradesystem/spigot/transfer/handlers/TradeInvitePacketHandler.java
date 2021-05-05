package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.managers.InvitationManager;
import de.codingair.tradesystem.spigot.trade.managers.RequestManager;
import de.codingair.tradesystem.spigot.trade.managers.RuleManager;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TradeInvitePacketHandler implements ResponsiblePacketHandler<TradeInvitePacket, TradeInvitePacket.ResultPacket> {
    @Override
    public @NotNull CompletableFuture<TradeInvitePacket.ResultPacket> response(@NotNull TradeInvitePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
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
                    if (TradeSystem.invitations().isInvited(player, packet.getInviter())) {
                        //double invite -> start trading

                        TradeSystem.invitations().invalidate(player, packet.getInviter());
                        player.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", player).replace("%player%", packet.getInviter()));
                        TradeSystem.man().startTrade(player, null, packet.getInviter());
                        result = TradeInvitePacket.Result.START_TRADING;
                    } else {
                        InvitationManager.registerExpiration(null, packet.getInviter(), player, player.getName());
                        RequestManager.sendRequest(packet.getInviter(), player);
                    }
                }
            }
        } else result = TradeInvitePacket.Result.INCOMPATIBLE;

        return CompletableFuture.completedFuture(new TradeInvitePacket.ResultPacket(result));
    }
}
