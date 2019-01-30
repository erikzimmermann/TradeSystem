package de.codingair.tradesystem.trade;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.utils.blacklist.BlockedItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeManager {
    private List<Player> offline = new ArrayList<>();
    private List<Trade> tradeList = new ArrayList<>();
    private List<BlockedItem> blacklist = new ArrayList<>();
    private int cooldown = 60;
    private int distance = 50;

    private boolean cancelOnDamage = true;
    private boolean requestOnRightclick = false;
    private boolean shiftclick = false;
    private List<String> allowedGameModes = new ArrayList<>();
    private boolean tradeBoth = true;
    private boolean dropItems = true;
    private SoundData soundStarted = null;
    private SoundData soundFinish = null;
    private SoundData soundCancel = null;
    private SoundData soundBlocked = null;

    public void load() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        boolean save = false;
        cooldown = config.getInt("TradeSystem.Request_Cooldown_In_Sek", 60);
        if(cooldown <= 10) {
            config.set("TradeSystem.Request_Cooldown_In_Sek", 10);
            save = true;
        }

        if(config.getBoolean("TradeSystem.Trade_Distance.enabled", true)) {
            this.distance = config.getInt("TradeSystem.Trade_Distance.distance_in_blocks", 50);
            if(this.distance < 1) {
                config.set("TradeSystem.Trade.distance_in_blocks", 1);
                save = true;
            }
        } else this.distance = -1;

        this.cancelOnDamage = config.getBoolean("TradeSystem.Action_To_Cancel.Player_get_damaged", true);
        this.requestOnRightclick = config.getBoolean("TradeSystem.Action_To_Request.Rightclick", false);
        this.shiftclick = config.getBoolean("TradeSystem.Action_To_Request.Shiftclick", true);
        this.tradeBoth = config.getBoolean("TradeSystem.Trade_Both", true);
        this.dropItems = config.getBoolean("TradeSystem.Trade_Drop_Items", true);

        try {
            this.soundStarted = new SoundData(Sound.valueOf(config.getString("TradeSystem.Sounds.Trade_Started.Name", "LEVEL_UP")),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Started.Volume", 0.6),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Started.Pitch", 1.0));
        } catch(Exception ex) {
            this.soundStarted = new SoundData(Sound.LEVEL_UP, 0.6F, 1F);
        }

        try {
            this.soundFinish = new SoundData(Sound.valueOf(config.getString("TradeSystem.Sounds.Trade_Finished.Name", "LEVEL_UP")),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Finished.Volume", 0.6),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Finished.Pitch", 1.0));
        } catch(Exception ex) {
            this.soundFinish = new SoundData(Sound.LEVEL_UP, 0.6F, 1F);
        }

        try {
            this.soundBlocked = new SoundData(Sound.valueOf(config.getString("TradeSystem.Sounds.Trade_Blocked.Name", "NOTE_BASS")),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Blocked.Volume", 0.8),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Blocked.Pitch", 0.6));
        } catch(Exception ex) {
            this.soundBlocked = new SoundData(Sound.NOTE_BASS, 0.8F, 0.6F);
        }

        try {
            this.soundCancel = new SoundData(Sound.valueOf(config.getString("TradeSystem.Sounds.Trade_Cancelled.Name", "ITEM_BREAK")),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Cancelled.Volume", 0.6),
                    (float) config.getDouble("TradeSystem.Sounds.Trade_Cancelled.Pitch", 1.0));
        } catch(Exception ex) {
            this.soundCancel = new SoundData(Sound.ITEM_BREAK, 0.6F, 1F);
        }

        this.allowedGameModes.clear();
        this.allowedGameModes = config.getStringList("TradeSystem.Allowed_GameModes");
        if(this.allowedGameModes == null) this.allowedGameModes = new ArrayList<>();

        TradeSystem.log("  > Loading blacklist");
        List<String> l = config.getStringList("TradeSystem.Blacklist");
        if(l != null && !l.isEmpty()) {
            for(String s : l) {
                BlockedItem item = BlockedItem.fromString(s);
                if(item != null) this.blacklist.add(item);
                else {
                    TradeSystem.log("    ...found a wrong Material-Tag (here: \"" + s + "\"). Skipping...");
                }
            }
        }

        if(this.blacklist.isEmpty()) {
            this.blacklist.add(new BlockedItem(Material.AIR, (byte) 0));
            this.blacklist.add(new BlockedItem(Material.AIR, (byte) 0, "&cExample"));
            this.blacklist.add(new BlockedItem("&cExample, which blocks all items with this strange name!"));
            saveBlackList();
        }

        TradeSystem.log("    ...got " + this.blacklist.size() + " blocked item(s)");

        if(save) file.saveConfig();
    }

    public void playStartSound(Player player) {
        this.soundStarted.play(player);
    }

    public void playFinishSound(Player player) {
        this.soundFinish.play(player);
    }

    public void playBlockSound(Player player) {
        this.soundBlocked.play(player);
    }

    public void playCancelSound(Player player) {
        this.soundCancel.play(player);
    }

    public void saveBlackList() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();
        List<String> l = new ArrayList<>();

        for(BlockedItem block : this.blacklist) {
            l.add(block.toString());
        }

        config.set("TradeSystem.Blacklist", l);
        file.saveConfig();
    }

    public void startTrade(Player player, Player other) {
        Trade trade = new Trade(other, player);
        this.tradeList.add(trade);
        trade.start();
    }

    public void cancelAll() {
        List<Trade> tradeList = new ArrayList<>(this.tradeList);

        for(Trade trade : tradeList) {
            trade.cancel();
        }

        tradeList.clear();

        TradeSystem.getInstance().getTradeCMD().getInvites().clear();
    }

    public List<Trade> getTradeList() {
        return tradeList;
    }

    public Trade getTrade(Player player) {
        for(Trade trade : this.tradeList) {
            if(trade.isParticipant(player)) return trade;
        }

        return null;
    }


    public boolean isTrading(Player player) {
        return getTrade(player) != null;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isRequestOnRightclick() {
        return requestOnRightclick;
    }

    public boolean isShiftclick() {
        return shiftclick;
    }

    public List<String> getAllowedGameModes() {
        return allowedGameModes;
    }

    public boolean isCancelOnDamage() {
        return cancelOnDamage;
    }

    public boolean isTradeBoth() {
        return tradeBoth;
    }

    public boolean isDropItems() {
        return dropItems;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    public boolean isOffline(Player player) {
        return this.offline.contains(player);
    }

    public boolean toggle(Player player) {
        if(isOffline(player)) {
            this.offline.remove(player);
            return false;
        } else {
            this.offline.add(player);
            return true;
        }
    }

    public List<BlockedItem> getBlacklist() {
        return blacklist;
    }

    public boolean isBlocked(ItemStack item) {
        for(BlockedItem blocked : this.blacklist) {
            if(blocked.matches(item)) return true;
        }

        return false;
    }
}
