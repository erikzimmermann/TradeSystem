package de.codingair.tradesystem.spigot.utils;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.FileManager;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.external.placeholderapi.PlaceholderDependency;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Lang {
    private static final String[] LANGUAGES = {
            "BR", "CHI", "CS", "ENG", "ES", "FR", "GER", "IT", "POL", "RUS", "TR", "UA", "VI"
    };
    private static final String DEFAULT_LANG = "ENG";
    private static final Map<String, FileManager> MANAGERS = new HashMap<>();
    private static final Map<String, String> CACHED = new HashMap<>();

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

    public static void init(@NotNull JavaPlugin plugin, @NotNull FileManager fileManager) {
        String path = plugin.getClass().getPackage().getName().toLowerCase();
        MANAGERS.put(path, fileManager);

        initPreDefinedLanguages(plugin);
        checkLanguageKeys(plugin);
    }

    @NotNull
    private static String getAccessingPath() {
        StackTraceElement[] elements = new Throwable().getStackTrace();

        // skip first 2 as they always are within this class
        for (int i = 2; i < elements.length; i++) {
            StackTraceElement e = elements[i];

            String c = e.getClassName();
            if (Lang.class.getName().equals(c)) continue;

            int idx = c.lastIndexOf(".");
            if (idx > 0) return c.substring(0, idx);
            else return "";
        }

        throw new IllegalStateException("Could not find accessing path!");
    }

    @NotNull
    private static FileManager getManager() {
        String path = getAccessingPath();

        String manPath = CACHED.get(path);
        if (manPath != null) return MANAGERS.get(manPath);

        for (String key : MANAGERS.keySet()) {
            if (path.startsWith(key)) {
                CACHED.put(path, key);
                return MANAGERS.get(key);
            }
        }

        throw new NullPointerException("No file manager found for path '" + path + "'!");
    }

    private static void initPreDefinedLanguages(@NotNull JavaPlugin plugin) {
        deleteEmptyFiles(plugin);

        File folder = new File(plugin.getDataFolder(), "/Languages/");
        if (!folder.exists()) mkDir(folder);

        for (String language : LANGUAGES) {
            InputStream is = plugin.getResource("languages/" + language + ".yml");
            if (is == null) continue;

            File file = new File(plugin.getDataFolder() + "/Languages/", language + ".yml");
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) copy(is, Files.newOutputStream(file.toPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void checkLanguageKeys(@NotNull JavaPlugin plugin) {
        FileManager manager = getManager();
        FileConfiguration def = getLanguageFile(manager, DEFAULT_LANG);

        for (String language : LANGUAGES) {
            File languageFile = new File(plugin.getDataFolder(), "/Languages/" + language + ".yml");
            if (!languageFile.exists()) continue;

            FileConfiguration file = getLanguageFile(manager, language);
            for (String key : def.getKeys(true)) {
                if (!file.contains(key)) {
                    plugin.getLogger().warning("Missing language key \"" + key + "\" in " + language + ".yml. Using default value.");
                    file.set(key, def.get(key));
                }
            }
        }
    }

    public static String getPrefix() {
        String prefix = getConfig().getString("TradeSystem.Prefix", "&8» &r");
        return prepare(null, prefix);
    }

    public static @NotNull String getLanguageKey() {
        String key = getConfig().getString("TradeSystem.Language");
        if (key == null) return "ENG";
        return key;
    }

    private static @NotNull FileConfiguration getLanguageFile(@Nullable FileManager fileManager, @NotNull String langTag) {
        if (fileManager == null) fileManager = TradeSystem.getInstance().getFileManager();

        ConfigFile file = fileManager.getFile(langTag);
        if (file == null) {
            fileManager.loadFile(langTag, "/Languages/", "languages/");
            file = fileManager.getFile(langTag);
        }

        return file.getConfig();
    }

    public static @NotNull String get(@NotNull String key, @NotNull P... placeholders) {
        return get(key, null, placeholders);
    }

    public static @NotNull String get(@NotNull String key, @Nullable Player p, @NotNull P... placeholders) {
        try {
            // prioritize external manager
            return get(getManager(), key, p, placeholders);
        } catch (NullPointerException e) {
            // use internal manager as fallback
            return get(null, key, p, placeholders);
        }
    }

    private static @NotNull String get(@Nullable FileManager fileManager, @NotNull String key, @Nullable Player p, @NotNull P... placeholders) {
        String s = getLanguageFile(fileManager, getLanguageKey()).getString(key, null);
        if (s == null) throw new NullPointerException("Message \"" + key + "\" cannot be found in " + getLanguageKey());

        for (P placeholder : placeholders) {
            s = placeholder.apply(s);
        }

        return prepare(p, s);
    }

    private static String prepare(@Nullable Player player, @NotNull String s) {
        s = s.replace("\\n", "\n");
        s = ChatColor.translateAll('&', s);
        if (player != null) s = PlaceholderDependency.convert(s, player);
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
    }
}
