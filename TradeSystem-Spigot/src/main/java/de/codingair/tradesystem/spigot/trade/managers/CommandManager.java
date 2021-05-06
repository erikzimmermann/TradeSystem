package de.codingair.tradesystem.spigot.trade.managers;

import de.codingair.codingapi.files.ConfigFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    private final ConfigFile configFile;

    public CommandManager(ConfigFile configFile) {
        this.configFile = configFile;
    }

    @NotNull
    public String[] getTradeAliases() {
        return getAliases("Trade");
    }

    @NotNull
    public String[] getAcceptAliases() {
        return getAliases("Accept");
    }

    @NotNull
    public String[] getDenyAliases() {
        return getAliases("Deny");
    }

    @NotNull
    public String[] getToggleAliases() {
        return getAliases("Toggle");
    }

    @NotNull
    private String[] getAliases(String configTag) {
        List<String> aliases = new ArrayList<>();
        String path = "TradeSystem.Commands." + configTag;

        List<?> l = this.configFile.getConfig().getList(path);
        if (l != null) {
            for (Object o : l) {
                if (o instanceof String) {
                    aliases.add((String) o);
                }
            }
        }

        System.out.println("Amount of '" + configTag + "': " + aliases.size());
        System.out.println(Arrays.toString(aliases.toArray()));

        if (aliases.isEmpty()) {
            String def = configTag.toLowerCase();
            aliases.add(def);
            this.configFile.getConfig().set(path, aliases);
            this.configFile.saveConfig();
        }
        System.out.println(Arrays.toString(aliases.toArray()));
        System.out.println("");

        return aliases.toArray(new String[0]);
    }
}
