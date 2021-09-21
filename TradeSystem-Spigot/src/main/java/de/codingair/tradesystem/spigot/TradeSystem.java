package de.codingair.tradesystem.spigot;

import de.codingair.codingapi.API;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.FileManager;
import de.codingair.codingapi.files.loader.UTFConfig;
import de.codingair.codingapi.player.chat.ChatButtonManager;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.time.Timer;
import de.codingair.codingapi.utils.Value;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.spigot.commands.TradeCMD;
import de.codingair.tradesystem.spigot.commands.TradeSystemCMD;
import de.codingair.tradesystem.spigot.extras.bstats.MetricsManager;
import de.codingair.tradesystem.spigot.extras.placeholderapi.PAPI;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogOptions;
import de.codingair.tradesystem.spigot.extras.tradelog.commands.TradeLogCMD;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.TradeLogRepository;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters.MysqlTradeLogRepository;
import de.codingair.tradesystem.spigot.extras.tradelog.repository.adapters.SqlLiteTradeLogRepository;
import de.codingair.tradesystem.spigot.trade.TradeHandler;
import de.codingair.tradesystem.spigot.trade.gui.TradeGUIListener;
import de.codingair.tradesystem.spigot.trade.gui.layout.LayoutManager;
import de.codingair.tradesystem.spigot.trade.listeners.ExpirationListener;
import de.codingair.tradesystem.spigot.trade.listeners.JoinNoteListener;
import de.codingair.tradesystem.spigot.trade.listeners.ProxyPayerListener;
import de.codingair.tradesystem.spigot.trade.listeners.TradeListener;
import de.codingair.tradesystem.spigot.trade.managers.CommandManager;
import de.codingair.tradesystem.spigot.trade.managers.InvitationManager;
import de.codingair.tradesystem.spigot.transfer.ProxyDataManager;
import de.codingair.tradesystem.spigot.transfer.SpigotHandler;
import de.codingair.tradesystem.spigot.utils.BackwardSupport;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import de.codingair.tradesystem.spigot.utils.database.DatabaseInitializer;
import de.codingair.tradesystem.spigot.utils.database.DatabaseType;
import de.codingair.tradesystem.spigot.utils.database.DatabaseUtil;
import de.codingair.tradesystem.spigot.utils.database.migrations.mysql.MySQLConnection;
import de.codingair.tradesystem.spigot.utils.updates.NotifyListener;
import de.codingair.tradesystem.spigot.utils.updates.UpdateNotifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.FileNotFoundException;

public class TradeSystem extends JavaPlugin implements Proxy {
    private static TradeSystem instance;

    private final LayoutManager layoutManager = new LayoutManager();
    private final TradeHandler tradeHandler = new TradeHandler();
    private final DatabaseInitializer databaseInitializer = new DatabaseInitializer();
    private final FileManager fileManager = new FileManager(this);

    private final SpigotHandler spigotHandler = new SpigotHandler(this);
    private final ProxyDataManager proxyDataManager = new ProxyDataManager();

    private final UpdateNotifier updateNotifier = new UpdateNotifier();
    private boolean needsUpdate = false;

    private CommandManager commandManager;
    private TradeSystemCMD tradeSystemCMD;
    private TradeLogCMD tradeLogCMD;
    private TradeCMD tradeCMD;

    private UTFConfig oldConfig;

    public static void log(String message) {
        Bukkit.getLogger().info(message);
    }

    public static TradeSystem getInstance() {
        return instance;
    }

    public static SpigotHandler proxyHandler() {
        return instance.spigotHandler;
    }

    public static ProxyDataManager proxy() {
        return instance.proxyDataManager;
    }

    public static TradeHandler man() {
        return instance.tradeHandler;
    }

    public static InvitationManager invitations() {
        return man().getInvitationManager();
    }

    @Override
    public void onEnable() {
        instance = this;
        API.getInstance().onEnable(this);

        printConsoleInfo(() -> {
            loadConfigFiles();
            new BackwardSupport();
            this.commandManager = new CommandManager(getFileManager().getFile("Config"));
            loadManagers();

            //register packet channels before listening to events
            this.spigotHandler.onEnable();

            checkPermissions();
            registerCommands();
            registerListeners();

            //initiates metrics
            new MetricsManager().start();

            afterOnEnable();
            startUpdateNotifier();

            Lang.initializeFile();
        });

        PAPI.register();
        notifyPlayers(null);
    }

    @Override
    public void onDisable() {
        API.getInstance().onDisable(this);
        Bukkit.getScheduler().cancelTasks(this);

        printConsoleInfo(() -> {
            log("  > Cancelling all active trades");
            this.tradeHandler.cancelAll();

            this.tradeCMD.unregister();
            this.tradeSystemCMD.unregister();
            this.tradeLogCMD.unregister();

            //unregister packet channels
            this.spigotHandler.onDisable();
            this.proxyDataManager.onDisable();

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
        if (needsUpdate) {
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
        this.tradeHandler.load();
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
        if (!fileManager.getFile("Config").getConfig().getBoolean("TradeSystem.Permissions", true)) {
            Permissions.PERMISSION = null;
            Permissions.PERMISSION_INITIATE = null;
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new NotifyListener(), this);

        TradeListener tradeListener = new TradeListener();
        Bukkit.getPluginManager().registerEvents(tradeListener, this);
        ChatButtonManager.getInstance().addListener(tradeListener);

        Bukkit.getPluginManager().registerEvents(new ExpirationListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProxyPayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new TradeGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinNoteListener(), this);
    }

    private void registerCommands() {
        tradeCMD = new TradeCMD(this.commandManager.getTradeAliases(), this.commandManager);
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

            if (needsUpdate) {
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
        if (Version.get().isBiggerThan(Version.v1_12)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }
        }
    }

    public void reload() throws FileNotFoundException {
        try {
            API.getInstance().reload(this);
        } catch (InvalidDescriptionException | InvalidPluginException e) {
            e.printStackTrace();
        }
    }

    public void notifyPlayers(Player player) {
        if (player == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                notifyPlayers(p);
            }
        } else {
            if (player.hasPermission(Permissions.PERMISSION_NOTIFY) && needsUpdate) {
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
        this.oldConfig = file.getConfig().copy();
        this.fileManager.unloadFile(file);
    }

    public TradeHandler getTradeManager() {
        return tradeHandler;
    }

    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public UTFConfig getOldConfig() {
        return oldConfig;
    }

    public TradeLogRepository getTradeLogRepository() {
        if (!TradeLogOptions.isEnabled()) {
            return null;
        }

        DatabaseType type = DatabaseUtil.database().getType();
        switch (type) {
            case MYSQL:
                return new MysqlTradeLogRepository(MySQLConnection.getConnection());
            case SQLITE:
                return new SqlLiteTradeLogRepository();
            default:
                throw new RuntimeException("Invalid database type provided: " + type);
        }
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
