package de.codingair.tradesystem.spigot.trade.layout;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.layout.registration.exceptions.TradeIconException;
import de.codingair.tradesystem.spigot.trade.layout.utils.DefaultPattern;
import de.codingair.tradesystem.spigot.trade.layout.utils.ImportHelper;
import de.codingair.tradesystem.spigot.trade.layout.utils.Name;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.logging.Level;

public class LayoutManager {
    private final HashMap<Name, Pattern> patterns = new HashMap<>();
    private final Map<Name, Pattern> incompletePatterns = new HashMap<>();
    private final Map<Name, Map<?, ?>> crashedPatterns = new HashMap<>();
    private String active;

    public void load() {
        this.patterns.clear();

        TradeSystem.log("  > Loading layouts");

        Pattern def = new DefaultPattern();
        this.patterns.put(new Name(def.getName()), def);

        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        int standardLayouts = this.patterns.size();
        List<?> dataList = config.getList("Layouts");

        incompletePatterns.clear();
        crashedPatterns.clear();
        if (dataList != null) {
            for (Object data : dataList) {
                if (data instanceof Map) {
                    JSON json = new JSON((Map<?, ?>) data);

                    Pattern pattern = new Pattern();
                    try {
                        pattern.read(json);
                        patterns.putIfAbsent(new Name(pattern.getName()), pattern);
                    } catch (TradeIconException e) {
                        TradeSystem.getInstance().getLogger().log(Level.SEVERE, "A layout could not been loaded due to an error: " + e.getMessage());
                        incompletePatterns.put(new Name(pattern.getName()), pattern);
                    } catch (Exception e) {
                        e.printStackTrace();
                        crashedPatterns.put(new Name(Pattern.deserializeName(json)), json);
                    }
                } else if (data instanceof String) {
                    //old format! (v1.3.2)
                    try {
                        Pattern pattern = ImportHelper.convert((String) data);
                        this.patterns.put(new Name(pattern.getName()), pattern);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        this.active = config.getString("Active", DefaultPattern.NAME);
        if (this.active == null || !patterns.containsKey(new Name(this.active))) {
            TradeSystem.getInstance().getLogger().log(Level.SEVERE, "The active layout could not be found. Switching to the default layout.");
            this.active = DefaultPattern.NAME;
        }

        TradeSystem.log("    ...got " + (this.patterns.size() - standardLayouts) + " layout(s)");
    }

    public void save() {
        TradeSystem.log("  > Saving layouts");
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        List<Map<?, ?>> data = new ArrayList<>();

        serializePatterns(data, this.patterns.values());
        serializePatterns(data, this.incompletePatterns.values());
        data.addAll(crashedPatterns.values());

        config.set("Layouts", data);
        config.set("Active", this.active);
        file.saveConfig();

        TradeSystem.log("    ...saved " + data.size() + " layout(s)");
    }

    private void serializePatterns(List<Map<?, ?>> data, Collection<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.getClass().equals(DefaultPattern.class)) continue;

            JSON json = new JSON();
            pattern.write(json);
            data.add(json);
        }
    }

    public Pattern getPattern(@Nullable String name) {
        return getPattern(name, false);
    }

    public Pattern getPattern(@Nullable String name, boolean incomplete) {
        if (name == null) return null;
        Pattern pattern = this.patterns.get(new Name(name));

        if (pattern == null && incomplete) return this.incompletePatterns.get(new Name(name));
        else return pattern;
    }

    public boolean addPattern(Pattern pattern) {
        boolean created = this.patterns.put(new Name(pattern.getName()), pattern) == null;
        save();

        return created;
    }

    public boolean remove(@NotNull Pattern pattern) {
        return this.patterns.remove(new Name(pattern.getName())) != null;
    }

    @NotNull
    public Pattern getActive() {
        return getPattern(active);
    }

    public void setActive(@NotNull String name) {
        this.active = name;
    }

    public Collection<Pattern> getPatterns() {
        return getPatterns(false);
    }

    public Collection<Pattern> getPatterns(boolean incomplete) {
        List<Pattern> patterns = new ArrayList<>(this.patterns.values());

        if (incomplete) patterns.addAll(this.incompletePatterns.values());

        return patterns;
    }

    public boolean isAvailable(@Nullable String name) {
        if (name == null) return true;
        return getPattern(name) == null && !incompletePatterns.containsKey(new Name(name)) && !crashedPatterns.containsKey(new Name(name));
    }

    public Map<Name, Pattern> getIncompletePatterns() {
        return incompletePatterns;
    }

    public Map<Name, Map<?, ?>> getCrashedPatterns() {
        return crashedPatterns;
    }
}
