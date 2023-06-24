package de.codingair.tradesystem.spigot.trade.managers;

import de.codingair.packetmanagement.exceptions.TimeOutException;
import de.codingair.tradesystem.proxy.packets.InviteResponsePacket;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.proxy.packets.TradeStateUpdatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeRequestExpireEvent;
import de.codingair.tradesystem.spigot.events.TradeRequestPreResponseEvent;
import de.codingair.tradesystem.spigot.events.TradeRequestResponseEvent;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InvitationManager {
    /**
     * receiver to invitations from others
     */
    private final Map<String, Map<String, Invitation>> invitations = new HashMap<>();
    private int expirationHandler = -1;

    public void startExpirationHandler() {
        long expiration = TradeSystem.man().getRequestExpirationTime() * 1000L;

        expirationHandler = Bukkit.getScheduler().scheduleSyncRepeatingTask(TradeSystem.getInstance(), () -> invitations.entrySet().removeIf(e -> {
            e.getValue().values().removeIf(inv -> {
                boolean valid = inv.valid(expiration);
                if (valid) return false;

                notifyExpiration(e.getKey(), inv);
                return true;
            });

            return e.getValue().isEmpty();
        }), 20, 10);
    }

    public void stopExpirationHandler() {
        if (expirationHandler == -1) return;
        Bukkit.getScheduler().cancelTask(expirationHandler);
        expirationHandler = -1;
    }

    private void notifyExpiration(String nameReceiver, Invitation inv) {
        String nameInviter = inv.getName();
        Player inviter = Bukkit.getPlayerExact(nameInviter);
        Player receiver = Bukkit.getPlayerExact(nameReceiver);

        if (inviter != null) nameInviter = inviter.getName();
        else nameInviter = TradeSystem.proxy().getCaseSensitive(nameInviter);
        if (receiver != null) nameReceiver = receiver.getName();
        else nameReceiver = TradeSystem.proxy().getCaseSensitive(nameReceiver);

        String nameInviterCase = nameInviter;
        String nameReceiverCase = nameReceiver;
        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
            TradeRequestExpireEvent event = new TradeRequestExpireEvent(nameInviterCase, inviter, nameReceiverCase, receiver);
            Bukkit.getPluginManager().callEvent(event);
        });

        if (inviter != null) inviter.sendMessage(Lang.getPrefix() + Lang.get("Your_request_expired", inviter, new Lang.P("player", nameReceiverCase)));
        if (receiver != null) receiver.sendMessage(Lang.getPrefix() + Lang.get("Request_expired", receiver, new Lang.P("player", nameInviterCase)));
    }

    private static InvitationManager instance() {
        return TradeSystem.invitations();
    }

    //proxy usage
    public static boolean processInvitation(@NotNull Player inviter, @Nullable Player receiver, @NotNull String receiverName) {
        // check if this player has already an invitation
        Map<String, Invitation> map = instance().getInvitations(inviter.getName());
        boolean invited = map != null && map.remove(receiverName.toLowerCase()) != null;

        if (invited) {
            if (map.isEmpty()) instance().clear(inviter.getName());
            acceptInvitation(inviter, receiver, receiverName);
            return true;
        }

        // check if other player has already an invitation from this player
        map = instance().getInvitations(receiverName);
        boolean alreadyRequested = map != null && map.containsKey(inviter.getName().toLowerCase());

        if (alreadyRequested) {
            inviter.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Trade_Spam", inviter));
            return true;
        }

        //wait for trade compatibility check if proxyTrade == true
        boolean proxyTrade = receiver == null;
        if (!proxyTrade) {
            registerInvitation(inviter, inviter.getName(), receiver, receiverName);
            inviter.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Invited", inviter, new Lang.P("player", receiverName)));
        }
        return false;
    }

    //proxy usage
    public static void registerInvitation(@Nullable Player inviter, @NotNull String nameInviter, @Nullable Player receiver, @NotNull String nameReceiver) {
        boolean proxy = inviter == null || receiver == null;
        Invitation invitation = new Invitation(nameInviter, proxy);

        instance().invitations
                .computeIfAbsent(nameReceiver.toLowerCase(), $ -> new HashMap<>())
                .put(nameInviter.toLowerCase(), invitation);
    }

    //proxy usage
    private static void acceptInvitation(@NotNull Player player, @Nullable Player other, @NotNull String name) {
        player.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", player));
        if (other != null) {
            Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(name, other, player.getName(), player, true));

            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", player, new Lang.P("player", player.getName())));
            TradeSystem.getInstance().getTradeManager().startTrade(other, player, player.getName(), true);
        } else {
            //START PROXY
            TradeSystem.proxyHandler().send(new TradeInvitePacket(player.getName(), name, TradeSystem.proxy().getTradeHash()), player).whenComplete((suc, t) -> {
                if (t != null) t.printStackTrace();
                else {
                    if (suc.getResult() == TradeInvitePacket.Result.START_TRADING) {
                        //call event
                        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(name, null, player.getName(), player, true)));

                        TradeSystem.getInstance().getTradeManager().startTrade(player, null, name, false);
                    } else RuleManager.message(player, name, suc.getResult(), suc.getServer());
                }
            });
        }
    }

    @Nullable
    private Map<String, Invitation> getInvitations(String name) {
        return this.invitations.get(name.toLowerCase());
    }

    @NotNull
    public Set<String> getInvitationNames(@NotNull String name) {
        Map<String, Invitation> map = getInvitations(name);
        return map == null ? new HashSet<>() : map.keySet();
    }

    public void clear(String name) {
        this.invitations.remove(name.toLowerCase());
    }

    public void invalidate(@NotNull Player player, @NotNull Invitation invitation) {
        Map<String, Invitation> l = getInvitations(player.getName());
        if (l != null) {
            l.remove(invitation.getName().toLowerCase());
            invalidateIfEmpty(player.getName(), l);
        }
    }

    public void invalidate(@NotNull Player inviter, @NotNull String other) {
        Map<String, Invitation> l = getInvitations(other);
        if (l != null) {
            l.remove(inviter.getName().toLowerCase());
            invalidateIfEmpty(other, l);
        }
    }

    public void clear() {
        this.invitations.clear();
    }

    private void invalidateIfEmpty(@NotNull String name, @Nullable Map<String, Invitation> map) {
        if (map == null) map = this.getInvitations(name);
        if (map != null && map.isEmpty()) this.clear(name);
    }

    /**
     * @param name The player name whom invitations should be cancelled.
     */
    public void cancelAll(@NotNull String name) {
        //cancel all open invitations on proxy too
        clear(name);

        this.invitations.entrySet().removeIf(e -> {
            Map<String, Invitation> invitations = e.getValue();
            invitations.remove(name.toLowerCase());
            return invitations.isEmpty();
        });
    }

    private void cancel(@Nullable Player player, @NotNull String sender, @NotNull String receiver) {
        TradeStateUpdatePacket packet = new TradeStateUpdatePacket(sender, receiver, TradeStateUpdatePacket.State.CANCELLED, null);
        TradeSystem.proxyHandler().send(packet, player);
    }

    @Nullable
    private Invitation getInvitationOrMessagePlayer(@NotNull Player player, @Nullable String argument) {
        Map<String, Invitation> invitations = this.getInvitations(player.getName());

        if (invitations == null || invitations.isEmpty()) {
            player.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found", player));
            return null;
        }

        if (argument == null) {
            if (invitations.size() > 1) player.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests", player));
            else return invitations.values().stream().findAny().get();
            return null;
        } else {
            Invitation inv = invitations.get(argument.toLowerCase());
            if (inv == null) player.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found", player));
            return inv;
        }
    }

    /**
     * Handles the trade request response.
     *
     * @param sender   The player who responded.
     * @param argument The player name forwarded by the command.
     * @param accept   True if the player accepted the request, false if he denied it.
     */
    public void handleInvitation(@NotNull Player sender, @Nullable String argument, boolean accept) {
        Invitation invitation = getInvitationOrMessagePlayer(sender, argument);
        if (invitation == null) return;

        TradeRequestPreResponseEvent e = new TradeRequestPreResponseEvent(invitation.getCaseSensitiveName(), invitation.getPlayer(), sender.getName(), sender, accept);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) return;

        if (accept) accept(sender, invitation);
        else deny(sender, invitation);
    }

    private void deny(@NotNull Player sender, @NotNull Invitation invitation) {
        Player other = invitation.getPlayer();
        invalidate(sender, invitation);

        if (other == null) {
            if (TradeSystem.proxy().isOnline(invitation.getName())) {
                //call event
                Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender, TradeSystem.proxy().getCaseSensitive(invitation.getName()), null, false));

                TradeSystem.proxyHandler().send(new InviteResponsePacket(invitation.getName(), sender.getName(), false, false).noFuture(), sender);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", sender, new Lang.P("player", invitation.getName())));
            } else sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", sender));
            return;
        }

        //call event
        Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender, other.getName(), other, false));

        sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", sender, new Lang.P("player", other.getName())));
        other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied", sender, new Lang.P("player", sender.getName())));
    }

    private void accept(@NotNull Player sender, @NotNull Invitation invitation) {
        Player other = invitation.getPlayer();

        if (other == null) {
            if (TradeSystem.proxy().isOnline(invitation.getName())) {
                String name = TradeSystem.proxy().getCaseSensitive(invitation.getName());

                if (RuleManager.isViolatingRules(sender)) return;

                TradeSystem.proxyHandler().send(new InviteResponsePacket(name, sender.getName(), true, false), sender, 1000).whenComplete((suc, t) -> {
                    if (t != null) {
                        if (t instanceof TimeOutException) return; // external plugin handling
                        t.printStackTrace();
                    } else {
                        if (suc.getResult() == InviteResponsePacket.Result.SUCCESS) {
                            //call event
                            Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender, name, null, true)));

                            invalidate(sender, invitation);

                            sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", sender));
                            TradeSystem.getInstance().getTradeManager().startTrade(sender, null, name, false);
                        } else if (suc.getResult() == InviteResponsePacket.Result.NOT_ONLINE) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", sender));
                        } else if (suc.getResult() == InviteResponsePacket.Result.OTHER_GROUP) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", sender));
                        }
                    }
                });
            } else sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", sender));
            return;
        }

        if (RuleManager.isViolatingRules(sender, other)) return;

        //call event
        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender, other.getName(), other, true)));

        invalidate(sender, invitation);

        sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", sender));
        other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", sender, new Lang.P("player", sender.getName())));

        TradeSystem.getInstance().getTradeManager().startTrade(other, sender, sender.getName(), true);
    }

    public boolean isInvited(@NotNull Player inviter, @NotNull String receiver) {
        Map<String, Invitation> invitations = getInvitations(receiver);
        if (invitations == null) return false;
        return invitations.containsKey(inviter.getName().toLowerCase());
    }

    public static class Invitation {
        private final String name;
        private final boolean proxyInvite;
        private final long birth;

        public Invitation(String name, boolean proxyInvite) {
            this(name, proxyInvite, System.currentTimeMillis());
        }

        public Invitation(String name, boolean proxyInvite, long birth) {
            this.name = name;
            this.proxyInvite = proxyInvite;
            this.birth = birth;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public Player getPlayer() {
            return Bukkit.getPlayerExact(name);
        }

        @NotNull
        public String getCaseSensitiveName() {
            Player player = getPlayer();
            if (player != null) return player.getName();

            // try proxy
            return TradeSystem.proxy().getCaseSensitive(name);
        }

        public boolean isProxyInvite() {
            return proxyInvite;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || (!(o instanceof String) && getClass() != o.getClass())) return false;

            if (o instanceof String) {
                String invite = (String) o;
                return this.name.equalsIgnoreCase(invite);
            }

            Invitation invitation = (Invitation) o;
            return this.name.equalsIgnoreCase(invitation.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name.toLowerCase());
        }

        public boolean valid(long expirationTime) {
            return System.currentTimeMillis() - birth < expirationTime;
        }
    }
}
