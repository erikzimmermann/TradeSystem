package de.codingair.tradesystem.trade.commands;

import de.codingair.codingapi.server.commands.BaseComponent;
import de.codingair.codingapi.server.commands.CommandBuilder;
import de.codingair.codingapi.server.commands.CommandComponent;
import de.codingair.codingapi.server.commands.MultiCommandComponent;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.editor.guis.GMenu;
import de.codingair.tradesystem.trade.layout.utils.AbstractPattern;
import de.codingair.tradesystem.trade.layout.utils.Pattern;
import de.codingair.tradesystem.utils.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TradeSystemCMD extends CommandBuilder {
    public TradeSystemCMD() {
        super("TradeSystem", new BaseComponent(TradeSystem.PERMISSION_MODIFY) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permissions"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) { }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem").replace("%LABEL%", label));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem").replace("%LABEL%", label));
                return false;
            }
        }, true);

        getBaseComponent().addChild(new CommandComponent("reload") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                try {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Plugin_Reloading"));
                    TradeSystem.getInstance().reload();
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Success_Plugin_Reloaded"));
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("layout") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout").replace("%LABEL%", label));
                return false;
            }
        });

        getComponent("layout").addChild(new CommandComponent("editor") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                new GMenu((Player) sender).open();
                return false;
            }
        });

        getComponent("layout").addChild(new CommandComponent("activate") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Activate").replace("%LABEL%", label));
                return false;
            }
        });

        getComponent("layout", "activate").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                for(AbstractPattern layout : TradeSystem.getInstance().getLayoutManager().getLayouts()) {
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = TradeSystem.getInstance().getLayoutManager().getPattern(argument);

                if(pattern == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return false;
                }

                if(TradeSystem.getInstance().getLayoutManager().getActive().getName().equals(pattern.getName())) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Already_Activated"));
                    return false;
                }

                TradeSystem.getInstance().getLayoutManager().setActive(pattern);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Activated").replace("%NAME%", pattern.getName()));
                return false;
            }
        });

        getComponent("layout").addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Help_TradeSystem_Layout_Delete").replace("%LABEL%", label));
                return false;
            }
        });

        getComponent("layout", "delete").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                for(AbstractPattern layout : TradeSystem.getInstance().getLayoutManager().getLayouts()) {
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = TradeSystem.getInstance().getLayoutManager().getPattern(argument);

                if(pattern == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Does_Not_Exist"));
                    return false;
                }
                
                if(pattern.isStandard()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Delete_Standard"));
                    return false;
                }

                TradeSystem.getInstance().getLayoutManager().remove(pattern.getName());
                TradeSystem.getInstance().getLayoutManager().setActive(TradeSystem.getInstance().getLayoutManager().getPattern("Standard"));
                sender.sendMessage(Lang.getPrefix() + Lang.get("Layout_Deleted").replace("%NAME%", pattern.getName()));
                return false;
            }
        });
    }
}
