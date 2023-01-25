package de.codingair.tradesystem.spigot.trade.managers;

import de.codingair.codingapi.tools.time.TimeMap;
import de.codingair.codingapi.tools.time.TimeSet;
import de.codingair.tradesystem.proxy.packets.InviteResponsePacket;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.proxy.packets.TradeStateUpdatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeRequestExpireEvent;
import de.codingair.tradesystem.spigot.events.TradeRequestResponseEvent;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

public class InvitationManager {
    /**
     * receiver to invitations from others
     */
    private final TimeMap<String, TimeSet<Invite>> invites = new TimeMap<>();

    private static InvitationManager instance() {
        return TradeSystem.invitations();
    }

    //proxy usage
    public static boolean registerInvitation(Player player, @Nullable Player other, @NotNull String name) {
        TimeSet<Invite> l = instance().invites.get(player.getName());
        boolean invited = l != null && l.remove(new Invite(name));

        if (invited) {
            acceptInvitation(player, other, name, l);
            return true;
        }

        l = instance().invites.get(name);
        boolean alreadyRequested = l != null && l.contains(new Invite(player.getName()));

        if (alreadyRequested) {
            player.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Trade_Spam", player));
            return true;
        }

        //wait for trade compatibility check if proxyTrade == true
        boolean proxyTrade = other == null;
        if (!proxyTrade) {
            registerExpiration(player, player.getName(), other, name);
            player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Invited", player, new Lang.P("player", name)));
        }
        return false;
    }

