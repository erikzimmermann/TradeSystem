package de.codingair.tradesystem.trade.layout;

import de.codingair.tradesystem.trade.layout.layouts.Standard;

import java.util.ArrayList;
import java.util.List;

public class LayoutManager {
    private List<Pattern> layouts = new ArrayList<>();
    private Pattern active;

    public LayoutManager() {
        this.layouts.add(new Standard());
        this.active = getPattern("Standard");
    }

    public void load() {

    }

    public void save() {

    }

    public Pattern getPattern(String name) {
        for(Pattern layout : this.layouts) {
            if(layout.getName().equals(name)) return layout;
        }

        return null;
    }

    public Pattern getActive() {
        return active;
    }

    public List<Pattern> getLayouts() {
        return layouts;
    }
}
