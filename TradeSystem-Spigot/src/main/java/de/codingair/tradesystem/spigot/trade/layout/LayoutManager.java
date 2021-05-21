package de.codingair.tradesystem.spigot.trade.layout;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.utils.DefaultPattern;
import de.codingair.tradesystem.spigot.trade.layout.utils.AbstractPattern;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LayoutManager {
    private final List<String> reservedNames = new ArrayList<>();
    private final List<Pattern> layouts = new ArrayList<>();
    private Pattern active;

    public void load() {
        this.layouts.clear();

        TradeSystem.log("  > Loading layouts");
//        this.layouts.add(new Standard());
        this.active = new DefaultPattern();

        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        List<String> dataList = config.getStringList("Layouts");
        int standardLayouts = this.layouts.size();

//        for (String data : dataList) {
//            AbstractPattern ap = AbstractPattern.getFromJSONString(data);
//            if (ap != null) this.layouts.add(ap);
//        }

//        setActive(getPattern(config.getString("Active", null)));

        TradeSystem.log("    ...got " + (this.layouts.size() - standardLayouts) + " layout(s)");
    }

    public void save() {
        TradeSystem.log("  > Saving layouts");
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        List<String> data = new ArrayList<>();

//        for (Pattern layout : this.layouts) {
//            if (layout.isStandard()) continue;
//            data.add(layout.toJSONString());
//        }

        config.set("Layouts", data);
//        config.set("Active", this.active.getName());
        file.saveConfig();

        TradeSystem.log("    ...saved " + data.size() + " layout(s)");
    }

    public AbstractPattern getPattern(String name) {
        if (name == null) return null;

        for (Pattern layout : this.layouts) {
//            if (layout.getName().equals(name)) return layout;
        }

        throw new IllegalStateException("Not implemented yet");
//        return null;
    }

    public void addPattern(AbstractPattern pattern) {
//        this.layouts.add(pattern);
        throw new IllegalStateException("Not implemented yet");
    }

    public boolean remove(@NotNull AbstractPattern pattern) {
//        return this.layouts.remove(pattern);
        throw new IllegalStateException("Not implemented yet");
    }

    public Pattern getActive() {
        return active;
    }

    public void setActive(de.codingair.tradesystem.spigot.trade.layout.utils.Pattern active) {
        if (active == null) return;
//        this.active = active;
        throw new IllegalStateException("Not implemented yet");
    }

    public List<AbstractPattern> getLayouts() {
//        return layouts;
        throw new IllegalStateException("Not implemented yet");
    }

    public boolean isAvailable(String name) {
        return getPattern(name) == null && !this.reservedNames.contains(name);
    }

    public void setAvailable(String name, boolean available) {
        if (!available && !this.reservedNames.contains(name)) this.reservedNames.add(name);
        else this.reservedNames.remove(name);
    }
}
