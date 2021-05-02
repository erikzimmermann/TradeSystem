package de.codingair.tradesystem.spigot.trade.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.editor.guis.GMenu;
import de.codingair.tradesystem.spigot.trade.layout.utils.AbstractPattern;
import de.codingair.tradesystem.spigot.trade.layout.utils.Pattern;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TradeSystemCMD extends CommandBuilder {
    public TradeSystemCMD() {
        super(TradeSystem.getInstance(), "de/codingair/tradesystem", "Trade-System-CMD", new BaseComponent(Permissions.PERMISSION_MODIFY) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permissions"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem").replace("%label%", label));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem").replace("%label%", label));
                return false;
            }
        }, true, "ts");

        getBaseComponent().addChild(new CommandComponent("reload") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                try {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Plugin_Reloading"));
                    String s = Lang.getPrefix() + Lang.get("Success_Plugin_Reloaded");
                    TradeSystem.getInstance().reload();
                    sender.sendMessage(s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("layout") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout").replace("%label%", label));
                return false;
            }
        });

        getComponent("layout").addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                new GMenu((Player) sender).open();
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("layout").addChild(new CommandComponent("edit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Edit").replace("%label%", label));
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("layout", "edit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (AbstractPattern layout : TradeSystem.getInstance().getLayoutManager().getLayouts()) {
                    if (layout.isStandard()) continue;
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                AbstractPattern pattern = TradeSystem.getInstance().getLayoutManager().getPattern(argument);

                if (pattern == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return false;
                }

                if (pattern.isStandard()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Edit_Standard"));
                    return false;
                }

                new GMenu((Player) sender, pattern).open();
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("layout").addChild(new CommandComponent("activate") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Activate").replace("%label%", label));
                return false;
            }
        });

        getComponent("layout", "activate").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (AbstractPattern layout : TradeSystem.getInstance().getLayoutManager().getLayouts()) {
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = TradeSystem.getInstance().getLayoutManager().getPattern(argument);

                if (pattern == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return false;
                }

                if (TradeSystem.getInstance().getLayoutManager().getActive().getName().equals(pattern.getName())) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Already_Activated"));
                    return false;
                }

                TradeSystem.getInstance().getLayoutManager().setActive(pattern);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Activated").replace("%name%", pattern.getName()));
                return false;
            }
        });

        getComponent("layout").addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Delete").replace("%label%", label));
                return false;
            }
        });

        getComponent("layout", "delete").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (AbstractPattern layout : TradeSystem.getInstance().getLayoutManager().getLayouts()) {
                    if (layout.isStandard()) continue;
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                AbstractPattern pattern = TradeSystem.getInstance().getLayoutManager().getPattern(argument);

                if (pattern == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return false;
                }

                if (pattern.isStandard()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Delete_Standard"));
                    return false;
                }

                TradeSystem.getInstance().getLayoutManager().remove(pattern);
                TradeSystem.getInstance().getLayoutManager().setActive(TradeSystem.getInstance().getLayoutManager().getPattern("Standard"));
                sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Deleted").replace("%name%", pattern.getName()));
                return false;
            }
        });
    }
}
