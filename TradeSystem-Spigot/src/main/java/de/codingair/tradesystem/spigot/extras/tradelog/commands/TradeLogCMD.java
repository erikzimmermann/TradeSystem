package de.codingair.tradesystem.spigot.extras.tradelog.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogOptions;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        getBaseComponent().addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                try {
                    if (TradeLogOptions.isEnabled()) {
                        Bukkit.getScheduler().runTaskAsynchronously(TradeSystem.getInstance(), () -> {
                            if (TradeLogService.getTradeLog().notConnected()) {
                                sender.sendMessage(Lang.getPrefix() + Lang.get("TradeLog_Disabled").replace("%label%", label));
                                return;
                            }

                            List<TradeLog> log = TradeLogService.getTradeLog().getLogMessages(argument);

                            List<String> messages = new ArrayList<>();
                            messages.add("§0");
                            messages.add("§0");
                            messages.add("§7§m                            §c §lTRADE LOG§7 §m                            ");
                            messages.add("§0");

                            if (log.isEmpty()) messages.add("  §c-");
                            else {
                                String p1 = null;
                                String p2 = null;
                                boolean samePlayers = false;

                                for (int i = 0; i < log.size(); i++) {
                                    TradeLog l = log.get(i);
                                    boolean last = i + 1 == log.size();

                                    String name1 = l.getPlayer1Name();
                                    String name2 = l.getPlayer2Name();

                                    samePlayers = p1 == null || Objects.equals(p1, name1) && Objects.equals(p2, name2);
                                    if (!samePlayers) {
                                        messages.add("§0");
                                        messages.add("§8§m                               §7 Players: §e" + p1 + " §7& §e" + p2);
                                        if (!last) messages.add("§0");
                                    }

                                    String color = TradeLogMessages.getByString(l.getMessage()).getColor();
                                    messages.add("§8" + l.getTimestamp().format(formatter) + " " + color + "» §7" + l.getMessage());

                                    p1 = name1;
                                    p2 = name2;
                                }

                                if (samePlayers) {
                                    messages.add("§0");
                                    messages.add("§8§m                               §7 Players: §e" + p1 + " §7& §e" + p2);
                                }
                            }

                            sender.sendMessage(messages.toArray(new String[0]));
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
