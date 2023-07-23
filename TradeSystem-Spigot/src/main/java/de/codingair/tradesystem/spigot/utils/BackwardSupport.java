package de.codingair.tradesystem.spigot.utils;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class BackwardSupport {
    private final YamlConfiguration old;
    private final ConfigFile current;
    private boolean changed = false;

    public BackwardSupport() {
        old = TradeSystem.getInstance().getOldConfig();
        current = TradeSystem.getInstance().getFileManager().getFile("Config");

        moveShiftRightClick();
        moveRequestCooldownInSek();
        moveCommandAliases();
        moveEasyMoneySelection();
        moveTradeLog();
        moveMySQLSettings();
        outsourceDbFromTradeLog();

        if (changed) current.saveConfig();
    }

    private void moveShiftRightClick() {
        if (old.get("TradeSystem.Action_To_Request.Rightclick", null) != null) {
            //old
            current.getConfig().set("TradeSystem.Action_To_Request.Shift_Rightclick",
                    old.getBoolean("TradeSystem.Action_To_Request.Rightclick")
                            && old.getBoolean("TradeSystem.Action_To_Request.Shiftclick"));
            changed = true;
        }
    }

    private void moveRequestCooldownInSek() {
        if (old.get("TradeSystem.Request_Cooldown_In_Sek", null) != null) {
            //old
            current.getConfig().set("TradeSystem.Trade_Request_Expiration_Time", old.getInt("TradeSystem.Request_Cooldown_In_Sek"));
            changed = true;
        }
    }

    private void moveCommandAliases() {
        if (old.get("TradeSystem.Trade_Aliases", null) != null) {
            //old
            current.getConfig().set("TradeSystem.Commands.Trade", old.getList("TradeSystem.Trade_Aliases"));
            changed = true;
        }
    }

    private void moveEasyMoneySelection() {
        if (old.get("TradeSystem.Easy_Money_Selection", null) != null) {
            //old
            current.getConfig().set("TradeSystem.Money.Easy_Selection", old.get("TradeSystem.Easy_Money_Selection"));
            changed = true;
        }
    }

    private void moveTradeLog() {
        ConfigurationSection sec = old.getConfigurationSection("TradeSystem.Tradelog");
        if (sec != null) {
            boolean oldCaseSensitive = sec.getKeys(false).contains("Tradelog");

            if (oldCaseSensitive) {
                //old
                current.getConfig().set("TradeSystem.TradeLog", old.getConfigurationSection("TradeSystem.Tradelog"));
                changed = true;
            }
        }
    }

    private void moveMySQLSettings() {
        if (old.contains("TradeSystem.TradeLog.Database.Db_host")) {
            //old

            String url = "jdbc:mysql://%s:%s/%s?autoReconnect=true&useSSL=false";
            url = String.format(url,
                    old.getString("TradeSystem.TradeLog.Database.Db_host"),
                    old.getInt("TradeSystem.TradeLog.Database.Db_port") + "",
                    old.getString("TradeSystem.TradeLog.Database.Db_name")
            );

            current.getConfig().set("TradeSystem.TradeLog.Database.MySQL.Connection_URL", url);
            current.getConfig().set("TradeSystem.TradeLog.Database.MySQL.User", old.getString("TradeSystem.TradeLog.Database.Db_user"));
            current.getConfig().set("TradeSystem.TradeLog.Database.MySQL.Password", old.getString("TradeSystem.TradeLog.Database.Db_password"));
            changed = true;
        }
    }

    private void outsourceDbFromTradeLog() {
        if (old.contains("TradeSystem.TradeLog.Database")) {
            //old

            String type = old.getString("TradeSystem.TradeLog.Database.Type");
            boolean bukkit = "bukkit".equalsIgnoreCase(type);
            current.getConfig().set("TradeSystem.TradeLog.Bukkit_logger", bukkit);

            current.getConfig().set("TradeSystem.Database.Type", bukkit ? "SQLite" : type);
            current.getConfig().set("TradeSystem.Database.MySQL.Connection_URL", old.getString("TradeSystem.TradeLog.Database.MySQL.Connection_URL"));
            current.getConfig().set("TradeSystem.Database.MySQL.User", old.getString("TradeSystem.TradeLog.Database.MySQL.User"));
            current.getConfig().set("TradeSystem.Database.MySQL.Password", old.getString("TradeSystem.TradeLog.Database.MySQL.Password"));
            changed = true;
        }
    }
}
