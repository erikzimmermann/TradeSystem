package de.codingair.tradesystem.spigot.extras.bstats;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.ext.Extension;
import de.codingair.tradesystem.spigot.ext.Extensions;
import de.codingair.tradesystem.spigot.extras.blacklist.BlockedItem;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsManager {
    public static int TRADES = 0;

    private static boolean isStandardWorldList(List<String> l) {
        if (l.size() == 2) {
            return "ExampleWorld-1".equals(l.get(0)) && "ExampleWorld-2".equals(l.get(1));
        } else return false;
    }

    private static boolean isStandardBlacklist(List<BlockedItem> l) {
        if (l.size() == 3) {
            return BlockedItem.create().material(Material.AIR).equals(l.get(0))
                    && BlockedItem.create().material(Material.AIR).displayName("&cExample").equals(l.get(1))
                    && BlockedItem.create().displayName("&cExample, which blocks all items with this strange name!").equals(l.get(2));
        } else return false;
    }

    public void start() {
        Metrics metrics = new Metrics(TradeSystem.getInstance(), 6959);

        addConfigurationMetrics(metrics);
        addTradeCountMetrics(metrics);
        addLayoutMetrics(metrics);
        addExtensionsMetrics(metrics);
    }

    private static void addConfigurationMetrics(Metrics metrics) {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
        YamlConfiguration config = file.getConfig();

        metrics.addCustomChart(new AdvancedPie("configuration", () -> {
            Map<String, Integer> map = new HashMap<>();

            map.put("Trade", 1);
            if (config.getBoolean("TradeSystem.Permissions", true)) map.put("Permissions", 1);
            if (!isStandardBlacklist(TradeSystem.getInstance().getTradeManager().getBlacklist())) map.put("Item blacklist", 1);
            if (!isStandardWorldList(TradeSystem.getInstance().getTradeManager().getBlockedWorlds())) map.put("Blocks worlds", 1);

            return map;
        }));
    }

    private static void addTradeCountMetrics(Metrics metrics) {
        metrics.addCustomChart(new SingleLineChart("trades", () -> {
            int trades = TRADES;
            TRADES = 0;
            return trades;
        }));
    }

    private static void addLayoutMetrics(Metrics metrics) {
        metrics.addCustomChart(new SingleLineChart("layouts", () -> TradeSystem.getInstance().getLayoutManager().getPatterns().size() - 1));
    }

    private void addExtensionsMetrics(Metrics metrics) {
        metrics.addCustomChart(new DrilldownPie("extensions", () -> {
            Map<String, Map<String, Integer>> data = new HashMap<>();

            // add extensions
            for (Extension value : Extensions.get().values()) {
                String version = value.getCurrentVersion();
                if (version == null) continue;

                data.put(value.getName(), Collections.singletonMap(version, 1));
            }

            // add TradeProxy
            String tradeProxy = TradeSystem.proxy().getTradeProxyVersion();
            if (tradeProxy != null) {
                data.put("TradeProxy", Collections.singletonMap(tradeProxy, 1));
            }

            return data;
        }));
    }
}
