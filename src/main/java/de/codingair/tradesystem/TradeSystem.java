package de.codingair.tradesystem;

import de.codingair.codingapi.API;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.FileManager;
import de.codingair.codingapi.files.loader.UTFConfig;
import de.codingair.codingapi.player.chat.ChatButtonManager;
import de.codingair.codingapi.server.reflections.IReflection;
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
import de.codingair.tradesystem.tradelog.TradeLogOptions;
import de.codingair.tradesystem.tradelog.commands.TradeLogCMD;
import de.codingair.tradesystem.tradelog.repository.TradeLogRepository;
import de.codingair.tradesystem.tradelog.repository.adapters.MysqlTradeLogRepository;
import de.codingair.tradesystem.tradelog.repository.adapters.SqlLiteTradeLogRepository;
import de.codingair.tradesystem.utils.BackwardSupport;
import de.codingair.tradesystem.utils.Lang;
import de.codingair.tradesystem.utils.Profile;
import de.codingair.tradesystem.utils.database.DatabaseInitializer;
import de.codingair.tradesystem.utils.database.DatabaseType;
import de.codingair.tradesystem.utils.database.DatabaseUtil;
import de.codingair.tradesystem.utils.database.migrations.mysql.MySQLConnection;
import de.codingair.tradesystem.utils.updates.NotifyListener;
import de.codingair.tradesystem.utils.updates.UpdateNotifier;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class TradeSystem extends JavaPlugin {
    public static final String PERMISSION_NOTIFY = "TradeSystem.Notify";
    public static final String PERMISSION_MODIFY = "TradeSystem.Modify";
    public static final String PERMISSION_LOG = "TradeSystem.Log";

    private static TradeSystem instance;

    private final LayoutManager layoutManager = new LayoutManager();
    private final TradeManager tradeManager = new TradeManager();
    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final FileManager fileManager = new FileManager(this);

    private final UpdateNotifier updateNotifier = new UpdateNotifier();
    private boolean needsUpdate = false;

    private TradeSystemCMD tradeSystemCMD;
    private TradeLogCMD tradeLogCMD;
    private TradeCMD tradeCMD;
    private UTFConfig oldConfig;

    @Override
    public void onEnable() {
        instance = this;
        Version.load();
        API.getInstance().onEnable(this);

        printConsoleInfo(() -> {
            loadConfigFiles();
            loadManagers();
            registerListeners();
            checkPermissions();
            registerCommands();

            //initiates metrics
            new MetricsManager().start();

            afterOnEnable();
            startUpdateNotifier();

            new BackwardSupport();
            Lang.initializeFile();
        });

        PAPI.register();
        notifyPlayers(null);
    }

    @Override
    public void onDisable() {
        API.getInstance().onDisable(this);

        printConsoleInfo(() -> {
            log("  > Cancelling all active trades");
            this.tradeManager.cancelAll();
            this.layoutManager.save();

            this.tradeCMD.unregister();
            this.tradeSystemCMD.unregister();
            this.tradeLogCMD.unregister();

            HandlerList.unregisterAll(this);
            this.fileManager.destroy();
        });
    }

    private void printConsoleInfo(Runnable runnable) {
        Timer timer = new Timer();
        timer.start();

        log(" ");
        log("__________________________________________________________");
        log(" ");
        log("                       TradeSystem [" + getDescription().getVersion() + "]");
        if(needsUpdate) {
            log(" ");
            log("New update available [v" + updateNotifier.getVersion() + " - " + updateNotifier.getUpdateInfo() + "]. Download it on \n\n" + updateNotifier.getDownloadLink() + "\n");
        }
        log(" ");
        log("Status:");
        log(" ");
        log("MC-Version: " + Version.get().fullVersion());
        log(" ");

        runnable.run();

        log(" ");
        log("Finished (" + timer.result() + ")");
        log(" ");
        log("__________________________________________________________");
        log(" ");
    }

    private void loadManagers() {
        this.tradeManager.load();
        this.layoutManager.load();
        this.databaseInitializer.initialize();
    }

    private void loadConfigFiles() {
        copyConfig();

        this.fileManager.loadFile("Config", "/");
        this.fileManager.loadFile("Layouts", "/");

        Lang.initPreDefinedLanguages(this);
    }

    private void checkPermissions() {
        if(!fileManager.getFile("Config").getConfig().getBoolean("TradeSystem.Permissions", true)) {
            TradeCMD.PERMISSION = null;
            TradeCMD.PERMISSION_INITIATE = null;
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new NotifyListener(), this);
        TradeListener listener;
        Bukkit.getPluginManager().registerEvents(listener = new TradeListener(), this);
        ChatButtonManager.getInstance().addListener(listener);
    }

    private void registerCommands() {
        tradeCMD = new TradeCMD();
        tradeCMD.register();

        tradeSystemCMD = new TradeSystemCMD();
        tradeSystemCMD.register();

        tradeLogCMD = new TradeLogCMD();
        tradeLogCMD.register();
    }

    private void startUpdateNotifier() {
        Value<BukkitTask> task = new Value<>(null);
        Runnable runnable = () -> {
            needsUpdate = updateNotifier.read();

            if(needsUpdate) {
                log("-----< TradeSystem >-----");
                log("New update available [" + updateNotifier.getUpdateInfo() + "].");
                log("Download it on\n\n" + updateNotifier.getDownloadLink() + "\n");
                log("------------------------");

                task.getValue().cancel();
            }
        };

        task.setValue(Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), runnable, 20L * 60 * 2, 20L * 60 * 60)); //check every hour on GitHub
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
        Bukkit.getLogger().info(message);
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
                player.sendMessage(Lang.getPrefix() + "§aA new update is available §8[§b" + updateNotifier.getUpdateInfo() + "§8]§a. Download it on §b§n" + this.updateNotifier.getDownloadLink());
                player.sendMessage("");
                player.sendMessage("");
            }
        }
    }

    private void copyConfig() {
        ConfigFile file = this.fileManager.loadFile("Config", "/", false);

        IReflection.FieldAccessor<Map<String, Object>> map = IReflection.getField(MemorySection.class, "map");
        Map<String, Object> copy = new HashMap<>(map.get(file.getConfig()));

        //noinspection ConstantConditions
        this.oldConfig = (UTFConfig) IReflection.getConstructor(UTFConfig.class).newInstance();
        map.set(oldConfig, copy);

        this.fileManager.unloadFile(file);
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

    public static TradeManager man() {
        return instance.tradeManager;
    }

    public UTFConfig getOldConfig() {
        return oldConfig;
    }

    public TradeLogRepository getTradeLogRepository() {
        if(!TradeLogOptions.isEnabled()) {
            return null;
        }

        DatabaseType type = DatabaseUtil.database().getType();
        switch(type) {
            case MYSQL:
                return new MysqlTradeLogRepository(MySQLConnection.getConnection());
            case SQLITE:
                return new SqlLiteTradeLogRepository();
            default:
                throw new RuntimeException("Invalid database type provided: " + type);
        }
    }
}
