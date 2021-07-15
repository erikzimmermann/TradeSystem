package de.codingair.tradesystem.spigot.trade.gui.layout;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui.layout.patterns.DefaultEssentialsPattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.patterns.DefaultExpPattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.patterns.DefaultPattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.patterns.DefaultVaultPattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions.TradeIconException;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.ImportHelper;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Name;
import de.codingair.tradesystem.spigot.trade.listeners.JoinNoteListener;
import de.codingair.tradesystem.spigot.utils.Lang;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
        } else stickDefaultPatterNote();

        if (this.patterns.isEmpty()) {
            addDefaultPatterns();
            saveLayouts();
        }

        this.active = config.getString("Active");
        if (this.active == null || !patterns.containsKey(new Name(this.active))) {
            Pattern pattern = getBackupPattern();

            TradeSystem.getInstance().getLogger().log(Level.WARNING, "The active layout " + (active == null ? "null" : "'" + active + "'") + " could not be found. Switching to the default layout: '" + pattern.getName() + "'");
            this.active = pattern.getName();
            saveActiveLayout();
        }

        TradeSystem.log("    ...got " + (this.patterns.size() - standardLayouts) + " layout(s)");
    }

    private void stickDefaultPatterNote() {
        if (!DefaultVaultPattern.valid() && !DefaultEssentialsPattern.valid()) return;

        TextComponent message = new TextComponent(Lang.getPrefix() + "§7You haven't setup a trade layout. Activate one now:");

        message.addExtra("\n§8- §eNon-economy layout §8[");

        TextComponent activateDef = new TextComponent("§7default");
        activateDef.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§8» §aActivate the non-economy layout §8«")}));
        activateDef.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradesystem layout activate " + DefaultPattern.NAME));

        message.addExtra(activateDef);
        message.addExtra("§8]");

        message.addExtra("\n§8- §eExp layout §8[");

        TextComponent activateExp = new TextComponent("§aactivate");
        activateExp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§8» §aActivate the Exp layout §8«")}));
        activateExp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradesystem layout activate " + DefaultExpPattern.NAME));

        message.addExtra(activateExp);
        message.addExtra("§8]");

        if (DefaultVaultPattern.valid()) {
            message.addExtra("\n§8- §eVault layout §8[");

            TextComponent activateVault = new TextComponent("§aactivate");
            activateVault.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§8» §aActivate the Vault layout §8«")}));
            activateVault.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradesystem layout activate " + DefaultVaultPattern.NAME));

            message.addExtra(activateVault);
            message.addExtra("§8]");
        }

        if (DefaultEssentialsPattern.valid()) {
            message.addExtra("\n§8- §eEssentials layout §8[§a");

            TextComponent activateEssentials = new TextComponent("§aactivate");
            activateEssentials.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§8» §aActivate the Essentials layout §8«")}));
            activateEssentials.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradesystem layout activate " + DefaultEssentialsPattern.NAME));

            message.addExtra(activateEssentials);
            message.addExtra("§8]");
        }

        JoinNoteListener.applyNote(message);
    }

    public void saveLayouts() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        List<Map<?, ?>> data = new ArrayList<>();

        serializePatterns(data, this.patterns.values());
        serializePatterns(data, this.incompletePatterns.values());
        data.addAll(crashedPatterns.values());

        config.set("Layouts", data);
        file.saveConfig();

        TradeSystem.getInstance().getLogger().log(Level.INFO, "Saved " + data.size() + " layout(s).");
    }

    public void saveActiveLayout() {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        config.set("Active", this.active);
        file.saveConfig();

        TradeSystem.getInstance().getLogger().log(Level.INFO, "Saved '" + this.active + "' as active layout.");
    }

    private void addDefaultPatterns() {
        TradeSystem.getInstance().getLogger().log(Level.WARNING, "No trade pattern found -> create default patterns");

        Pattern def = new DefaultPattern();
        this.patterns.put(new Name(def.getName()), def);

        def = new DefaultExpPattern();
        this.patterns.put(new Name(def.getName()), def);

        if (DefaultVaultPattern.valid()) {
            def = new DefaultVaultPattern();
            this.patterns.put(new Name(def.getName()), def);
        }

        if (DefaultEssentialsPattern.valid()) {
            def = new DefaultEssentialsPattern();
            this.patterns.put(new Name(def.getName()), def);
        }
    }

    private Pattern getBackupPattern() {
        return patterns.getOrDefault(new Name(DefaultPattern.NAME),
                patterns.getOrDefault(new Name(DefaultExpPattern.NAME),
                        patterns.getOrDefault(new Name(DefaultVaultPattern.NAME),
                                patterns.getOrDefault(new Name(DefaultEssentialsPattern.NAME),
                                        patterns.values().stream().findAny().orElse(null)
                                )
                        )
                )
        );
    }

    private void serializePatterns(List<Map<?, ?>> data, Collection<Pattern> patterns) {
        for (Pattern pattern : patterns) {
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
        saveLayouts();

        return created;
    }

    public void delete(@NotNull Pattern pattern) {
        if (this.patterns.remove(new Name(pattern.getName())) != null) {
            if (this.patterns.isEmpty()) addDefaultPatterns();

            saveLayouts();
        }
    }

    @NotNull
    public Pattern getActive() {
        return getPattern(active);
    }

    public void setActive(@NotNull String name) {
        this.active = name;
        saveActiveLayout();
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

    public Map<Name, Map<?, ?>> getCrashedPatterns() {
        return crashedPatterns;
    }
}
