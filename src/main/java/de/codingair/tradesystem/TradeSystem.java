package de.codingair.tradesystem;

import de.codingair.codingapi.API;
import de.codingair.codingapi.files.FileManager;
import de.codingair.codingapi.player.chat.ChatButtonManager;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.time.Timer;
import de.codingair.codingapi.utils.Value;
import de.codingair.tradesystem.extras.bstats.MetricsManager;
import de.codingair.tradesystem.extras.placeholderapi.PAPI;
import de.codingair.tradesystem.trade.TradeManager;
import de.codingair.tradesystem.trade.commands.TradeCMD;
import de.codingair.tradesystem.trade.commands.TradeSystemCMD;
import de.codingair.tradesystem.trade.layout.LayoutManager;
import de.codingair.tradesystem.trade.listeners.TradeListener;
import de.codingair.tradesystem.utils.Lang;
import de.codingair.tradesystem.utils.Profile;
import de.codingair.tradesystem.utils.updates.NotifyListener;
import de.codingair.tradesystem.utils.updates.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.FileNotFoundException;

public class TradeSystem extends JavaPlugin {
    public static final String PERMISSION_NOTIFY = "TradeSystem.Notify";
    public static final String PERMISSION_MODIFY = "TradeSystem.Modify";

    private static TradeSystem instance;
    private final LayoutManager layoutManager = new LayoutManager();
    private final TradeManager tradeManager = new TradeManager();
    private final FileManager fileManager = new FileManager(this);

    private final UpdateChecker updateNotifier = new UpdateChecker("https://www.spigotmc.org/resources/trade-system-only-gui.58434/history");
    private boolean needsUpdate = false;
    private final Timer timer = new Timer();

    private TradeSystemCMD tradeSystemCMD;
    private TradeCMD tradeCMD;

    @Override
    public void onEnable() {
        Version.load();
        API.getInstance().onEnable(this);
        instance = this;

        timer.start();

        log(" ");
        log("__________________________________________________________");
        log(" ");
        log("                       TradeSystem [" + getDescription().getVersion() + "]");
        log(" ");
        log("Status:");
        log(" ");
        log("MC-Version: " + Version.get().fullVersion());
        log(" ");

        this.fileManager.loadFile("Config", "/");
        this.fileManager.loadFile("Layouts", "/");

        PAPI.register();
        Lang.initPreDefinedLanguages(this);

        this.tradeManager.load();
        this.layoutManager.load();

        Bukkit.getPluginManager().registerEvents(new NotifyListener(), this);
        TradeListener listener;
        Bukkit.getPluginManager().registerEvents(listener = new TradeListener(), this);
        ChatButtonManager.getInstance().addListener(listener);

        if(!fileManager.getFile("Config").getConfig().getBoolean("TradeSystem.Permissions", true)) {
            TradeCMD.PERMISSION = null;
            TradeCMD.PERMISSION_INITIATE = null;
        }

        tradeCMD = new TradeCMD();
        tradeCMD.register();

        tradeSystemCMD = new TradeSystemCMD();
        tradeSystemCMD.register();

        //initiates metrics
        new MetricsManager();

        afterOnEnable();
        startUpdateNotifier();

        log(" ");
        log("Done (" + timer.result() + ")");
        log(" ");
        log("__________________________________________________________");
        log(" ");

        notifyPlayers(null);
    }

    @Override
    public void onDisable() {
        timer.start();
        API.getInstance().onDisable(this);

        log(" ");
        log("__________________________________________________________");
        log(" ");
        log("                       TradeSystem [" + getDescription().getVersion() + "]");
        if(needsUpdate) {
            log(" ");
            log("New update available [v" + updateNotifier.getVersion() + " - " + TradeSystem.this.updateNotifier.getUpdateInfo() + "]. Download it on \n\n" + updateNotifier.getDownload() + "\n");
        }
        log(" ");
        log("Status:");
        log(" ");
        log("MC-Version: " + Version.get().name());
        log(" ");
        log("  > Cancelling all active trades");
        this.tradeManager.cancelAll();
        this.layoutManager.save();

        this.tradeCMD.unregister();
        this.tradeSystemCMD.unregister();

        HandlerList.unregisterAll(this);

        log(" ");
        log("Done (" + timer.result() + ")");
        log(" ");
        log("__________________________________________________________");
        log(" ");

        this.fileManager.destroy();
    }

    private void startUpdateNotifier() {
        Value<BukkitTask> task = new Value<>(null);
        Runnable runnable = () -> {
            needsUpdate = updateNotifier.needsUpdate();

            if(needsUpdate) {
                String v = updateNotifier.getVersion();
                if(!v.startsWith("v")) v = "v" + v;

                log("-----< TradeSystem >-----");
                log("New update available [" + v + " - " + updateNotifier.getUpdateInfo() + "].");
                log("Download it on\n\n" + updateNotifier.getDownload() + "\n");
                log("------------------------");

                task.getValue().cancel();
            }
        };

        task.setValue(Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), runnable, 20L * 5, 5 * 60 * 20L));
    }

    private void afterOnEnable() {
        //update command dispatcher for players to synchronize CommandList
        Bukkit.getScheduler().runTask(this, this::updateCommandList);
    }

    private void updateCommandList() {
        if(Version.get().isBiggerThan(Version.v1_12)) {
            for(Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }
        }
    }

    public void reload() throws FileNotFoundException {
        try {
            API.getInstance().reload(this);
        } catch(InvalidDescriptionException | InvalidPluginException e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        System.out.println(message);
    }

    public void notifyPlayers(Player player) {
        if(player == null) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                notifyPlayers(p);
            }
        } else {
            if(player.hasPermission(TradeSystem.PERMISSION_NOTIFY) && needsUpdate) {
                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage(Lang.getPrefix() + "§aA new update is available §8[§bv" + TradeSystem.getInstance().updateNotifier.getVersion() + "§8 - §b" + TradeSystem.getInstance().updateNotifier.getUpdateInfo() + "§8]§a. Download it on §b§n" + this.updateNotifier.getLink());
                player.sendMessage("");
                player.sendMessage("");
            }
        }
    }

    public static TradeSystem getInstance() {
        return instance;
    }

    public static Profile getProfile(Player player) {
        return new Profile(player);
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public TradeCMD getTradeCMD() {
        return tradeCMD;
    }
}
