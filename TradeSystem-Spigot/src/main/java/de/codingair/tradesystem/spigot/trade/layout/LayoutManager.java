package de.codingair.tradesystem.spigot.trade.layout;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.layout.utils.DefaultPattern;
import de.codingair.tradesystem.spigot.trade.layout.utils.ImportHelper;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.util.*;

public class LayoutManager {
    private final HashMap<String, Pattern> patterns = new HashMap<>();
    private final List<Map<?, ?>> crashedData = new ArrayList<>();
    private String active;

    public void load() {
        this.patterns.clear();

        TradeSystem.log("  > Loading layouts");

        Pattern def = new DefaultPattern();
        this.patterns.put(def.getName(), def);

        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        int standardLayouts = this.patterns.size();
        List<?> dataList = config.getList("Layouts");

        crashedData.clear();
        if (dataList != null) {
            for (Object data : dataList) {
                if (data instanceof Map) {
                    JSON json = new JSON((Map<?, ?>) data);

                    Pattern pattern = new Pattern();
                    try {
                        pattern.read(json);
                        patterns.putIfAbsent(pattern.getName(), pattern);
                    } catch (Exception e) {
                        e.printStackTrace();
                        crashedData.add(json);
                    }
                } else if (data instanceof String) {
                    //old format! (v1.3.2)
                    try {
                        Pattern pattern = ImportHelper.convert((String) data);
                        this.patterns.put(pattern.getName(), pattern);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        this.active = config.getString("Active", DefaultPattern.NAME);

        TradeSystem.log("    ...got " + (this.patterns.size() - standardLayouts) + " layout(s)");
    }

    public void save() {
        TradeSystem.log("  > Saving layouts");
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        List<Map<?, ?>> data = new ArrayList<>();

        for (Pattern pattern : this.patterns.values()) {
            if (pattern.getClass().equals(DefaultPattern.class)) continue;

            JSON json = new JSON();
            pattern.write(json);
            data.add(json);
        }

        data.addAll(crashedData);

        config.set("Layouts", data);
        config.set("Active", this.active);
        file.saveConfig();

        TradeSystem.log("    ...saved " + data.size() + " layout(s)");
    }

    public Pattern getPattern(String name) {
        if (name == null) return null;

        return this.patterns.get(name);
    }

    public boolean addPattern(Pattern pattern) {
        return this.patterns.put(pattern.getName(), pattern) == null;
    }

    public boolean remove(@NotNull Pattern pattern) {
        return this.patterns.remove(pattern.getName()) != null;
    }

    public Pattern getActive() {
        return getPattern(active);
    }

    public void setActive(@NotNull String name) {
        this.active = name;
    }

    public Collection<Pattern> getPatterns() {
        return this.patterns.values();
    }

    public boolean isAvailable(String name) {
        return getPattern(name) == null;
    }
}
