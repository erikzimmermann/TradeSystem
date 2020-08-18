package de.codingair.tradesystem.trade.layout;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.trade.layout.layouts.Standard;
import de.codingair.tradesystem.trade.layout.utils.AbstractPattern;
import de.codingair.tradesystem.trade.layout.utils.Pattern;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class LayoutManager {
    private final List<String> reservedNames = new ArrayList<>();
    private final List<AbstractPattern> layouts = new ArrayList<>();
    private Pattern active;

    public void load() {
        this.layouts.clear();

        TradeSystem.log("  > Loading layouts");
        this.layouts.add(new Standard());
        this.active = getPattern("Standard");

        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        List<String> dataList = config.getStringList("Layouts");

        int amount = this.layouts.size();

        if(dataList != null) {
            for(String data : dataList) {
                AbstractPattern ap = AbstractPattern.getFromJSONString(data);
                if(ap != null) this.layouts.add(ap);
            }
        }

        setActive(getPattern(config.getString("Active", null)));

        TradeSystem.log("    ...got " + (this.layouts.size() - amount) + " layout(s)");
    }

    public void save() {
        TradeSystem.log("  > Saving layouts");
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Layouts");
        FileConfiguration config = file.getConfig();

        List<String> data = new ArrayList<>();

        for(AbstractPattern layout : this.layouts) {
            if(layout.isStandard()) continue;
            data.add(layout.toJSONString());
        }

        config.set("Layouts", data);
        config.set("Active", this.active.getName());
        file.saveConfig();

        TradeSystem.log("    ...saved " + data.size() + " layout(s)");
    }

    public AbstractPattern getPattern(String name) {
        if(name == null) return null;

        for(AbstractPattern layout : this.layouts) {
            if(layout.getName().equals(name)) return layout;
        }

        return null;
    }

    public void addPattern(AbstractPattern pattern) {
        this.layouts.add(pattern);
    }

    public boolean remove(String name) {
        AbstractPattern pattern = getPattern(name);
        return pattern != null && this.layouts.remove(pattern);
    }

    public Pattern getActive() {
        return active;
    }

    public void setActive(Pattern active) {
        if(active == null) return;
        this.active = active;
    }

    public List<AbstractPattern> getLayouts() {
        return layouts;
    }

    public boolean isAvailable(String name) {
        return getPattern(name) == null && !this.reservedNames.contains(name);
    }

    public void setAvailable(String name, boolean available) {
        if(!available && !this.reservedNames.contains(name)) this.reservedNames.add(name);
        else this.reservedNames.remove(name);
    }
}
