package de.codingair.tradesystem.spigot.commands;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.special.MultiCommandComponent;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.TradeHandler;
import de.codingair.tradesystem.spigot.trade.gui.editor.Editor;
import de.codingair.tradesystem.spigot.trade.gui.layout.LayoutManager;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.patterns.DefaultPattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Name;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TradeSystemCMD extends CommandBuilder {
    public TradeSystemCMD() {
        super(TradeSystem.getInstance(), "tradesystem", "Trade-System-CMD", new BaseComponent(Permissions.PERMISSION_MODIFY) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                Lang.send(sender, "No_Permissions");
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                Lang.send(sender, "Only_for_Player");
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Help_TradeSystem", new Lang.P("label", label));
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Help_TradeSystem", new Lang.P("label", label));
                return true;
            }
        }, true, "ts");

        LayoutManager l = TradeSystem.getInstance().getLayoutManager();

        if (!TradeSystem.handler().tradeProxy()) {
            getBaseComponent().addChild(new CommandComponent("activateTradeProxy") {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    sender.sendMessage(Lang.getPrefix() + "§cWARNING§7. You're about to enable §eTradeProxy §7for this server. If you don't have §eTradeProxy§7 installed, you'll be §cvulnerable to custom payload attacks§7. Run §c/tradesystem activateTradeProxy confirm§7 if you're sure.");
                    return true;
                }
            });

            getComponent("activateTradeProxy").addChild(new CommandComponent("confirm") {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
                    FileConfiguration config = file.getConfig();

                    config.set("TradeSystem.TradeProxy", true);
                    file.saveConfig();

                    getComponent("reload").runCommand(sender, "reload", new String[0]);
                    return true;
                }
            });
        }

        getBaseComponent().addChild(new CommandComponent("reload") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                try {
                    Lang.send(sender, "Plugin_Reloading");
                    TradeSystem.getInstance().reload();
                    Lang.send(sender, "Success_Plugin_Reloaded");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        });

        getBaseComponent().addChild(new CommandComponent("layout") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Help_TradeSystem_Layout", new Lang.P("label", label));
                return true;
            }
        });

        getComponent("layout").addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Help_TradeSystem_Layout_Create", new Lang.P("label", label));
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout", "create").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender commandSender, String[] strings, List<String> list) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if (!l.isAvailable(argument)) {
                    Lang.send(sender, "§7", "Layout_Name_Already_Exists");
                    return true;
                }

                try {
                    new Editor(argument, (Player) sender).open();
                } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout", "create", null).addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender commandSender, String[] strings, List<String> list) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if (!l.isAvailable(args[2])) {
                    Lang.send(sender, "§7", "Layout_Name_Already_Exists");
                    return true;
                }

                int size;
                try {
                    size = Integer.parseInt(argument);
                } catch (NumberFormatException e) {
                    Lang.send(sender, "Help_TradeSystem_Layout_Create", new Lang.P("label", label));
                    return true;
                }

                if (size % 9 != 0 || size < 9 || size > 54) {
                    Lang.send(sender, "Help_TradeSystem_Layout_Create", new Lang.P("label", label));
                    return true;
                }

                try {
                    new Editor(args[2], size, (Player) sender).open();
                } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout").addChild(new CommandComponent("edit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Help_TradeSystem_Layout_Edit", new Lang.P("label", label));
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout", "edit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Pattern layout : l.getPatterns(true)) {
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = l.getPattern(argument, true);

                if (pattern == null) {
                    Lang.send(sender, "Layout_Does_Not_Exist");
                    return true;
                }

                try {
                    new Editor(pattern, (Player) sender).open();
                } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }.setOnlyPlayers(true));

        getComponent("layout").addChild(new CommandComponent("activate") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Help_TradeSystem_Layout_Activate", new Lang.P("label", label));
                return true;
            }
        });

        getComponent("layout", "activate").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Pattern layout : l.getPatterns()) {
                    suggestions.add(layout.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = l.getPattern(argument);

                if (pattern == null) {
                    Lang.send(sender, "Layout_Does_Not_Exist");
                    return true;
                }

                if (l.getActive().getName().equals(pattern.getName())) {
                    Lang.send(sender, "Layout_Already_Activated");
                    return true;
                }

                l.setActive(pattern.getName());
                Lang.send(sender, "Layout_Activated", new Lang.P("name", pattern.getName()));
                return true;
            }
        });

        getComponent("layout").addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Lang.send(sender, "Help_TradeSystem_Layout_Delete", new Lang.P("label", label));
                return true;
            }
        });

        getComponent("layout", "delete").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for (Pattern layout : l.getPatterns(true)) {
                    suggestions.add(layout.getName());
                }

                for (Name name : l.getCrashedPatterns().keySet()) {
                    suggestions.add(name.toString());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Pattern pattern = l.getPattern(argument, true);

                if (pattern == null) {
                    Map<?, ?> data = l.getCrashedPatterns().remove(new Name(argument));
                    if (data != null) {
                        Lang.send(sender, "Layout_Deleted", new Lang.P("name", Pattern.deserializeName(new JSON(data))));
                        return true;
                    }

                    Lang.send(sender, "Layout_Does_Not_Exist");
                    return true;
                }

                // set active layout to default if the active layout is the layout that should be deleted
                if (pattern.equals(l.getActive())) l.setActive(DefaultPattern.NAME);
                l.delete(pattern);

                Lang.send(sender, "Layout_Deleted", new Lang.P("name", pattern.getName()));
                return true;
            }
        });
    }
}
