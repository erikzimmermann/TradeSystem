package de.codingair.tradesystem.spigot;

import de.codingair.codingapi.API;
import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.FileManager;
import de.codingair.codingapi.player.chat.ChatButtonManager;
import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.utils.Value;
import de.codingair.packetmanagement.utils.Proxy;
import de.codingair.tradesystem.spigot.commands.TradeCMD;
import de.codingair.tradesystem.spigot.commands.TradeSystemCMD;
import de.codingair.tradesystem.spigot.database.DatabaseHandler;
import de.codingair.tradesystem.spigot.ext.Extensions;
import de.codingair.tradesystem.spigot.extras.bstats.MetricsManager;
import de.codingair.tradesystem.spigot.extras.external.PluginDependencies;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogCMD;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogListener;
import de.codingair.tradesystem.spigot.trade.TradeHandler;
import de.codingair.tradesystem.spigot.trade.gui.TradeGUIListener;
import de.codingair.tradesystem.spigot.trade.gui.layout.LayoutManager;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.IconController;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.IconHandler;
import de.codingair.tradesystem.spigot.trade.listeners.*;
import de.codingair.tradesystem.spigot.trade.managers.CommandManager;
import de.codingair.tradesystem.spigot.trade.managers.InvitationManager;
import de.codingair.tradesystem.spigot.transfer.ProxyDataHandler;
import de.codingair.tradesystem.spigot.transfer.ProxyDataListener;
import de.codingair.tradesystem.spigot.transfer.SpigotHandler;
import de.codingair.tradesystem.spigot.utils.BackwardSupport;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import de.codingair.tradesystem.spigot.utils.updates.NotifyListener;
import de.codingair.tradesystem.spigot.utils.updates.UpdateNotifier;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class TradeSystem extends JavaPlugin implements Proxy {
    private static TradeSystem instance;
    private static IconController iconController;

    private final LayoutManager layoutManager = new LayoutManager();
    private final TradeHandler tradeHandler = new TradeHandler();
    private final DatabaseHandler databaseHandler = new DatabaseHandler();
    private final FileManager fileManager = new FileManager(this);

    private final SpigotHandler spigotHandler = new SpigotHandler(this);
    private final ProxyDataHandler proxyDataHandler = new ProxyDataHandler();

    private final UpdateNotifier updateNotifier = new UpdateNotifier(getDescription().getVersion(), "TradeSystem", 58434);
    private boolean needsUpdate = false;

    private CommandManager commandManager;
    private TradeSystemCMD tradeSystemCMD;
    private TradeLogCMD tradeLogCMD;
    private TradeCMD tradeCMD;

    private boolean firstSetup;
    private YamlConfiguration oldConfig;

    public static void log(String message) {
        Bukkit.getLogger().info(message);
    }

    public static TradeSystem getInstance() {
        return instance;
    }

    public static SpigotHandler proxyHandler() {
        return instance.spigotHandler;
    }

    public static ProxyDataHandler proxy() {
        return instance.proxyDataHandler;
    }

    public static DatabaseHandler database() {
        return instance.databaseHandler;
    }

    public static TradeHandler handler() {
        return instance.tradeHandler;
    }

    public static InvitationManager invitations() {
        return handler().getInvitationManager();
    }

    @Override
    public void onEnable() {
        instance = this;
        API.getInstance().onEnable(this);

        printConsoleInfo(() -> {
            loadConfigFiles();
            new BackwardSupport();

            IconHandler.init();  // create icon controller
            iconController.registerDefault();

            PluginDependencies.enable();
            registerDefaultPluginMessagingChannel();

            this.commandManager = new CommandManager(getFileManager().getFile("Config"));
            loadManagers();

            //register packet channels before listening to events
            this.spigotHandler.onEnable();
            this.proxyDataHandler.onEnable();

            Permissions.checkPermissions(firstSetup);
            registerCommands();
            registerListeners();

            //initiates metrics
            new MetricsManager().start();

            afterOnEnable();
            startUpdateNotifier();
        });

        notifyPlayers(null);
    }

    @Override
    public void onDisable() {
        API.getInstance().onDisable(this);
        Bukkit.getScheduler().cancelTasks(this);

        printConsoleInfo(() -> {
            this.tradeHandler.disable();

            this.tradeCMD.unregister();
            this.tradeSystemCMD.unregister();
            if (this.tradeLogCMD != null) this.tradeLogCMD.unregister();

            //unregister packet channels
            this.spigotHandler.onDisable();
            this.proxyDataHandler.onDisable();

            HandlerList.unregisterAll(this);
            this.fileManager.destroy();

            PluginDependencies.disable();

            if (iconController != null) iconController.clear();
        });
    }

    private void printConsoleInfo(Runnable runnable) {
        long start = System.currentTimeMillis();

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
        log("Finished (" + (System.currentTimeMillis() - start) + "ms)");
        log(" ");
        log("__________________________________________________________");
        log(" ");
    }

    private void loadManagers() {
        this.tradeHandler.load();
        this.layoutManager.load();
        this.databaseHandler.load();
    }

    private void registerDefaultPluginMessagingChannel() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void loadConfigFiles() {
        copyConfig();

        this.fileManager.loadFile("Config", "/");
        this.fileManager.loadFile("Layouts", "/");

        Lang.init(this, fileManager);
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
        Bukkit.getPluginManager().registerEvents(new PublishSkinListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProxyDataListener(), this);
        Bukkit.getPluginManager().registerEvents(new TradeLogListener(), this);
    }

    private void registerCommands() {
        tradeCMD = new TradeCMD(this.commandManager.getTradeAliases(), this.commandManager);
        tradeCMD.register();

        tradeSystemCMD = new TradeSystemCMD();
        tradeSystemCMD.register();

        if (!Extensions.TradeAudit.isEnabled()) {
            tradeLogCMD = new TradeLogCMD();
            tradeLogCMD.register();
        }
    }

    private void startUpdateNotifier() {
        Value<BukkitTask> task = new Value<>(null);
        Runnable runnable = () -> {
            needsUpdate = updateNotifier.read();

            if (needsUpdate) {
                log("-----< TradeSystem >-----");
                log("New update available [" + updateNotifier.getUpdateInfo() + "].");
                log("You are " + updateNotifier.getReleasesBehind() + " release(s) behind.");
                log("Download it on\n\n" + updateNotifier.getDownloadLink() + "\n");
                log("------------------------");

                task.getValue().cancel();
            }
        };

        task.setValue(Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), runnable, 20L * 60 * 60, 20L * 60 * 60)); //check every hour on GitHub
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
        onDisable();
        onEnable();
    }

    public void notifyPlayers(Player player) {
        if (player == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                notifyPlayers(p);
            }
        } else {
            if (needsUpdate && player.hasPermission(Permissions.PERMISSION_NOTIFY)) {
                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage(Lang.getPrefix() + "§7A §anew update §7is available §8[§b" + updateNotifier.getUpdateInfo() + "§8]§7. You are §a" + updateNotifier.getReleasesBehind() + "§7 release(s) behind. Download it on §b§n" + this.updateNotifier.getDownloadLink());
                player.sendMessage("");
                player.sendMessage("");
            }
        }
    }

    private void copyConfig() {
        this.firstSetup = !getDataFolder().exists();

        ConfigFile file = this.fileManager.loadFile("Config", "/", false, true);
        this.oldConfig = file.getConfig();
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

    public YamlConfiguration getOldConfig() {
        return oldConfig;
    }

    public DatabaseHandler getDatabaseInitializer() {
        return databaseHandler;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public static void setIconController(@NotNull IconController iconController) {
        TradeSystem.iconController = iconController;
    }
}
