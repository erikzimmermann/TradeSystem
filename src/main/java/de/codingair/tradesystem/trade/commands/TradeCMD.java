package de.codingair.tradesystem.trade.commands;

import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.tools.time.TimeMap;
import de.codingair.codingapi.tools.time.TimeSet;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.utils.Invite;
import de.codingair.tradesystem.utils.Lang;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class TradeCMD extends CommandBuilder {
    public static String PERMISSION = "TradeSystem.Trade";
    public static String PERMISSION_INITIATE = "TradeSystem.Trade.Initiate";
    private final TimeMap<String, TimeSet<Invite>> invites = new TimeMap<>();

    public TradeCMD() {
        super(TradeSystem.getInstance(), "trade", "Trade-System-CMD", new BaseComponent(PERMISSION) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Not_Able_To_Trade", (Player) sender));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + "§cOnly for players!");
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_How_To", (Player) sender));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_How_To", (Player) sender));
                return false;
            }
        }.setOnlyPlayers(true), true);

        //TOGGLE
        getBaseComponent().addChild(new CommandComponent("toggle") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                if (TradeSystem.getInstance().getTradeManager().toggle((Player) sender)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Offline", (Player) sender));
                    invites.remove(sender.getName());
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Online", (Player) sender));
                }
                return false;
            }
        });

        //ACCEPT
        getBaseComponent().addChild(new CommandComponent("accept") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                if (((Player) sender).isSleeping()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed", (Player) sender));
                    return false;
                }

                if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(((Player) sender).getWorld())) {
                    sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world", (Player) sender));
                    return false;
                }

                Set<Invite> l = invites.get(sender.getName());

                if (l == null || l.isEmpty()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found", (Player) sender));
                } else if (l.size() == 1) {
                    Player other = Bukkit.getPlayer(l.stream().findAny().get().getName());

                    if (other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", (Player) sender));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", (Player) sender).replace("%player%", sender.getName()));

                    TradeSystem.getInstance().getTradeManager().startTrade((Player) sender, other);
                } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests", (Player) sender));
                return false;
            }
        });

        getComponent("accept").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                Set<Invite> l = invites.get(sender.getName());
                if (l == null) return;
                for (Invite invite : l) {
                    suggestions.add(invite.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if (((Player) sender).isSleeping()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed", (Player) sender));
                    return false;
                }

                if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(((Player) sender).getWorld())) {
                    sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world", (Player) sender));
                    return false;
                }

                Set<Invite> l = invites.get(sender.getName());

                if (l != null && l.remove(new Invite(argument))) {
                    Player other = Bukkit.getPlayer(argument);
                    if (l.isEmpty()) invites.remove(sender.getName());

                    if (other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", (Player) sender));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", (Player) sender).replace("%player%", sender.getName()));

                    TradeSystem.getInstance().getTradeManager().startTrade((Player) sender, other);
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found", (Player) sender));
                }

                return false;
            }
        });

        //DENY
        getBaseComponent().addChild(new CommandComponent("deny") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Set<Invite> l = invites.get(sender.getName());

                if (l == null || l.isEmpty()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found", (Player) sender));
                } else if (l.size() == 1) {
                    Player other = Bukkit.getPlayer(l.stream().findAny().get().getName());

                    if (other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", (Player) sender).replace("%player%", other.getName()));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied", (Player) sender).replace("%player%", sender.getName()));
                } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests", (Player) sender));
                return false;
            }
        });

        getComponent("deny").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                Set<Invite> l = invites.get(sender.getName());
                if (l == null) return;
                for (Invite invite : l) {
                    suggestions.add(invite.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Set<Invite> l = invites.get(sender.getName());

                if (l != null && l.remove(new Invite(argument))) {
                    Player other = Bukkit.getPlayer(argument);
                    if (l.isEmpty()) invites.remove(sender.getName());

                    if (other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", (Player) sender).replace("%player%", other.getName()));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied", (Player) sender).replace("%player%", sender.getName()));
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found", (Player) sender));
                }

                return false;
            }
        });

        //INVITE
        getBaseComponent().addChild(new MultiCommandComponent(PERMISSION_INITIATE) {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().equals(sender.getName()) || !((Player) sender).canSee(player)) continue;

                    Set<Invite> l = invites.get(player.getName());
                    if (l != null && l.contains(new Invite(sender.getName()))) continue;
                    suggestions.add(player.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                request((Player) sender, Bukkit.getPlayer(argument));
                return false;
            }
        });
    }

    public static void request(Player p, Player other) {
        if (PERMISSION != null && !p.hasPermission(PERMISSION)) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Not_Able_To_Trade", p));
            return;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(p)) {
            String[] a = Lang.get("Trade_You_are_Offline", p).split("%command%", -1);

            String s0 = a[0];
            String s = a[1];
            String s1 = a[2];

            SimpleMessage message = new SimpleMessage(Lang.getPrefix() + s0, TradeSystem.getInstance());
            message.add(new ChatButton(s, Lang.get("Want_To_Trade_Hover", p)) {
                @Override
                public void onClick(Player player) {
                    p.performCommand("trade toggle");
                    message.destroy();
                }
            }.setType("TRADE_TOGGLE"));

            message.setTimeOut(10);
            message.add(new TextComponent(s1));
            message.send(p);
            return;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(p.getWorld())) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world", p));
            return;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(p.getGameMode().name())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_that_GameMode", p));
            return;
        }

        if (p.isSleeping()) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed", p));
            return;
        }

        if (other == null) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online", p));
            return;
        }

        if (other.equals(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Trade_With_Yourself", p));
            return;
        }

        if (!other.canSee(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_while_invisible", p));
            return;
        }

        if (!p.canSee(other)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online", p));
            return;
        }

        if (PERMISSION != null && !other.hasPermission(PERMISSION)) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_Is_Not_Able_Trade", p));
            return;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(other.getWorld())) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Other_cannot_trade_in_world", p));
            return;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(other.getGameMode().name())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Other_cannot_trade_in_that_GameMode", p));
            return;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(other)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_is_Offline", p));
            return;
        }

        if (TradeSystem.getInstance().getTradeManager().getDistance() > 0) {
            if (!p.getWorld().equals(other.getWorld()) || p.getLocation().distance(other.getLocation()) > TradeSystem.getInstance().getTradeManager().getDistance()) {
                p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_is_not_in_range", p).replace("%player%", other.getName()));
                return;
            }
        }

        TimeSet<Invite> l = TradeSystem.getInstance().getTradeCMD().getInvites().get(p.getName());
        if (l != null && l.remove(new Invite(other.getName()))) {
            if (l.isEmpty()) TradeSystem.getInstance().getTradeCMD().getInvites().remove(p.getName());

            p.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", p));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", p).replace("%player%", p.getName()));

            TradeSystem.getInstance().getTradeManager().startTrade(p, other);
            return;
        }

        l = TradeSystem.getInstance().getTradeCMD().getInvites().get(other.getName());
        if (l != null && l.contains(new Invite(p.getName()))) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Trade_Spam", p));
            return;
        }

        if (l == null) l = new TimeSet<Invite>() {
            @Override
            public void timeout(Invite i) {
                p.sendMessage(Lang.getPrefix() + Lang.get("Your_request_epired", p).replace("%player%", other.getName()));
                other.sendMessage(Lang.getPrefix() + Lang.get("Request_expired", other).replace("%player%", p.getName()));
            }
        };

        l.add(new Invite(p.getName()), (long) TradeSystem.getInstance().getTradeManager().getRequestExpirationTime() * 1000L);
        TradeSystem.getInstance().getTradeCMD().getInvites().put(other.getName(), l, TradeSystem.getInstance().getTradeManager().getRequestExpirationTime() * 1000);

        TextComponent base = new TextComponent(Lang.getPrefix() + Lang.get("Want_To_Trade", p).replace("%player%", p.getName()));
        base.setColor(ChatColor.GRAY);

        SimpleMessage message = new SimpleMessage(other, base, TradeSystem.getInstance());

        TextComponent accept = new TextComponent(Lang.get("Want_To_Trade_Accept", other));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + p.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", other))}));

        TextComponent deny = new TextComponent(Lang.get("Want_To_Trade_Deny", other));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + p.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", other))}));

        message.replace("%accept%", accept);
        message.replace("%deny%", deny);

        message.send();
        p.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Invited", p).replace("%player%", other.getName()));

        TradeSystem.getInstance().getTradeManager().playRequestSound(other);
    }

    public TimeMap<String, TimeSet<Invite>> getInvites() {
        return invites;
    }

    public void removesInvitesWith(Player player) {
        invites.entrySet().removeIf(e -> {
            if (e.getKey().equals(player.getName())) return true;

            Set<Invite> invites = e.getValue();
            if (invites == null) return true;

            invites.remove(new Invite(player.getName()));
            return invites.isEmpty();
        });
    }
}
