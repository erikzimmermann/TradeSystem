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

    public TradeCMD(String... commandAliases) {
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
                return true;
            }
        }.setOnlyPlayers(true), true, commandAliases);

        //TOGGLE
        getBaseComponent().addChild(new CommandComponent("toggle") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                return toggle(sender);
            }
        });

        //ACCEPT
        getBaseComponent().addChild(new CommandComponent("accept") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                return accept(sender);
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
                return TradeCMD.this.accept(sender, argument);
            }
        });

        //DENY
        getBaseComponent().addChild(new CommandComponent("deny") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                return deny(sender);
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
                return TradeCMD.this.deny(sender, argument);
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
                return true;
            }
        });
    }

    private boolean deny(CommandSender sender, String argument) {
        Set<Invite> l = invites.get(sender.getName());

        if (l != null && l.remove(new Invite(argument))) {
            Player other = Bukkit.getPlayer(argument);
            if (l.isEmpty()) invites.remove(sender.getName());

            if (other == null) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                return true;
            }

            sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", (Player) sender).replace("%player%", other.getName()));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied", (Player) sender).replace("%player%", sender.getName()));
        } else {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found", (Player) sender));
        }

        return true;
    }

    private boolean deny(CommandSender sender) {
        Set<Invite> l = invites.get(sender.getName());

        if (l == null || l.isEmpty()) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found", (Player) sender));
        } else if (l.size() == 1) {
            Player other = Bukkit.getPlayer(l.stream().findAny().get().getName());

            if (other == null) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                return true;
            }

            sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied", (Player) sender).replace("%player%", other.getName()));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied", (Player) sender).replace("%player%", sender.getName()));
        } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests", (Player) sender));
        return true;
    }

    private boolean accept(CommandSender sender, String argument) {
        if (checkForAccept(sender)) return true;

        Set<Invite> l = invites.get(sender.getName());

        if (l != null && l.remove(new Invite(argument))) {
            Player other = Bukkit.getPlayer(argument);
            if (l.isEmpty()) invites.remove(sender.getName());

            if (other == null) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                return true;
            }

            sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", (Player) sender));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", (Player) sender).replace("%player%", sender.getName()));

            TradeSystem.getInstance().getTradeManager().startTrade((Player) sender, other);
        } else {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found", (Player) sender));
        }

        return true;
    }

    private boolean checkForAccept(CommandSender sender) {
        if (((Player) sender).isSleeping()) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed", (Player) sender));
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(((Player) sender).getWorld())) {
            sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world", (Player) sender));
            return true;
        }
        return false;
    }

    private boolean accept(CommandSender sender) {
        if (checkForAccept(sender)) return true;

        Set<Invite> l = invites.get(sender.getName());

        if (l == null || l.isEmpty()) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found", (Player) sender));
        } else if (l.size() == 1) {
            Player other = Bukkit.getPlayer(l.stream().findAny().get().getName());

            if (other == null) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online", (Player) sender));
                return true;
            }

            sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", (Player) sender));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", (Player) sender).replace("%player%", sender.getName()));

            TradeSystem.getInstance().getTradeManager().startTrade((Player) sender, other);
        } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests", (Player) sender));
        return true;
    }

    private boolean toggle(CommandSender sender) {
        if (TradeSystem.getInstance().getTradeManager().toggle((Player) sender)) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Offline", (Player) sender));
            invites.remove(sender.getName());
        } else {
            sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Online", (Player) sender));
        }
        return true;
    }

    public static void request(Player p, Player other) {
        if (checkRules(p, other)) return;
        requestFinalTrade(p, other);
    }

    private static boolean checkRules(Player p, Player other) {
        if (PERMISSION != null && !p.hasPermission(PERMISSION)) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Not_Able_To_Trade", p));
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(p)) {
            notifyOfflinePlayer(p);
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(p.getWorld())) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world", p));
            return true;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(p.getGameMode().name())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_that_GameMode", p));
            return true;
        }

        if (p.isSleeping()) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed", p));
            return true;
        }

        if (other == null) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online", p));
            return true;
        }

        if (other.equals(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Trade_With_Yourself", p));
            return true;
        }

        if (!other.canSee(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_while_invisible", p));
            return true;
        }

        if (!p.canSee(other)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online", p));
            return true;
        }

        if (PERMISSION != null && !other.hasPermission(PERMISSION)) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_Is_Not_Able_Trade", p));
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(other.getWorld())) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Other_cannot_trade_in_world", p));
            return true;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(other.getGameMode().name())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Other_cannot_trade_in_that_GameMode", p));
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(other)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_is_Offline", p));
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().getDistance() > 0) {
            if (!p.getWorld().equals(other.getWorld()) || p.getLocation().distance(other.getLocation()) > TradeSystem.getInstance().getTradeManager().getDistance()) {
                p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_is_not_in_range", p).replace("%player%", other.getName()));
                return true;
            }
        }

        return false;
    }

    private static void requestFinalTrade(Player player, Player recipient) {
        if (checkInvitation(player, recipient)) return;

        sendRequest(player, recipient);
        player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Invited", player).replace("%player%", recipient.getName()));

        TradeSystem.getInstance().getTradeManager().playRequestSound(recipient);
    }

    @SuppressWarnings ("deprecation")
    private static void sendRequest(Player player, Player recipient) {
        TextComponent base = new TextComponent(TextComponent.fromLegacyText(Lang.getPrefix() + Lang.get("Want_To_Trade", player).replace("%player%", player.getName())));
        base.setColor(ChatColor.GRAY);

        SimpleMessage message = new SimpleMessage(recipient, base, TradeSystem.getInstance());

        TextComponent accept = new TextComponent(Lang.get("Want_To_Trade_Accept", recipient));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + player.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", recipient))}));

        TextComponent deny = new TextComponent(Lang.get("Want_To_Trade_Deny", recipient));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + player.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", recipient))}));

        message.replace("%accept%", accept);
        message.replace("%deny%", deny);

        message.send();
    }

    private static boolean checkInvitation(Player player, Player other) {
        TimeSet<Invite> l = TradeSystem.getInstance().getTradeCMD().getInvites().get(player.getName());
        boolean invited = l != null && l.remove(new Invite(other.getName()));

        if (invited) {
            acceptInvitation(player, other, l);
            return true;
        }

        l = TradeSystem.getInstance().getTradeCMD().getInvites().get(other.getName());
        boolean alreadyRequested = l != null && l.contains(new Invite(player.getName()));

        if (alreadyRequested) {
            player.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Trade_Spam", player));
            return true;
        }

        registerExpiration(player, other, l);
        return false;
    }

    private static void registerExpiration(Player player, Player other, TimeSet<Invite> l) {
        if (l == null) l = new TimeSet<Invite>() {
            @Override
            public void timeout(Invite i) {
                player.sendMessage(Lang.getPrefix() + Lang.get("Your_request_epired", player).replace("%player%", other.getName()));
                other.sendMessage(Lang.getPrefix() + Lang.get("Request_expired", other).replace("%player%", player.getName()));
            }
        };

        long expiration = TradeSystem.getInstance().getTradeManager().getRequestExpirationTime() * 1000;
        l.add(new Invite(player.getName()), expiration);
        TradeSystem.getInstance().getTradeCMD().getInvites().put(other.getName(), l, expiration);
    }

    private static void acceptInvitation(Player player, Player other, TimeSet<Invite> l) {
        if (l.isEmpty()) TradeSystem.getInstance().getTradeCMD().getInvites().remove(player.getName());

        player.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted", player));
        other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted", player).replace("%player%", player.getName()));

        TradeSystem.getInstance().getTradeManager().startTrade(player, other);
    }

    private static void notifyOfflinePlayer(Player p) {
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
