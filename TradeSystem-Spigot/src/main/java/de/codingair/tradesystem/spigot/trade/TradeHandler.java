package de.codingair.tradesystem.spigot.trade;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.blacklist.BlockedItem;
import de.codingair.tradesystem.spigot.extras.bstats.MetricsManager;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.EconomyIcon;
import de.codingair.tradesystem.spigot.trade.managers.InvitationManager;
import de.codingair.tradesystem.spigot.utils.InputGUI;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.MalformedParametersException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService.getTradeLog;

public class TradeHandler {
    /**
     * Allow disconnected players to reconnect with same options so they don't have to disable trade requests again.
     */
    private final Cache<String, Boolean> disconnectedOffline = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final Set<String> offline = new HashSet<>();

    private final HashMap<String, Trade> trades = new HashMap<>();
    private final List<BlockedItem> blacklist = new ArrayList<>();
    private final HashMap<String, Integer> moneyShortcuts = new HashMap<>();
    private final InvitationManager invitationManager = new InvitationManager();
    private int requestExpirationTime = 60;
    private int distance = 50;

    private int countdownRepetitions = 0;
    private int countdownInterval = 0;

    private boolean cancelOnDamage = true;
    private boolean requestOnShiftRightclick = false;
    private List<String> allowedGameModes = new ArrayList<>();
    private List<String> blockedWorlds;

    private InputGUI inputGUI = InputGUI.SIGN;
    private boolean tradeBoth = true;
    private boolean dropItems = true;

    private SoundData soundStarted = null;
    private SoundData soundFinish = null;
    private SoundData soundCancel = null;
    private SoundData soundBlocked = null;
    private SoundData soundRequest = null;
    private SoundData countdownTick = null;
    private SoundData countdownStop = null;

    public void load() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        boolean save = false;

        //load options
        requestExpirationTime = config.getInt("TradeSystem.Trade_Request_Expiration_Time", 60);
        if (requestExpirationTime <= 10) {
            config.set("TradeSystem.Trade_Request_Expiration_Time", 10);
            save = true;
        }

        if (config.getBoolean("TradeSystem.Trade_Distance.enabled", true)) {
            this.distance = config.getInt("TradeSystem.Trade_Distance.distance_in_blocks", 50);
            if (this.distance < 1) {
                config.set("TradeSystem.Trade.distance_in_blocks", 1);
                save = true;
            }
        } else this.distance = -1;

        this.cancelOnDamage = config.getBoolean("TradeSystem.Action_To_Cancel.Player_get_damaged", true);
        this.requestOnShiftRightclick = config.getBoolean("TradeSystem.Action_To_Request.Shift_Rightclick", false);
        this.tradeBoth = config.getBoolean("TradeSystem.Trade_Both", true);
        this.inputGUI = InputGUI.getByName(config.getString("TradeSystem.Input_GUI", "SIGN"));
        this.dropItems = config.getBoolean("TradeSystem.Trade_Drop_Items", true);

        if (config.getBoolean("TradeSystem.Trade_Countdown.Enabled", true)) {
            countdownRepetitions = config.getInt("TradeSystem.Trade_Countdown.Repetitions");
            countdownInterval = config.getInt("TradeSystem.Trade_Countdown.Interval");
        } else {
            countdownRepetitions = (countdownInterval = 0);
        }

        //load money abbreviations like 1k = 1,000
        moneyShortcuts.clear();
        if (config.getBoolean("TradeSystem.Easy_Money_Selection.Enabled", true)) {
            List<?> data = config.getList("TradeSystem.Easy_Money_Selection.Shortcuts");
            if (data != null) {
                for (Object s : data) {
                    if (s instanceof Map) {
                        try {
                            JSON json = new JSON((Map<?, ?>) s);

                            int value = json.getInteger("Value", -1);
                            if (value < 0) continue;

                            JSONArray a = json.getList("Keys");

                            if (a == null) continue;
                            for (Object o : a) {
                                String key = ((String) o).trim().toLowerCase();
                                moneyShortcuts.put(key, value);
                            }
                        } catch (Exception e) {
                            throw new MalformedParametersException("Malformed money selection for input: '" + s + "'");
                        }
                    }
                }
            }
        }

