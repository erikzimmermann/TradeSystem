package de.codingair.tradesystem.spigot.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Invite;
import de.codingair.tradesystem.spigot.trade.managers.CommandManager;
import de.codingair.tradesystem.spigot.trade.managers.RequestManager;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TradeCMD extends CommandBuilder {
    public TradeCMD(String[] aliases, CommandManager commandManager) {
        super(TradeSystem.getInstance(), aliases[0], "Trade-System-CMD", new BaseComponent(Permissions.PERMISSION) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Not_Able_To_Trade", (Player) sender));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Only_for_Player"));
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
        }.setOnlyPlayers(true), true, Arrays.copyOfRange(aliases, 1, aliases.length));

        //TOGGLE
        for (String cmd : commandManager.getToggleAliases()) {
            getBaseComponent().addChild(new CommandComponent(cmd) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    return toggle(sender);
                }
            });
        }

        //ACCEPT
        for (String cmd : commandManager.getAcceptAliases()) {
            getBaseComponent().addChild(new CommandComponent(cmd) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    return TradeSystem.invitations().accept((Player) sender);
                }
            });

            getComponent(cmd).addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    Set<Invite> l = TradeSystem.invitations().getInvites(sender.getName());
                    if (l == null) return;
                    for (Invite invite : l) {
                        suggestions.add(invite.getName());
                    }
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    return TradeSystem.invitations().accept((Player) sender, argument);
                }
            });
        }


        //DENY
        for (String cmd : commandManager.getDenyAliases()) {
            getBaseComponent().addChild(new CommandComponent(cmd) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    return deny(sender);
                }
            });

            getComponent(cmd).addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    Set<Invite> l = TradeSystem.invitations().getInvites(sender.getName());
                    if (l == null) return;
                    for (Invite invite : l) {
                        suggestions.add(invite.getName());
                    }
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    return TradeSystem.invitations().deny(sender, argument);
                }
            });
        }


        //INVITE
        getBaseComponent().addChild(new MultiCommandComponent(Permissions.PERMISSION_INITIATE) {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().equals(sender.getName()) || !((Player) sender).canSee(player)) continue;

                    Set<Invite> l = TradeSystem.invitations().getInvites(player.getName());
                    if (l != null && l.contains(new Invite(sender.getName()))) continue;
                    suggestions.add(player.getName());
                }

                TradeSystem.proxy().getPlayers(sender).forEach(player -> {
                    Set<Invite> l = TradeSystem.invitations().getInvites(player);
                    if (l != null && l.contains(new Invite(sender.getName()))) return;

                    suggestions.add(player);
                });
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                RequestManager.request((Player) sender, argument);
                return true;
            }
        });
    }

    private boolean deny(CommandSender sender) {
        Set<Invite> l = TradeSystem.invitations().getInvites(sender.getName());

        if (l == null || l.isEmpty()) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("No_Requests_Found", (Player) sender));
        } else if (l.size() == 1) {
            String other = l.stream().findAny().get().getName();
            return TradeSystem.invitations().deny(sender, other);
        } else sender.sendMessage(Lang.getPrefix() + Lang.get("Too_many_requests", (Player) sender));
        return true;
    }

    private boolean toggle(CommandSender sender) {
        if (TradeSystem.getInstance().getTradeManager().toggle((Player) sender)) {
            sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Offline", (Player) sender));
            TradeSystem.invitations().clear(sender.getName());
        } else {
            sender.sendMessage(Lang.getPrefix() + Lang.get("Trade_Online", (Player) sender));
        }
        return true;
    }
}
