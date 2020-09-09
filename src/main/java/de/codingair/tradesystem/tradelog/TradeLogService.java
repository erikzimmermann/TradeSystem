package de.codingair.tradesystem.tradelog;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.tradelog.repository.TradeLogRepository;
import de.codingair.tradesystem.tradelog.repository.adapters.SqlLiteTradeLogRepository;
import de.codingair.tradesystem.utils.database.DatabaseType;
import de.codingair.tradesystem.utils.database.DatabaseUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TradeLogService {

    private static TradeLogService instance;
    private static final String SQLITE = "SQLITE";

    private ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
    private FileConfiguration config = file.getConfig();
    private boolean enabled = config.getBoolean("TradeLog.enabled", false);

    private TradeLogRepository tradeLogRepository;

    public static TradeLogService getTradeLog() {
        if(instance == null) {
            instance = new TradeLogService();
        }
        return instance;
    }

    private TradeLogService() {
        DatabaseType type = DatabaseUtil.database().getType();
        if (type == DatabaseType.SQLITE) {
            this.tradeLogRepository = new SqlLiteTradeLogRepository();
        } else {
            throw new RuntimeException("Invalid database type provided: " + type);
        }
    }

    public void log(Player player1, Player player2, String message) {
        if(enabled) {
            tradeLogRepository.log(player1, player2, message);
        }
    }

    public List<TradeLog> getLogMessages(String playerName) {
        if(enabled) {
            return tradeLogRepository.getLogMessages(playerName);
        }
        return Collections.emptyList();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