        TradeSystem.log("  > Loading sounds");
        this.soundStarted = getSound("Trade_Started", config, "LEVEL_UP");
        if (this.soundStarted == null) TradeSystem.log("    > No start sound detected (maybe a spelling mistake?)");

        this.soundFinish = getSound("Trade_Finished", config, "LEVEL_UP");
        if (this.soundFinish == null) TradeSystem.log("    > No finish sound detected (maybe a spelling mistake?)");

        this.soundBlocked = getSound("Trade_Blocked", config, "NOTE_BASS");
        if (this.soundBlocked == null) TradeSystem.log("    > No itemBlocked sound detected (maybe a spelling mistake?)");

        this.soundCancel = getSound("Trade_Cancelled", config, "ITEM_BREAK");
        if (this.soundCancel == null) TradeSystem.log("    > No cancel sound detected (maybe a spelling mistake?)");

        this.soundRequest = getSound("Trade_Request", config, "ORB_PICKUP");
        if (this.soundRequest == null) TradeSystem.log("    > No request sound detected (maybe a spelling mistake?)");

        this.countdownTick = getSound("Countdown_Tick", config, "ORB_PICKUP");
        if (this.countdownTick == null) TradeSystem.log("    > No countdown tick sound detected (maybe a spelling mistake?)");

        this.countdownStop = getSound("Countdown_Stop", config, "ITEM_BREAK");
        if (this.countdownStop == null) TradeSystem.log("    > No countdown stop sound detected (maybe a spelling mistake?)");

        //load allowed game modes
        if (this.allowedGameModes != null) this.allowedGameModes.clear();
        this.allowedGameModes = config.getStringList("TradeSystem.Allowed_GameModes");

        //load blocked worlds
        if (this.blockedWorlds != null) this.blockedWorlds.clear();
        this.blockedWorlds = config.getStringList("TradeSystem.Blocked_Worlds");

        //load blacklisted items and add example items if there are no items.
        TradeSystem.log("  > Loading blacklist");
        List<String> l = config.getStringList("TradeSystem.Blacklist");
        if (!l.isEmpty()) {
            for (String s : l) {
                BlockedItem item = BlockedItem.fromString(s);
                if (item != null) this.blacklist.add(item);
                else {
                    TradeSystem.log("    ...found a wrong Material-Tag (here: \"" + s + "\"). Skipping...");
                }
            }
        }

        if (this.blacklist.isEmpty()) {
            this.blacklist.add(new BlockedItem(Material.AIR, (byte) 0));
            this.blacklist.add(new BlockedItem(Material.AIR, (byte) 0, "&cExample"));
            this.blacklist.add(new BlockedItem("&cExample, which blocks all items with this strange name!"));
            saveBlackList();
        }

        TradeSystem.log("    ...got " + this.blacklist.size() + " blocked item(s)");

