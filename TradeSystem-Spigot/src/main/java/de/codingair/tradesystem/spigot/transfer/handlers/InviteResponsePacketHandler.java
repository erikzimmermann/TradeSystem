package de.codingair.tradesystem.spigot.transfer.handlers;

import de.codingair.packetmanagement.handlers.ResponsiblePacketHandler;
import de.codingair.packetmanagement.utils.Direction;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.proxy.packets.InviteResponsePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
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
        Player player = Bukkit.getPlayer(packet.getInviter());
        if (player == null) {
            return CompletableFuture.completedFuture(new InviteResponsePacket.ResultPacket(InviteResponsePacket.Result.NOT_ONLINE));
        }

        TradeSystem.invitations().invalidate(player, packet.getResponding());
        if (packet.isAccept()) {
            //start
            TradeSystem.man().startTrade(player, null, packet.getResponding());
            return CompletableFuture.completedFuture(new InviteResponsePacket.ResultPacket(InviteResponsePacket.Result.SUCCESS));
        } else {
            //ignored answer
            player.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied", player).replace("%player%", packet.getResponding()));
            return CompletableFuture.completedFuture(null);
        }
    }
}
