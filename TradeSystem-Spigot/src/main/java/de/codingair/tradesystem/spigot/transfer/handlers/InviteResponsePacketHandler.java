package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.InviteResponsePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeRequestPreResponseEvent;
import de.codingair.tradesystem.spigot.events.TradeRequestResponseEvent;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class InviteResponsePacketHandler implements ResponsiblePacketHandler<InviteResponsePacket, InviteResponsePacket.ResultPacket> {
    @Override
    public void process(@NotNull InviteResponsePacket packet, @NotNull Proxy proxy, @Nullable Object connection, @NotNull Direction direction) {
        //ignore response
        response(packet, proxy, connection, direction);
    }

    @Override
    public @NotNull CompletableFuture<InviteResponsePacket.ResultPacket> response(@NotNull InviteResponsePacket packet, @NotNull Proxy proxy, @Nullable Object o, @NotNull Direction direction) {
        Player player = Bukkit.getPlayerExact(packet.getInviter());
        if (player == null) {
            return CompletableFuture.completedFuture(new InviteResponsePacket.ResultPacket(InviteResponsePacket.Result.NOT_ONLINE, null));
        }

        TradeRequestPreResponseEvent e = new TradeRequestPreResponseEvent(player.getName(), player.getUniqueId(), player, packet.getResponding(), packet.getRespondingId(), null, packet.isAccept());
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) return CompletableFuture.completedFuture(null);  // let other server run into timeout to indicate plugin handling

        TradeRequestResponseEvent e2 = new TradeRequestResponseEvent(player.getName(), player.getUniqueId(), player, packet.getResponding(), packet.getRespondingId(), null, packet.isAccept());
        Bukkit.getPluginManager().callEvent(e2);

        TradeSystem.invitations().invalidate(player, packet.getResponding());
        if (packet.isAccept()) {
            //start
            TradeSystem.handler().startTrade(player, packet.getResponding(), packet.getRespondingId(), packet.getRespondingWorld(), packet.getRespondingServer(), true);
            return CompletableFuture.completedFuture(new InviteResponsePacket.ResultPacket(InviteResponsePacket.Result.SUCCESS, player.getWorld().getName()));
        } else {
            //ignored answer
            Lang.send(player, "Request_Was_Denied", new Lang.P("player", packet.getResponding()));
            return CompletableFuture.completedFuture(null);
        }
    }
}
