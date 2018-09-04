package de.codingair.tradesystem.trade.commands;

import de.codingair.codingapi.server.commands.BaseComponent;
import de.codingair.codingapi.server.commands.CommandBuilder;
import de.codingair.codingapi.server.commands.CommandComponent;
import de.codingair.codingapi.server.commands.MultiCommandComponent;
import de.codingair.codingapi.tools.time.TimeMap;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.utils.Lang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TradeCMD extends CommandBuilder {
    private static String PERMISSION = "TradeSystem.Trade";
    private TimeMap<String, List<String>> invites = new TimeMap<>();

    public TradeCMD() {
        super("Trade", new BaseComponent(PERMISSION) {
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

        //ACCEPT
        getBaseComponent().addChild(new CommandComponent("accept") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_How_To_Accept"));
                return false;
            }
        });

        getComponent("accept").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                List<String> l = invites.get(sender.getName());
                if(l == null) return;
                suggestions.addAll(l);
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                List<String> l = invites.get(sender.getName());
                if(l == null) return false;

                if(l.contains(argument)) {
                    Player other = Bukkit.getPlayer(argument);

                    if(other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online"));
                        return false;
                    }

                    l.remove(argument);

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
                sender.sendMessage(Lang.getPrefix() + Lang.get("Command_How_To_Deny"));
                return false;
            }
        });

        getComponent("deny").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                List<String> l = invites.get(sender.getName());
                if(l == null) return;
                suggestions.addAll(l);
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                List<String> l = invites.get(sender.getName());
                if(l == null) return false;

                if(l.contains(argument)) {
                    Player other = Bukkit.getPlayer(argument);

                    if(other == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Of_Request_Not_Online"));
                        return false;
                    }

                    l.remove(argument);

                    sender.sendMessage(Lang.getPrefix() + Lang.get("Request_Denied"));
                    other.sendMessage(Lang.getPrefix() + Lang.get("Request_Was_Denied"));
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("No_Request_Found"));
                }

                return false;
            }
        });

        //INVITE
        getBaseComponent().addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.getName().equals(sender.getName())) continue;
                    suggestions.add(player.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if(argument.equals(sender.getName())) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Trade_With_Yourself"));
                    return false;
                }

                if(Bukkit.getPlayer(argument) == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online"));
                    return false;
                }

                if(!Bukkit.getPlayer(argument).hasPermission(PERMISSION)) {
                    sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_Is_Not_Able_Trade"));
                    return false;
                }

                List<String> l = invites.remove(argument);
                if(l == null) l = new ArrayList<>();

                l.add(sender.getName());
                invites.put(argument, l, 60);


                List<TextComponent> parts = new ArrayList<>();

                TextComponent accept = new TextComponent(Lang.get("Want_To_Trade_Accept"));
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + sender.getName()));
                accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover"))}));

                TextComponent deny = new TextComponent(Lang.get("Want_To_Trade_Deny"));
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + sender.getName()));
                deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover"))}));

                String s = Lang.getPrefix() + Lang.get("Want_To_Trade").replace("%PLAYER%", sender.getName());

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

                for(TextComponent part : parts) {
                    basic.addExtra(part);
                }

                System.out.println(basic.toLegacyText());

                Bukkit.getPlayer(argument).spigot().sendMessage(basic);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Invited").replace("%PLAYER%", Bukkit.getPlayer(argument).getName()));

                return false;
            }
        });
    }
}
