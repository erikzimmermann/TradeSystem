package de.codingair.tradesystem;

import de.codingair.codingapi.files.FileManager;
import de.codingair.tradesystem.trade.Trade;
import de.codingair.tradesystem.trade.TradeCMD;
import de.codingair.tradesystem.trade.TradeManager;
import de.codingair.tradesystem.utils.Lang;
import de.codingair.tradesystem.utils.Profile;
import de.codingair.tradesystem.utils.updates.NotifyListener;
import de.codingair.tradesystem.utils.updates.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TradeSystem extends JavaPlugin {
    public static final String PERMISSION_NOTIFY = "WarpSystem.Notify";

    private static TradeSystem instance;
    private TradeManager tradeManager = new TradeManager();
    private FileManager fileManager = new FileManager(this);
    private UpdateChecker updateChecker = new UpdateChecker("https://www.spigotmc.org/resources/trade-system-only-gui.58434/history");
    private boolean needsUpdate = false;

    @Override
    public void onEnable() {
        instance = this;

        this.needsUpdate = updateChecker.needsUpdate();

        log(" ");
        log("__________________________________________________________");
        log(" ");
        log("                       TradeSystem [" + getDescription().getVersion() + "]");
        if(needsUpdate) {
            log(" ");
            log("New update available [v" + updateChecker.getVersion() + " - " + updateChecker.getUpdateInfo() + "].");
            log("Download it on\n\n" + updateChecker.getDownload() + "\n");
        }
        log(" ");
        log("__________________________________________________________");
        log(" ");


        Bukkit.getPluginManager().registerEvents(new NotifyListener(), this);
        this.fileManager.loadFile("Language", "/");
        new TradeCMD().register(this);

        notifyPlayers(null);
    }

    @Override
    public void onDisable() {
        this.tradeManager.cancelAll();
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
                player.sendMessage(Lang.getPrefix() + "§aA new update is available §8[§bv" + TradeSystem.getInstance().updateChecker.getVersion() + "§8 - §b" + TradeSystem.getInstance().updateChecker.getUpdateInfo() + "§8]§a. Download it on §b§nhttps://www.spigotmc.org/resources/warpsystem-gui.29595/history");
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

    public FileManager getFileManager() {
        return fileManager;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }
}
