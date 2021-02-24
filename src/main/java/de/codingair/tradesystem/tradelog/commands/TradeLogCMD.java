package de.codingair.tradesystem.tradelog.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.tradelog.TradeLog;
import de.codingair.tradesystem.tradelog.TradeLogOptions;
import de.codingair.tradesystem.utils.Lang;
import de.codingair.tradesystem.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.codingair.tradesystem.tradelog.TradeLogService.getTradeLog;

public class TradeLogCMD extends CommandBuilder {
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");

    public TradeLogCMD() {
        super(TradeSystem.getInstance(), "tradelog", "Trade-Log-CMD", new BaseComponent(Permissions.PERMISSION_LOG) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permissions"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeLog").replace("%label%", label));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeLog").replace("%label%", label));
                return false;
            }
        }, true, "tl");


        //LOG
        getBaseComponent().addChild(new MultiCommandComponent(Permissions.PERMISSION_LOG) {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                try {
                    if (TradeLogOptions.isEnabled()) {
                        Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), () -> {
                            List<TradeLog> logMessages = getTradeLog().getLogMessages(argument);
                            sender.sendMessage("§c============= TRADE LOG ==============");

                            for (TradeLog logMessage : logMessages) {
                                sender.sendMessage("§2[" + logMessage.getTimestamp().format(formatter) + "]: " +
                                        "[" + logMessage.getPlayer1Name() + " - " + logMessage.getPlayer2Name() + "]");
                                sender.sendMessage(logMessage.getMessage());
                            }
                        });
                    } else {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("TradeLog_Disabled").replace("%label%", label));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        });
    }
}
