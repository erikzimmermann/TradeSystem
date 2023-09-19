package de.codingair.tradesystem.spigot.extras.tradelog;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.ext.Extensions;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import org.bukkit.command.CommandSender;

public class TradeLogCMD extends CommandBuilder {
    public TradeLogCMD() {
        super(TradeSystem.getInstance(), "tradelog", "Trade-Log-CMD", new BaseComponent(Permissions.PERMISSION_LOG) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                Lang.send(sender, "No_Permissions");
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Extensions.TradeAudit.sendDownloadInfo(sender);
                return false;
            }
        }, true);
    }
}
