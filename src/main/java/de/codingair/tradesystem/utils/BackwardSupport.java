package de.codingair.tradesystem.utils;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.loader.UTFConfig;
import de.codingair.tradesystem.TradeSystem;

public class BackwardSupport {
    private final UTFConfig old;
    private final ConfigFile current;
    private boolean changed = false;

    public BackwardSupport() {
        old = TradeSystem.getInstance().getOldConfig();
        current = TradeSystem.getInstance().getFileManager().getFile("Config");

        moveShiftRightclick();

        if(changed) current.saveConfig();
    }

    private void moveShiftRightclick() {
        if(old.contains("TradeSystem.Action_To_Request.Rightclick", true)) {
            //old
            current.getConfig().set("TradeSystem.Action_To_Request.Shift_Rightclick",
                    old.getBoolean("TradeSystem.Action_To_Request.Rightclick")
                    && old.getBoolean("TradeSystem.Action_To_Request.Shiftclick"));
            changed = true;
        }
    }
}
