package de.codingair.tradesystem.spigot.utils;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.placeholderapi.PAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Lang {
    private static void deleteEmptyFiles(JavaPlugin plugin) {
        File folder = new File(plugin.getDataFolder(), "/Languages/");

        if (folder.exists()) {
            File[] children = folder.listFiles();
            if (children == null) throw new NullPointerException("Could not create language files!");

            for (File file : children) {
                if (file.length() == 0) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
    }

    public static void initPreDefinedLanguages(JavaPlugin plugin) {
        deleteEmptyFiles(plugin);

        List<String> languages = new ArrayList<>();
        languages.add("BR.yml");
        languages.add("CHI.yml");
        languages.add("CS.yml");
        languages.add("ENG.yml");
        languages.add("ES.yml");
        languages.add("FR.yml");
        languages.add("GER.yml");
        languages.add("POL.yml");
        languages.add("RUS.yml");
        languages.add("TR.yml");
        languages.add("VI.yml");

        File folder = new File(plugin.getDataFolder(), "/Languages/");
        if (!folder.exists()) mkDir(folder);

        for (String language : languages) {
            InputStream is = plugin.getResource("languages/" + language);

            File file = new File(plugin.getDataFolder() + "/Languages/", language);
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) copy(is, new FileOutputStream(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getPrefix() {
        String prefix = getConfig().getString("TradeSystem.Prefix", "&8» &r");
        prefix = ChatColor.translateAll('&', prefix);
        return prefix;
    }

    public static @NotNull String getLanguageKey() {
        String key = getConfig().getString("TradeSystem.Language");
        if (key == null) return "ENG";
        return key;
    }

    public static void initializeFile() {
        getLanguageFile(getLanguageKey());
    }

    private static @NotNull FileConfiguration getLanguageFile(String langTag) {
        ConfigFile file = TradeSystem.getInstance().getFileManager().getFile(langTag);
        if (file == null) {
            TradeSystem.getInstance().getFileManager().loadFile(langTag, "/Languages/", "languages/");
            return getLanguageFile(langTag);
        }
        return file.getConfig();
    }

    public static @NotNull String get(@NotNull String key, @NotNull P... placeholders) {
        return get(key, null, placeholders);
    }

    public static @NotNull String get(@NotNull String key, @Nullable Player p, @NotNull P... placeholders) {
        String s = getLanguageFile(getLanguageKey()).getString(key, null);
        if (s == null) throw new NullPointerException("Message \"" + key + "\" cannot be found in " + getLanguageKey());

        for (P placeholder : placeholders) {
            s = placeholder.apply(s);
        }

        return prepare(p, s);
    }

    private static String prepare(Player player, String s) {
        s = s.replace("\\n", "\n");
        s = ChatColor.translateAll('&', s);
        if (player != null) s = PAPI.convert(s, player);
        return s;
    }

    private static FileConfiguration getConfig() {
        return TradeSystem.getInstance().getFileManager().getFile("Config").getConfig();
    }

    private static void mkDir(File file) {
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.mkdirs();
            } catch (SecurityException ex) {
                throw new IllegalArgumentException("Plugin is not permitted to create a folder!");
            }
        }
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        if (from == null) return;
        if (to == null) throw new NullPointerException();

        byte[] buf = new byte[4096];

        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                return;
            }

            to.write(buf, 0, r);
        }
    }

    /**
     * Short for placeholder.
     */
    public static class P {
        private final String placeholder;
        private final String replacement;

        public P(@NotNull String placeholder, @NotNull String replacement) {
            this.placeholder = placeholder;
            this.replacement = replacement;
        }

        @NotNull
        public String apply(@NotNull String s) {
            return s.replace("%" + placeholder + "%", replacement);
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public String getReplacement() {
            return replacement;
        }
    }
}
