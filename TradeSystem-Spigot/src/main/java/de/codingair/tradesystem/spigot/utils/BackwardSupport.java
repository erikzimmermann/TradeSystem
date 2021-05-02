package de.codingair.tradesystem.spigot.utils;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.loader.UTFConfig;
import de.codingair.tradesystem.spigot.TradeSystem;

public class BackwardSupport {
    private final UTFConfig old;
    private final ConfigFile current;
    private boolean changed = false;

    public BackwardSupport() {
        old = TradeSystem.getInstance().getOldConfig();
        current = TradeSystem.getInstance().getFileManager().getFile("Config");

        moveShiftRightClick();
        moveRequestCooldownInSek();

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
}
