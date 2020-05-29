package de.codingair.tradesystem.trade.commands;

import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.tools.time.TimeList;
import de.codingair.codingapi.tools.time.TimeMap;
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

import java.util.ArrayList;
import java.util.List;

public class TradeCMD extends CommandBuilder {
    public static String PERMISSION = "TradeSystem.Trade";
    public static String PERMISSION_INITIATE = "TradeSystem.Trade.Initiate";
    private TimeMap<String, TimeList<Invite>> invites = new TimeMap<>();

    public TradeCMD() {
        super("trade", "Trade-System-CMD", new BaseComponent(PERMISSION) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Not_Able_To_Trade"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + "§cOnly for players!");
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_How_To"));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_How_To"));
                return false;
            }
        }.setOnlyPlayers(true), true);

        //TOGGLE
        getBaseComponent().addChild(new CommandComponent("toggle") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                if(TradeSystem.getInstance().getTradeManager().toggle((Player) sender)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Offline"));
                    invites.remove(sender.getName());
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Online"));
                }
                return false;
            }
        });

        //ACCEPT
        getBaseComponent().addChild(new CommandComponent("accept") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                if(((Player) sender).isSleeping()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed"));
                    return false;
                }

                if(TradeSystem.getInstance().getTradeManager().isBlockedWorld(((Player) sender).getWorld())) {
                    sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world"));
                    return false;
                }

                List<Invite> l = invites.get(sender.getName());

                if(l == null || l.isEmpty()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found"));
                } else if(l.size() == 1) {
                    Player other = Bukkit.getPlayer(l.remove(0).getName());

                    if(other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online"));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted"));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted").replace("%PLAYER%", sender.getName()));

                    TradeSystem.getInstance().getTradeManager().startTrade((Player) sender, other);
                } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests"));
                return false;
            }
        });

        getComponent("accept").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                List<Invite> l = invites.get(sender.getName());
                if(l == null) return;
                for(Invite invite : l) {
                    suggestions.add(invite.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if(((Player) sender).isSleeping()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed"));
                    return false;
                }

                if(TradeSystem.getInstance().getTradeManager().isBlockedWorld(((Player) sender).getWorld())) {
                    sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world"));
                    return false;
                }

                List<Invite> l = invites.get(sender.getName());

                if(l != null && l.contains(new Invite(argument))) {
                    Player other = Bukkit.getPlayer(argument);
                    l.remove(new Invite(argument));

                    if(other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online"));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted"));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted").replace("%PLAYER%", sender.getName()));

                    TradeSystem.getInstance().getTradeManager().startTrade((Player) sender, other);
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found"));
                }

                return false;
            }
        });

        //DENY
        getBaseComponent().addChild(new CommandComponent("deny") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                List<Invite> l = invites.get(sender.getName());

                if(l == null || l.isEmpty()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found"));
                } else if(l.size() == 1) {
                    Player other = Bukkit.getPlayer(l.remove(0).getName());

                    if(other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online"));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied").replace("%PLAYER%", other.getName()));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied").replace("%PLAYER%", sender.getName()));
                } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests"));
                return false;
            }
        });

        getComponent("deny").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                List<Invite> l = invites.get(sender.getName());
                if(l == null) return;
                for(Invite invite : l) {
                    suggestions.add(invite.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                List<Invite> l = invites.get(sender.getName());

                if(l != null && l.contains(new Invite(argument))) {
                    Player other = Bukkit.getPlayer(argument);
                    l.remove(new Invite(argument));

                    if(other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online"));
                        return false;
                    }

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied").replace("%PLAYER%", other.getName()));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied").replace("%PLAYER%", sender.getName()));
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found"));
                }

                return false;
            }
        });

        //INVITE
        getBaseComponent().addChild(new MultiCommandComponent(PERMISSION_INITIATE) {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.getName().equals(sender.getName()) || !((Player) sender).canSee(player)) continue;

                    TimeList<Invite> l = invites.get(player.getName());
                    if(l != null && l.contains(new Invite(sender.getName()))) continue;
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
        if(PERMISSION != null && !p.hasPermission(PERMISSION)) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Not_Able_To_Trade"));
            return;
        }

        if(TradeSystem.getInstance().getTradeManager().isOffline(p)) {
            String[] a = Lang.get("Trade_You_are_Offline").split("%COMMAND%", -1);

            String s0 = a[0];
            String s = a[1];
            String s1 = a[2];

            SimpleMessage message = new SimpleMessage(Lang.getPrefix() + s0, TradeSystem.getInstance());
            message.add(new ChatButton(s, Lang.get("Want_To_Trade_Hover")) {
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

        if(TradeSystem.getInstance().getTradeManager().isBlockedWorld(p.getWorld())) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world"));
            return;
        }

        if(!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(p.getGameMode().name())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_that_GameMode"));
            return;
        }

        if(p.isSleeping()) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed"));
            return;
        }

        if(other == null) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online"));
            return;
        }

        if(other.getName().equals(p.getName())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Trade_With_Yourself"));
            return;
        }

        if(PERMISSION != null && !other.hasPermission(PERMISSION)) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_Is_Not_Able_Trade"));
            return;
        }

        if(TradeSystem.getInstance().getTradeManager().isBlockedWorld(other.getWorld())) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Other_cannot_trade_in_world"));
            return;
        }

        if(!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(other.getGameMode().name())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Other_cannot_trade_in_that_GameMode"));
            return;
        }

        if(!other.canSee(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_while_invisible"));
            return;
        }

        if(!p.canSee(other)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online"));
            return;
        }

        if(TradeSystem.getInstance().getTradeManager().isOffline(other)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_is_Offline"));
            return;
        }

        if(TradeSystem.getInstance().getTradeManager().getDistance() > 0) {
            if(!p.getWorld().equals(other.getWorld()) || p.getLocation().distance(other.getLocation()) > TradeSystem.getInstance().getTradeManager().getDistance()) {
                p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_is_not_in_range").replace("%PLAYER%", other.getName()));
                return;
            }
        }

        TimeList<Invite> l = TradeSystem.getInstance().getTradeCMD().getInvites().get(p.getName());
        if(l != null && l.contains(new Invite(other.getName()))) {
            l.remove(new Invite(other.getName()));

            p.sendMessage(Lang.getPrefix() + Lang.get("Request_Accepted"));
            other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Accepted").replace("%PLAYER%", p.getName()));

            TradeSystem.getInstance().getTradeManager().startTrade(p, other);
            return;
        }

        l = TradeSystem.getInstance().getTradeCMD().getInvites().get(other.getName());
        if(l != null && l.contains(new Invite(p.getName()))) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Trade_Spam"));
            return;
        }

        if(l == null) l = new TimeList<>();
        l.add(new Invite(p.getName()), TradeSystem.getInstance().getTradeManager().getCooldown());
        TradeSystem.getInstance().getTradeCMD().getInvites().put(other.getName(), l, TradeSystem.getInstance().getTradeManager().getCooldown());

        List<TextComponent> parts = new ArrayList<>();

        TextComponent accept = new TextComponent(Lang.get("Want_To_Trade_Accept"));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + p.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover"))}));

        TextComponent deny = new TextComponent(Lang.get("Want_To_Trade_Deny"));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + p.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover"))}));

        String s = Lang.getPrefix() + Lang.get("Want_To_Trade").replace("%PLAYER%", p.getName());

        String[] a1 = s.split("%ACCEPT%");
        if(a1[0].contains("%DENY%")) {
            String[] a2 = a1[0].split("%DENY%");
            parts.add(new TextComponent(a2[0]));
            parts.add(deny);
            parts.add(new TextComponent(a2[1]));
            parts.add(accept);
            parts.add(new TextComponent(a1[1]));
        } else {
            parts.add(new TextComponent(a1[0]));
            parts.add(accept);

            String[] a2 = a1[1].split("%DENY%");
            parts.add(new TextComponent(a2[0]));
            parts.add(deny);
            parts.add(new TextComponent(a2[1]));
        }

        TextComponent basic = new TextComponent("");
        basic.setColor(ChatColor.GRAY);

        for(TextComponent part : parts) {
            basic.addExtra(part);
        }

        other.spigot().sendMessage(basic);
        p.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Invited").replace("%PLAYER%", other.getName()));

        TradeSystem.getInstance().getTradeManager().playRequestSound(other);
    }

    public TimeMap<String, TimeList<Invite>> getInvites() {
        return invites;
    }

    public void removesAllInvitesFrom(Player player) {
        getInvites().forEach((other, invites) -> invites.remove(new Invite(player.getName())));
    }
}
