package de.codingair.tradesystem.utils;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.TradeSystem;
import de.codingair.tradesystem.extras.placeholderapi.PAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Lang {
    private static void deleteEmptyFiles(JavaPlugin plugin) {
        File folder = new File(plugin.getDataFolder(), "/Languages/");

        if(folder.exists()) {
            for(File file : folder.listFiles()) {
                if(file.length() == 0) file.delete();
            }
        }
    }

    public static void initPreDefinedLanguages(JavaPlugin plugin) {
        deleteEmptyFiles(plugin);

        List<String> languages = new ArrayList<>();
        languages.add("ENG.yml");
        languages.add("ES.yml");
        languages.add("FR.yml");
        languages.add("GER.yml");
        languages.add("POL.yml");
        languages.add("RUS.yml");

        File folder = new File(plugin.getDataFolder(), "/Languages/");
        if(!folder.exists()) mkDir(folder);

        for(String language : languages) {
            InputStream is = plugin.getResource("languages/" + language);

            File file = new File(plugin.getDataFolder() + "/Languages/", language);
            if(!file.exists()) {
                try {
                    file.createNewFile();
                    copy(is, new FileOutputStream(file));
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getPrefix() {
        String prefix = getConfig().getString("TradeSystem.Prefix", "&8» &r");
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        return prefix;
    }

    public static String getLanguageKey() {
        return getConfig().getString("TradeSystem.Language", "ENG");
    }

    private static FileConfiguration getLanguageFile(String langTag) {
        try {
            ConfigFile file = TradeSystem.getInstance().getFileManager().getFile(langTag);
            if(file == null) {
                TradeSystem.getInstance().getFileManager().loadFile(langTag, "/Languages/", "languages/");
                return getLanguageFile(langTag);
            }
            return file.getConfig();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String get(String key) {
        String s = getLanguageFile(getLanguageKey()).getString(key, null);
        return s == null ? null : prepare(null, s);
    }

    public static String get(String key, Player p) {
        String s = getLanguageFile(getLanguageKey()).getString(key, null);
        return s == null ? null : prepare(p, s);
    }

    private static String prepare(Player player, String s) {
        s = s.replace("\\n", "\n");
        s = ChatColor.translateAlternateColorCodes('&', s);
        if(player != null) s = PAPI.convert(s, player);
        return s;
    }

    private static FileConfiguration getConfig() {
        return TradeSystem.getInstance().getFileManager().getFile("Config").getConfig();
    }

    private static void mkDir(File file) {
        if(!file.exists()) {
            try {
                file.mkdirs();
            } catch(SecurityException ex) {
                throw new IllegalArgumentException("Plugin is not permitted to create a folder!");
            }
        }
    }

    private static long copy(InputStream from, OutputStream to) throws IOException {
        if(from == null) return -1;
        if(to == null) throw new NullPointerException();

        byte[] buf = new byte[4096];
        long total = 0L;

        while(true) {
            int r = from.read(buf);
            if(r == -1) {
                return total;
            }

            to.write(buf, 0, r);
            total += r;
        }
    }
}