        if (save) file.saveConfig();
    }

    private SoundData getSound(String name, FileConfiguration config, String def) {
        try {
            String sound = config.getString("TradeSystem.Sounds." + name + ".Name", def);
            assert sound != null;

            Optional<Sound> opt = Sound.matchXSound(sound);
            return opt.map(value -> new SoundData(value,
                    (float) config.getDouble("TradeSystem.Sounds." + name + ".Volume", 0.6),
                    (float) config.getDouble("TradeSystem.Sounds." + name + ".Pitch", 1.0))
            ).orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void playRequestSound(Player player) {
        if (this.soundRequest != null) this.soundRequest.play(player);
    }

    public void playStartSound(Player player) {
        if (this.soundStarted != null) this.soundStarted.play(player);
    }

    public void playFinishSound(Player player) {
        if (this.soundFinish != null) this.soundFinish.play(player);
    }

    public void playBlockSound(Player player) {
        if (this.soundBlocked != null) this.soundBlocked.play(player);
    }

    public void playCancelSound(Player player) {
        if (this.soundCancel != null) this.soundCancel.play(player);
    }

    public void playCountdownTickSound(Player player) {
        if (this.countdownTick != null) this.countdownTick.play(player);
    }

    public void playCountdownStopSound(Player player) {
        if (this.countdownStop != null) this.countdownStop.play(player);
    }

    public void saveBlackList() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();
        List<String> l = new ArrayList<>();

        for (BlockedItem block : this.blacklist) {
            l.add(block.toString());
        }

        config.set("TradeSystem.Blacklist", l);
        file.saveConfig();
    }

    public void startTrade(Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        if (TradeSystem.man().isTrading(player) || TradeSystem.man().isTrading(other)) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Other_is_already_trading", player));
            return;
        }

        //log only one start (proxy trades have a start on each server)
        if (initiationServer) getTradeLog().log(player.getName(), othersName, TradeLogMessages.STARTED.get());

        MetricsManager.TRADES++;

        player.closeInventory();
        if (other != null) other.closeInventory();

            Trade trade = createTrade(player, other, othersName, initiationServer);

            //register
            this.trades.put(player.getName().toLowerCase(), trade);
            this.trades.put(othersName.toLowerCase(), trade);

            trade.start();
    }

    @NotNull
    private Trade createTrade(Player player, @Nullable Player other, @NotNull String name, boolean initiationServer) {
        if (other != null) return new BukkitTrade(player, other, initiationServer);
        else return new ProxyTrade(player, name, initiationServer);
    }

    public void quit(Player player) {
        //save options
        if (this.offline.remove(player.getName())) {
            //just use any value
            this.disconnectedOffline.put(player.getName(), true);
        }

        //cancel active trade
        Trade activeTrade = getTrade(player);
        if (activeTrade != null) activeTrade.cancel();
    }

    public void join(Player player) {
        //revive options
        if (this.disconnectedOffline.getIfPresent(player.getName()) != null) {
            this.disconnectedOffline.invalidate(player.getName());
            this.offline.add(player.getName());
        }
    }

    public void cancelAll() {
        List<Trade> tradeList = new ArrayList<>(this.trades.values());

        for (Trade trade : tradeList) {
            trade.cancel();
        }

        tradeList.clear();
        TradeSystem.invitations().clear();
    }

    public Collection<Trade> getTradesList() {
        return trades.values();
    }

    public HashMap<String, Trade> getTrades() {
        return trades;
    }

    public Trade getTrade(Player player) {
        return getTrade(player.getName().toLowerCase());
    }

    public Trade getTrade(String player) {
        return this.trades.get(player.toLowerCase());
    }

    public boolean isTrading(Player player) {
        if (player == null) return false;
        return getTrade(player) != null;
    }

    public int getRequestExpirationTime() {
        return requestExpirationTime;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isRequestOnShiftRightClick() {
        return requestOnShiftRightclick;
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
        return this.offline.contains(player.getName());
    }

    public boolean toggle(Player player) {
        if (offline.remove(player.getName())) return false;
        this.offline.add(player.getName());
        return true;
    }

    public List<BlockedItem> getBlacklist() {
        return blacklist;
    }

    public boolean isBlocked(ItemStack item) {
        for (BlockedItem blocked : this.blacklist) {
            if (blocked.matches(item)) return true;
        }

        return false;
    }

    public List<String> getBlockedWorlds() {
        return blockedWorlds;
    }

    public boolean isBlockedWorld(World w) {
        if (w == null) return true;

        for (String world : this.blockedWorlds) {
            if (w.getName().equalsIgnoreCase(world)) return true;
        }

        return false;
    }

    @Nullable
    public Integer getMoneyShortcutFactor(String s) {
        String key = s.toLowerCase().replaceAll("[^a-z]", "");
        return moneyShortcuts.get(key);
    }

    @NotNull
    public String buildString(@NotNull Number value, boolean forceDecimal) {
        DecimalFormat df = getDefaultDecimalFormat();

        if (forceDecimal) {
            df.setDecimalSeparatorAlwaysShown(true);
            df.setMinimumFractionDigits(1);
        }

        return df.format(value);
    }

    @NotNull
    public String makeAmountFancy(@NotNull Number value) {
        DecimalFormat df = getDefaultDecimalFormat();

        df.setGroupingUsed(true);
        df.setGroupingSize(3);

        return df.format(value);
    }

    private DecimalFormat getDefaultDecimalFormat() {
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(EconomyIcon.FRACTION_DIGITS);
        df.setMinimumIntegerDigits(1);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(symbols);

       return df;
    }

    public int getCountdownRepetitions() {
        return countdownRepetitions;
    }

    public int getCountdownInterval() {
        return countdownInterval;
    }

    public InvitationManager getInvitationManager() {
        return invitationManager;
    }

    public InputGUI getInputGUI() {
        return inputGUI;
    }
}