    //proxy usage
    public static void registerExpiration(@Nullable Player inviter, @NotNull String nameInviter, @Nullable Player receiver, @NotNull String nameReceiver) {
        TimeSet<Invite> l = instance().invites.get(nameReceiver);
        if (l == null) l = new TimeSet<Invite>() {
            @Override
            public void timeout(Invite i) {
                String nameInviter = i.getName();
                Player inviter = Bukkit.getPlayer(nameInviter);
                Player receiver = Bukkit.getPlayer(nameReceiver);

                Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
                    TradeRequestExpireEvent event = new TradeRequestExpireEvent(nameInviter, inviter, nameReceiver, receiver);
                    Bukkit.getPluginManager().callEvent(event);
                });

                if (inviter != null) inviter.sendMessage(Lang.getPrefix() + Lang.get("Your_request_expired", inviter, new Lang.P("player", nameReceiver)));
                if (receiver != null) receiver.sendMessage(Lang.getPrefix() + Lang.get("Request_expired", receiver, new Lang.P("player", nameInviter)));
            }
        };

        long expiration = TradeSystem.man().getRequestExpirationTime() * 1000L;
        boolean proxy = inviter == null || receiver == null;

        l.add(new Invite(nameInviter, proxy), expiration);
        instance().invites.put(nameReceiver, l, expiration);
    }

    //proxy usage
    private static void acceptInvitation(@NotNull Player player, @Nullable Player other, @NotNull String name, TimeSet<Invite> l) {
        if (l.isEmpty()) instance().clear(player.getName());

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
    public Set<Invite> getInvites(String name) {
        return this.invites.get(name);
    }

    @Nullable
    public TimeSet<Invite> clear(String name) {
        TimeSet<Invite> set = this.invites.remove(name);
        if (set != null) set.unregister();
        return set;
    }

    private boolean invalidateIfEmpty(@NotNull String name, @Nullable TimeSet<Invite> set) {
        if (set == null) set = this.invites.get(name);
        if (set != null && set.isEmpty()) {
            this.clear(name);
            return true;
        } else return false;
    }

    public void cancelAll(@Nullable Player player, @NotNull String name) {
        //cancel all open invitations on proxy too
        TimeSet<Invite> received = clear(name);
        if (received != null) {
            for (Invite invite : received) {
                if (invite.isProxyInvite()) cancel(player, invite.getName(), name);
            }
        }

        Invite invite = new Invite(name);

        this.invites.entrySet().removeIf(e -> {
            String receiver = e.getKey();
            TimeSet<Invite> invites = e.getValue();

            //get original invitation to check if it's a proxy invite
            invites.stream().filter(i -> i.equals(invite)).findFirst().ifPresent(original -> {
                //cancel on proxy level
                if (original.isProxyInvite()) cancel(player, name, receiver);

                //remove it when an invitation is available
                invites.remove(invite);
            });

            return invalidateIfEmpty(receiver, invites);
        });
    }

    private void cancel(@Nullable Player player, @NotNull String sender, @NotNull String receiver) {
        TradeStateUpdatePacket packet = new TradeStateUpdatePacket(sender, receiver, TradeStateUpdatePacket.State.CANCELLED, null);
        TradeSystem.proxyHandler().send(packet, player);
    }

    public void clear() {
        this.invites.clear();
    }

    public void invalidate(Player inviter, String other) {
        TimeSet<Invite> l = invites.get(other);
        if (l != null) {
            l.remove(new Invite(inviter.getName()));
            invalidateIfEmpty(other, l);
        }
    }

    public boolean deny(CommandSender sender, String argument) {
        TimeSet<Invite> l = invites.get(sender.getName());

        if (l != null && l.remove(new Invite(argument))) {
            Player other = Bukkit.getPlayer(argument);
            invalidateIfEmpty(sender.getName(), l);

            if (other == null) {
                if (TradeSystem.proxy().isOnline(argument)) {
                    //call event
                    Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), (Player) sender, TradeSystem.proxy().getCaseSensitive(argument), null, false));

                    TradeSystem.proxyHandler().send(new InviteResponsePacket(argument, sender.getName(), false, false).noFuture(), (Player) sender);
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", (Player) sender, new Lang.P("player", argument)));
                } else sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                return true;
            }

            //call event
            Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), (Player) sender, other.getName(), other, false));

            sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", (Player) sender, new Lang.P("player", other.getName())));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied", (Player) sender, new Lang.P("player", sender.getName())));
        } else {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found", (Player) sender));
        }

        return true;
    }

    public boolean accept(Player sender, String argument) {
        TimeSet<Invite> l = invites.get(sender.getName());
        Invite dummy = new Invite(argument);

        if (l != null && l.contains(dummy)) {
            Player other = Bukkit.getPlayerExact(argument);

            if (other == null) {
                String name = TradeSystem.proxy().getCaseSensitive(argument);

                if (TradeSystem.proxy().isOnline(argument)) {
                    if (RuleManager.isViolatingRules(sender)) return true;

                    TradeSystem.proxyHandler().send(new InviteResponsePacket(argument, sender.getName(), true, false), sender).whenComplete((suc, t) -> {
                        if (t != null) t.printStackTrace();
                        else {
                            if (suc.getResult() == InviteResponsePacket.Result.SUCCESS) {
                                //call event
                                Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender, name, null, true)));

                                l.remove(dummy);
                                invalidateIfEmpty(sender.getName(), l);

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
                return true;
            }

            if (RuleManager.isViolatingRules(sender, other)) return true;

            //call event
            Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> Bukkit.getPluginManager().callEvent(new TradeRequestResponseEvent(sender.getName(), sender, other.getName(), other, true)));

            l.remove(dummy);
            invalidateIfEmpty(sender.getName(), l);

            sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", sender));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", sender, new Lang.P("player", sender.getName())));

            TradeSystem.getInstance().getTradeManager().startTrade(other, sender, sender.getName(), true);
        } else {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found", sender));
        }

        return true;
    }

    public boolean accept(Player sender) {
        Set<Invite> l = invites.get(sender.getName());

        if (l == null || l.isEmpty()) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found", sender));
        } else if (l.size() == 1) {
            String other = l.stream().findAny().get().getName();

            return accept(sender, other);
        } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests", sender));
        return true;
    }

    public boolean isInvited(Player inviter, String receiver) {
        Set<Invite> l = getInvites(receiver);
        if (l == null) return false;
        return l.contains(new Invite(inviter.getName()));
    }

    public static class Invite {
        private final String name;
        private final boolean proxyInvite;

        public Invite(String name) {
            this(name, false);
        }

        public Invite(String name, boolean proxyInvite) {
            this.name = name;
            this.proxyInvite = proxyInvite;
        }

        public String getName() {
            return name;
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

            Invite invite = (Invite) o;
            return this.name.equalsIgnoreCase(invite.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name.toLowerCase());
        }
    }
}
