package de.codingair.tradesystem.tradelog.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.tradelog.TradeLog;
import de.codingair.tradesystem.utils.Lang;
import org.bukkit.command.CommandSender;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.codingair.tradesystem.tradelog.TradeLogService.getTradeLog;

public class TradeLogCMD extends CommandBuilder {
    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public TradeLogCMD() {
        super(TradeSystem.getInstance(), "tradelog", "Trade-Log-CMD", new BaseComponent(TradeSystem.PERMISSION_LOG) {
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

        getBaseComponent().addChild(new CommandComponent("read") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                try {
                    if (getTradeLog().isEnabled()) {
                        List<TradeLog> logMessages = getTradeLog().getLogMessages(args[0]);
                        sender.sendMessage("============= TRADE LOG ==============");
                        sender.sendMessage("======================================");

                        for (TradeLog logMessage : logMessages) {
                            sender.sendMessage("[" + logMessage.getTimestamp().format(formatter) + "]: " +
                                    "[" + logMessage.getPlayer1Name() + " - " + logMessage.getPlayer2Name() + "] : " + logMessage.getMessage());
                        }

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
