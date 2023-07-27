package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeRequestEvent;
import de.codingair.tradesystem.spigot.events.TradeRequestResponseEvent;
import de.codingair.tradesystem.spigot.trade.managers.InvitationManager;
import de.codingair.tradesystem.spigot.trade.managers.RequestManager;
import de.codingair.tradesystem.spigot.trade.managers.RuleManager;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TradeInvitePacketHandler implements ResponsiblePacketHandler<TradeInvitePacket, TradeInvitePacket.ResultPacket> {
    @Override
    public @NotNull CompletableFuture<TradeInvitePacket.ResultPacket> response(@NotNull TradeInvitePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        Player player = Bukkit.getPlayer(packet.getRecipient());
        UUID playerId = null;

        TradeInvitePacket.Result result;

        //same trade options?
        int hash = TradeSystem.proxy().getTradeHash();
        if (hash == packet.getTradeHash()) {
            //compatible
            if (player == null) result = TradeInvitePacket.Result.NOT_ONLINE;
            else {
                playerId = player.getUniqueId();
                result = RuleManager.isOtherViolatingRules(player);

                if (result == TradeInvitePacket.Result.INVITED) {
                    CompletableFuture<TradeInvitePacket.ResultPacket> future = new CompletableFuture<>();

                    Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
                        //call request event
                        TradeRequestEvent event = new TradeRequestEvent(packet.getInviter(), packet.getInviterId(), player, TradeSystem.handler().getRequestExpirationTime());
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            future.complete(new TradeInvitePacket.ResultPacket(TradeInvitePacket.Result.PLUGIN, player.getUniqueId()));
                            return;
                        }

                        if (TradeSystem.invitations().isInvited(player, packet.getInviter())) {
                            //double invite -> start trading

                            //call response event
                            TradeRequestResponseEvent responseEvent = new TradeRequestResponseEvent(player.getName(), player.getUniqueId(), player, packet.getInviter(), packet.getInviterId(), null, true);
                            Bukkit.getPluginManager().callEvent(responseEvent);

                            TradeSystem.invitations().invalidate(player, packet.getInviter());
                            Lang.send(player, "Request_Was_Accepted", new Lang.P("player", packet.getInviter()));
                            TradeSystem.handler().startTrade(player, null, packet.getInviter(), packet.getInviterId(), true);

                            future.complete(new TradeInvitePacket.ResultPacket(TradeInvitePacket.Result.START_TRADING, player.getUniqueId()));
                        } else {
                            InvitationManager.registerInvitation(null, packet.getInviter(), packet.getInviterId(), player, player.getName());
                            RequestManager.sendRequest(packet.getInviter(), player);
                            future.complete(new TradeInvitePacket.ResultPacket(TradeInvitePacket.Result.INVITED, player.getUniqueId()));
                        }
                    });

                    return future;
                }
            }
        } else result = TradeInvitePacket.Result.INCOMPATIBLE;

        return CompletableFuture.completedFuture(new TradeInvitePacket.ResultPacket(result, playerId));
    }
}
