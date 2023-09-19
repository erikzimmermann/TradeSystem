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
     * receiver name to invitations from others
     */
    private final Map<String, Map<String, Invitation>> invitations = new HashMap<>();
    private int expirationHandler = -1;

    public void startExpirationHandler() {
        long expiration = TradeSystem.handler().getRequestExpirationTime() * 1000L;

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

    private void notifyExpiration(@NotNull String nameReceiver, @NotNull Invitation inv) {
        String nameInviter = inv.getName();
        UUID idInviter;
        UUID idReceiver;

        Player inviter = Bukkit.getPlayerExact(nameInviter);
        Player receiver = Bukkit.getPlayerExact(nameReceiver);

        if (inviter != null) {
            nameInviter = inviter.getName();
            idInviter = inviter.getUniqueId();
        } else {
            nameInviter = TradeSystem.proxy().getCaseSensitive(nameInviter);
            idInviter = TradeSystem.proxy().getUniqueId(nameInviter);
        }

        if (receiver != null) {
            nameReceiver = receiver.getName();
            idReceiver = receiver.getUniqueId();
        } else {
            nameReceiver = TradeSystem.proxy().getCaseSensitive(nameReceiver);
            idReceiver = TradeSystem.proxy().getUniqueId(nameReceiver);
        }

        String nameInviterCase = nameInviter;
        UUID finalIdInviter = idInviter;
        String nameReceiverCase = nameReceiver;
        UUID finalIdReceiver = idReceiver;
        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
            TradeRequestExpireEvent event = new TradeRequestExpireEvent(nameInviterCase, finalIdInviter, inviter, nameReceiverCase, finalIdReceiver, receiver);
            Bukkit.getPluginManager().callEvent(event);
        });

        if (inviter != null)
            Lang.send(inviter, "Your_request_expired", new Lang.P("player", nameReceiverCase));
        if (receiver != null)
            Lang.send(receiver, "Request_expired", new Lang.P("player", nameInviterCase));
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
            Lang.send(inviter, "§c", "Trade_Spam");
            return true;
        }

        //wait for trade compatibility check if proxyTrade == true
        boolean proxyTrade = receiver == null;
        if (!proxyTrade) {
            registerInvitation(inviter, inviter.getName(), inviter.getUniqueId(), receiver, receiverName);
            Lang.send(inviter, "Player_Is_Invited", new Lang.P("player", receiverName));
        }
        return false;
    }

    //proxy usage
    public static void registerInvitation(@Nullable Player inviter, @NotNull String nameInviter, @NotNull UUID idInviter, @Nullable Player receiver, @NotNull String nameReceiver) {
        boolean proxy = inviter == null || receiver == null;
        Invitation invitation = new Invitation(nameInviter, idInviter, proxy);

        instance().invitations
                .computeIfAbsent(nameReceiver.toLowerCase(), $ -> new HashMap<>())
                .put(nameInviter.toLowerCase(), invitation);
    }

    //proxy usage
    private static void acceptInvitation(@NotNull Player player, @Nullable Player other, @NotNull String name) {
        Lang.send(player, "Request_Accepted");
        if (other != null) {
            Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(name, other.getUniqueId(), other, player.getName(), player.getUniqueId(), player, true));

            Lang.send(other, "Request_Was_Accepted", new Lang.P("player", player.getName()));
            TradeSystem.getInstance().getTradeManager().startTrade(other, player);
        } else {
            //START PROXY
            TradeSystem.proxyHandler().send(new TradeInvitePacket(player.getName(), player.getUniqueId(), name, TradeSystem.proxy().getTradeHash(), player.getWorld().getName()), player).whenComplete((suc, t) -> {
                if (t != null) throw new RuntimeException(t);
                else {
                    if (suc.getResult() == TradeInvitePacket.Result.START_TRADING) {
                        //call event
                        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(name, suc.getRecipientId(), null, player.getName(), player.getUniqueId(), player, true)));

                        TradeSystem.getInstance().getTradeManager().startTrade(player, name, suc.getRecipientId(), suc.getWorld(), suc.getServer(), false);
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
            Lang.send(player, "No_Requests_Found");
            return null;
        }

        if (argument == null) {
            if (invitations.size() > 1) Lang.send(player, "Too_many_requests");
            else return invitations.values().stream().findAny().get();
            return null;
        } else {
            Invitation inv = invitations.get(argument.toLowerCase());
            if (inv == null) Lang.send(player, "No_Request_Found");
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

        TradeRequestPreResponseEvent e = new TradeRequestPreResponseEvent(invitation.getCaseSensitiveName(), invitation.getId(), invitation.getPlayer(), sender.getName(), sender.getUniqueId(), sender, accept);
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
                Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender.getUniqueId(), sender, TradeSystem.proxy().getCaseSensitive(invitation.getName()), invitation.getId(), null, false));

                TradeSystem.proxyHandler().send(new InviteResponsePacket(invitation.getName(), sender.getName(), sender.getUniqueId(), sender.getWorld().getName(), false, false).noFuture(), sender);
                Lang.send(sender, "Request_Denied", new Lang.P("player", invitation.getName()));
            } else Lang.send(sender, "Player_Of_Request_Not_Online");
            return;
        }

        //call event
        Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender.getUniqueId(), sender, other.getName(), other.getUniqueId(), other, false));

        Lang.send(sender, "Request_Denied", new Lang.P("player", other.getName()));
        Lang.send(other, "Request_Was_Denied", new Lang.P("player", sender.getName()));
    }

    private void accept(@NotNull Player sender, @NotNull Invitation invitation) {
        Player other = invitation.getPlayer();

        if (other == null) {
            if (TradeSystem.proxy().isOnline(invitation.getName())) {
                String name = TradeSystem.proxy().getCaseSensitive(invitation.getName());

                if (RuleManager.isViolatingRules(sender)) return;

                TradeSystem.proxyHandler().send(new InviteResponsePacket(name, sender.getName(), sender.getUniqueId(), sender.getWorld().getName(), true, false), sender, 1000).whenComplete((suc, t) -> {
                    if (t != null) {
                        if (t instanceof TimeOutException) return; // external plugin handling
                        t.printStackTrace();
                    } else {
                        if (suc.getResult() == InviteResponsePacket.Result.SUCCESS) {
                            //call event
                            Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender.getUniqueId(), sender, name, invitation.getId(), null, true)));

                            invalidate(sender, invitation);

                            Lang.send(sender, "Request_Accepted");
                            TradeSystem.getInstance().getTradeManager().startTrade(sender, name, invitation.getId(), suc.getWorld(), suc.getServer(), false);
                        } else if (suc.getResult() == InviteResponsePacket.Result.NOT_ONLINE) {
                            Lang.send(sender, "Player_Of_Request_Not_Online");
                        } else if (suc.getResult() == InviteResponsePacket.Result.OTHER_GROUP) {
                            Lang.send(sender, "Player_Of_Request_Not_Online");
                        }
                    }
                });
            } else Lang.send(sender, "Player_Of_Request_Not_Online");
            return;
        }

        if (RuleManager.isViolatingRules(sender, other)) return;

        //call event
        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender.getUniqueId(), sender, other.getName(), other.getUniqueId(), other, true)));

        invalidate(sender, invitation);

        Lang.send(sender, "Request_Accepted");
        Lang.send(other, "Request_Was_Accepted", new Lang.P("player", sender.getName()));

        TradeSystem.getInstance().getTradeManager().startTrade(other, sender);
    }

    public boolean isInvited(@NotNull Player inviter, @NotNull String receiver) {
        Map<String, Invitation> invitations = getInvitations(receiver);
        if (invitations == null) return false;
        return invitations.containsKey(inviter.getName().toLowerCase());
    }

    public static class Invitation {
        private final String name;
        private final UUID id;
        private final boolean proxyInvite;
        private final long birth;

        public Invitation(String name, UUID id, boolean proxyInvite) {
            this(name, id, proxyInvite, System.currentTimeMillis());
        }

        public Invitation(String name, UUID id, boolean proxyInvite, long birth) {
            this.name = name;
            this.id = id;
            this.proxyInvite = proxyInvite;
            this.birth = birth;
        }

        public String getName() {
            return name;
        }

        public UUID getId() {
            return id;
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
