package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.lib.JSONArray;
import de.codingair.tradesystem.proxy.packets.PlayerStatePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeOfferItemEvent;
import de.codingair.tradesystem.spigot.events.TradeToggleEvent;
import de.codingair.tradesystem.spigot.extras.blacklist.BlockedItem;
import de.codingair.tradesystem.spigot.extras.bstats.MetricsManager;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogService;
import de.codingair.tradesystem.spigot.trade.managers.InvitationManager;
import de.codingair.tradesystem.spigot.transfer.utils.ItemStackUtils;
import de.codingair.tradesystem.spigot.utils.InputGUI;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.MalformedParametersException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class TradeHandler {
    /**
     * Allow disconnected players to reconnect with same options, so they don't have to disable trade requests again.
     */
    private final Set<String> offline = new HashSet<>();
    private boolean tradeProxy = false;

    private final HashMap<String, Trade> trades = new HashMap<>();

    private final List<BlockedItem> blacklist = new ArrayList<>();
    private final InvitationManager invitationManager = new InvitationManager();
    private int requestExpirationTime = 60;
    private int distance = 50;

    private DecimalFormat moneyPattern;
    private final HashMap<String, BigDecimal> moneyShortcuts = new HashMap<>();

    private int countdownRepetitions = 0;
    private int countdownInterval = 0;

    private boolean cancelOnDamage = true;
    private boolean revokeReadyOnChange = true;
    private boolean requestOnShiftRightclick = false;
    private List<String> allowedGameModes = new ArrayList<>();
    private List<String> blockedWorlds;

    private boolean tradeReportItems = true;
    private boolean tradeReportEconomy = true;

    private InputGUI inputGUI = InputGUI.SIGN;
    private boolean tradeBoth = true;
    private boolean dropItems = true;
    private boolean onlyDisplayNameInMessage = false;

    private SoundData soundStarted = null;
    private SoundData soundFinish = null;
    private SoundData soundCancel = null;
    private SoundData soundBlocked = null;
    private SoundData soundRequest = null;
    private SoundData soundCountdownTick = null;
    private SoundData soundCountdownStop = null;
    private SoundData soundChangeDuringShulkerPeek = null;

    public void load() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();

        boolean save = false;
        tradeProxy = config.getBoolean("TradeSystem.TradeProxy", false);

        //load options
        requestExpirationTime = config.getInt("TradeSystem.Trade_Request_Expiration_Time", 60);
        if (requestExpirationTime < 5) {
            requestExpirationTime = 5;
            config.set("TradeSystem.Trade_Request_Expiration_Time", 5);
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
        this.revokeReadyOnChange = config.getBoolean("TradeSystem.Revoke_Ready_State_on_Offer_Change", true);
        this.requestOnShiftRightclick = config.getBoolean("TradeSystem.Action_To_Request.Shift_Rightclick", false);
        this.tradeBoth = config.getBoolean("TradeSystem.Trade_Both", true);
        this.inputGUI = InputGUI.getByName(config.getString("TradeSystem.Input_GUI", "SIGN"));
        this.dropItems = config.getBoolean("TradeSystem.Trade_Drop_Items", true);

        this.tradeReportItems = config.getBoolean("TradeSystem.Trade_Finish_Report.Items", true);
        this.tradeReportEconomy = config.getBoolean("TradeSystem.Trade_Finish_Report.Economy", true);
        this.onlyDisplayNameInMessage = config.getBoolean("TradeSystem.Trade_Finish_Report.Display_Name_Only", false);

        if (config.getBoolean("TradeSystem.Trade_Countdown.Enabled", true)) {
            countdownRepetitions = config.getInt("TradeSystem.Trade_Countdown.Repetitions");
            countdownInterval = config.getInt("TradeSystem.Trade_Countdown.Interval");
        } else {
            countdownRepetitions = (countdownInterval = 0);
        }

        String pattern = config.getString("TradeSystem.Money.Pattern", "###,###.####");
        try {
            this.moneyPattern = new DecimalFormat(pattern);
        } catch (IllegalArgumentException ex) {
            TradeSystem.getInstance().getLogger().warning("The money pattern '%s' is invalid. Please check your syntax. The pattern '###,###.####' will be used instead.");
            this.moneyPattern = new DecimalFormat("###,###.####");
        }
        this.moneyPattern.setParseBigDecimal(true);
        this.moneyPattern.setRoundingMode(RoundingMode.FLOOR);

        String groupingSeparator = config.getString("TradeSystem.Money.Grouping_Separator", ",");
        String decimalSeparator = config.getString("TradeSystem.Money.Decimal_Separator", ".");

        if (groupingSeparator.length() != 1) {
            TradeSystem.getInstance().getLogger().warning("The grouping separator must be a single character. Please check your syntax. The separator ',' will be used instead.");
            groupingSeparator = ",";
        }

        if (decimalSeparator.length() != 1) {
            TradeSystem.getInstance().getLogger().warning("The decimal separator must be a single character. Please check your syntax. The separator '.' will be used instead.");
            decimalSeparator = ".";
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(decimalSeparator.charAt(0));
        symbols.setGroupingSeparator(groupingSeparator.charAt(0));
        moneyPattern.setDecimalFormatSymbols(symbols);

        //load money abbreviations like 1k = 1,000
        moneyShortcuts.clear();
        if (config.getBoolean("TradeSystem.Money.Easy_Selection.Enabled", true)) {
            List<?> data = config.getList("TradeSystem.Money.Easy_Selection.Shortcuts");
            if (data != null) {
                for (Object s : data) {
                    if (s instanceof Map) {
                        try {
                            JSON json = new JSON((Map<?, ?>) s);

                            Object number = json.getRaw("Value");
                            if (number == null) continue;

                            BigDecimal value = new BigDecimal(number.toString());
                            if (value.signum() <= 0) continue;

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
        if (this.soundBlocked == null)
            TradeSystem.log("    > No itemBlocked sound detected (maybe a spelling mistake?)");

        this.soundCancel = getSound("Trade_Cancelled", config, "ITEM_BREAK");
        if (this.soundCancel == null) TradeSystem.log("    > No cancel sound detected (maybe a spelling mistake?)");

        this.soundRequest = getSound("Trade_Request", config, "ORB_PICKUP");
        if (this.soundRequest == null) TradeSystem.log("    > No request sound detected (maybe a spelling mistake?)");

        this.soundCountdownTick = getSound("Countdown_Tick", config, "ORB_PICKUP");
        if (this.soundCountdownTick == null)
            TradeSystem.log("    > No countdown tick sound detected (maybe a spelling mistake?)");

        this.soundCountdownStop = getSound("Countdown_Stop", config, "ITEM_BREAK");
        if (this.soundCountdownStop == null)
            TradeSystem.log("    > No countdown stop sound detected (maybe a spelling mistake?)");

        this.soundChangeDuringShulkerPeek = getSound("Change_during_Shulker_Peek", config, "ITEM_BREAK");
        if (this.soundChangeDuringShulkerPeek == null)
            TradeSystem.log("    > No change-during-shulker-peek sound detected (maybe a spelling mistake?)");

        //load allowed game modes
        if (this.allowedGameModes != null) this.allowedGameModes.clear();
        this.allowedGameModes = config.getStringList("TradeSystem.Allowed_GameModes");

        //load blocked worlds
        if (this.blockedWorlds != null) this.blockedWorlds.clear();
        this.blockedWorlds = config.getStringList("TradeSystem.Blocked_Worlds");

        //load blacklisted items and add example items if there are no items.
        TradeSystem.log("  > Loading blacklist");
        List<?> l = config.getList("TradeSystem.Blacklist");

        if (l != null) {
            for (Object o : l) {
                if (o instanceof Map) {
                    try {
                        BlockedItem item = BlockedItem.create((Map<?, ?>) o);
                        this.blacklist.add(item);
                    } catch (Exception ex) {
                        TradeSystem.log("    ...could not deserialize blocked item: " + o + ". Skipping...");
                        ex.printStackTrace();
                    }
                } else if (o instanceof String) {
                    //LEGACY SUPPORT: 2.0.6 and lower
                    @SuppressWarnings("deprecation")
                    BlockedItem item = BlockedItem.fromString((String) o);
                    if (item != null) this.blacklist.add(item);
                    else TradeSystem.log("    ...found a wrong Material-Tag (here: \"" + o + "\"). Skipping...");
                } else TradeSystem.log("    ...could not deserialize blocked item: " + o + ". Skipping...");
            }
        }

        if (this.blacklist.isEmpty()) {
            this.blacklist.add(BlockedItem.create().material(Material.AIR));
            this.blacklist.add(BlockedItem.create().material(Material.AIR).displayName("&cExample"));
            this.blacklist.add(BlockedItem.create().displayName("&cExample, which blocks all items with this strange name!"));
            saveBlackList();
        }

        TradeSystem.log("    ...got " + this.blacklist.size() + " blocked item(s)");

        if (save) file.saveConfig();

        invitationManager.startExpirationHandler();
    }

    public void disable() {
        cancelAll();
        invitationManager.stopExpirationHandler();
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
        if (this.soundCountdownTick != null) this.soundCountdownTick.play(player);
    }

    public void playCountdownStopSound(Player player) {
        if (this.soundCountdownStop != null) this.soundCountdownStop.play(player);
    }

    public void playChangeDuringShulkerPeekSound(Player player) {
        if (this.soundChangeDuringShulkerPeek != null) this.soundChangeDuringShulkerPeek.play(player);
    }

    public void saveBlackList() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        FileConfiguration config = file.getConfig();
        List<JSON> l = new ArrayList<>();

        for (BlockedItem block : this.blacklist) {
            JSON json = new JSON();
            block.write(json);
            l.add(json);
        }

        config.set("TradeSystem.Blacklist", l);
        file.saveConfig();
    }

    public void startTrade(@NotNull Player player, @NotNull Player other) {
        startTrade(player, other, other.getName(), other.getUniqueId(), other.getWorld().getName(), null, true);
    }

    public void startTrade(@NotNull Player player, @NotNull String othersName, @NotNull UUID otherId, @NotNull String otherWorld, @NotNull String otherServer, boolean initiationServer) {
        startTrade(player, null, othersName, otherId, otherWorld, otherServer, initiationServer);
    }

    /**
     * Starts a trade between two players.
     *
     * @param player           The player who starts the trade.
     * @param other            The player who gets the trade request.
     * @param othersName       The name of the other player.
     * @param otherId          The id of the other player.
     * @param initiationServer If the trade is started on the proxy.
     */
    private void startTrade(@NotNull Player player, @Nullable Player other, @NotNull String othersName, @NotNull UUID otherId, @NotNull String otherWorld, @Nullable String otherServer, boolean initiationServer) {
        if (TradeSystem.handler().isTrading(player) || TradeSystem.handler().isTrading(other)) {
            Lang.send(player, "Other_is_already_trading");
            return;
        }

        MetricsManager.TRADES++;

        player.closeInventory();
        if (other != null) other.closeInventory();

        Trade trade = createTrade(player, other, othersName, otherId, otherWorld, otherServer, initiationServer);

        //log only one start (proxy trades have a start on each server)
        if (initiationServer) TradeLogService.log(player.getName(), othersName, TradeLog.STARTED.get());

        //register
        registerTrade(trade, player.getName());
        registerTrade(trade, othersName);

        trade.start();
    }

    private void registerTrade(@NotNull Trade trade, @NotNull String player) {
        this.trades.put(player.toLowerCase(), trade);
    }

    public void unregisterTrade(@NotNull String player) {
        this.trades.remove(player.toLowerCase());
    }

    @NotNull
    private Trade createTrade(Player player, @Nullable Player other, @NotNull String name, @NotNull UUID otherId, @NotNull String otherWorld, @Nullable String otherServer, boolean initiationServer) {
        if (other != null) return new BukkitTrade(player, other, initiationServer);
        else {
            if (otherServer == null)
                throw new IllegalArgumentException("'otherServer' cannot be null when creating a ProxyTrade");
            return new ProxyTrade(player, name, otherId, initiationServer, otherWorld, otherServer);
        }
    }

    public void quit(Player player) {
        //cancel active trade
        Trade activeTrade = getTrade(player);
        if (activeTrade != null) activeTrade.cancel();
    }

    private void cancelAll() {
        TradeSystem.log("  > Cancelling all active trades");
        List<Trade> tradeList = new ArrayList<>(this.trades.values());

        for (Trade trade : tradeList) {
            trade.cancel();
        }

        tradeList.clear();
        TradeSystem.invitations().clear();
    }

    public Trade getTrade(Player player) {
        return getTrade(player.getName());
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

    public boolean isRevokeReadyOnChange() {
        return revokeReadyOnChange;
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

    public boolean isOnlyDisplayNameInMessage() {
        return onlyDisplayNameInMessage;
    }

    public boolean isOffline(Player player) {
        return this.offline.contains(player.getName());
    }

    public boolean toggle(Player player) {
        if (offline.remove(player.getName())) {
            // removed
            TradeSystem.proxyHandler().send(new PlayerStatePacket(player.getUniqueId(), player.getName(), false), player);
            Bukkit.getPluginManager().callEvent(new TradeToggleEvent(player.getUniqueId(), player.getName(), false));
            return false;
        }

        // not contained -> will be added now
        this.offline.add(player.getName());
        TradeSystem.proxyHandler().send(new PlayerStatePacket(player.getUniqueId(), player.getName(), true), player);
        Bukkit.getPluginManager().callEvent(new TradeToggleEvent(player.getUniqueId(), player.getName(), true));
        return true;
    }

    public void setState(@NotNull UUID playerId, @NotNull String playerName, boolean online) {
        if (online) {
            if (this.offline.remove(playerName)) {
                Bukkit.getPluginManager().callEvent(new TradeToggleEvent(playerId, playerName, true));
            }
        } else {
            if (this.offline.add(playerName)) {
                Bukkit.getPluginManager().callEvent(new TradeToggleEvent(playerId, playerName, false));
            }
        }
    }

    public List<BlockedItem> getBlacklist() {
        return blacklist;
    }

    /**
     * Checks whether an item is blocked or not. Also includes compatibility checks for TradeProxy.
     *
     * @param item The {@link ItemStack} which will be traded.
     * @return {@link Boolean#TRUE} if this item should be marked as blocked.
     */
    public boolean isBlocked(@NotNull Trade trade, @NotNull ItemStack item) {
        boolean blacklisted = false;

        if (trade instanceof ProxyTrade) {
            boolean compatible = ItemStackUtils.isCompatible(item);
            if (!compatible) {
                // item is not TradeProxy compatible
                return true;
            }
        }

        for (BlockedItem blocked : this.blacklist) {
            if (blocked.matches(item)) {
                blacklisted = true;
                break;
            }
        }

        return blacklisted;
    }

    /**
     * Checks whether an item is blocked or not. Also includes compatibility checks for TradeProxy and other plugins.
     *
     * @param placer          The player that placed the item.
     * @param receivingPlayer The player that should receive the item.
     * @param receiver        The name of the receivingPlayer.
     * @param receiverId      The {@link UUID} of the receivingPlayer.
     * @param item            The {@link ItemStack} which will be traded.
     * @return {@link Boolean#TRUE} if this item should be marked as blocked.
     */
    public boolean isBlocked(@NotNull Trade trade, @NotNull Player placer, @Nullable Player receivingPlayer, @NotNull String receiver, @NotNull UUID receiverId, @NotNull ItemStack item) {
        boolean blacklisted = isBlocked(trade, item);

        TradeOfferItemEvent event;
        if (receivingPlayer == null) event = new TradeOfferItemEvent(placer, receiver, receiverId, item, blacklisted);
        else event = new TradeOfferItemEvent(placer, receivingPlayer, item, blacklisted);

        Bukkit.getPluginManager().callEvent(event);
        blacklisted = event.isCancelled();

        return blacklisted;
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
    public BigDecimal getMoneyShortcutFactor(@NotNull String s) {
        String key = s.toLowerCase().replaceAll("[^a-z]", "");
        return moneyShortcuts.get(key);
    }

    @Nullable
    public Map.Entry<String, BigDecimal> getApplicableMoneyShortcut(@NotNull BigDecimal d) {
        Map.Entry<String, BigDecimal> highest = null;

        for (Map.Entry<String, BigDecimal> e : moneyShortcuts.entrySet()) {
            if (d.compareTo(e.getValue()) >= 0) {
                if (highest == null || highest.getValue().compareTo(e.getValue()) < 0) {
                    highest = e;
                }
            }
        }

        return highest;
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

    public DecimalFormat getMoneyPattern() {
        return moneyPattern;
    }

    public boolean isTradeReportItems() {
        return tradeReportItems;
    }

    public boolean isTradeReportEconomy() {
        return tradeReportEconomy;
    }

    public HashMap<String, Trade> getTrades() {
        return trades;
    }

    public boolean tradeProxy() {
        return tradeProxy;
    }
}
