package de.codingair.tradesystem.spigot.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.tradesystem.spigot.TradeSystem;
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
                Lang.send(sender, "§c", "Not_Able_To_Trade");
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                Lang.send(sender, "Only_for_Player");
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Command_How_To");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Command_How_To");
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
                    try {
                        TradeSystem.invitations().handleInvitation((Player) sender, null, true);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    return true;
                }
            });

            getComponent(cmd).addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    suggestions.addAll(TradeSystem.invitations().getInvitationNames(sender.getName()));
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    try {
                        TradeSystem.invitations().handleInvitation((Player) sender, argument, true);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    return true;
                }
            });
        }


        //DENY
        for (String cmd : commandManager.getDenyAliases()) {
            getBaseComponent().addChild(new CommandComponent(cmd) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    TradeSystem.invitations().handleInvitation((Player) sender, null, false);
                    return true;
                }
            });

            getComponent(cmd).addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    suggestions.addAll(TradeSystem.invitations().getInvitationNames(sender.getName()));
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    TradeSystem.invitations().handleInvitation((Player) sender, argument, false);
                    return true;
                }
            });
        }


        //INVITE
        getBaseComponent().addChild(new MultiCommandComponent(Permissions.PERMISSION_INITIATE) {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().equals(sender.getName()) || !((Player) sender).canSee(player)) continue;


                    Set<String> invitations = TradeSystem.invitations().getInvitationNames(player.getName());
                    if (invitations.contains(sender.getName().toLowerCase())) continue;
                    suggestions.add(player.getName());
                }

                TradeSystem.proxy().getPlayers(sender).forEach(player -> {
                    Set<String> l = TradeSystem.invitations().getInvitationNames(player);
                    if (l.contains(sender.getName().toLowerCase())) return;

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

    private boolean toggle(CommandSender sender) {
        if (TradeSystem.getInstance().getTradeManager().toggle((Player) sender)) {
            Lang.send(sender, "Trade_Offline");
            TradeSystem.invitations().clear(sender.getName());
        } else {
            Lang.send(sender, "Trade_Online");
        }
        return true;
    }
}
