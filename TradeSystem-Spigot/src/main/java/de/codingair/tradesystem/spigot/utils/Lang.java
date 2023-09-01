package de.codingair.tradesystem.spigot.utils;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.files.FileManager;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.external.placeholderapi.PlaceholderDependency;
import org.bukkit.command.CommandSender;
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
import java.util.function.BiConsumer;

public class Lang {
    private static final String[] LANGUAGES = {
            "BR", "CHI", "CS", "ENG", "ES", "FR", "GER", "IT", "POL", "RUS", "TR", "UA", "VI"
    };
    private static final String DEFAULT_LANG = "ENG";
    private static final Map<String, JavaPlugin> PLUGINS = new HashMap<>();
    private static final Map<String, FileManager> MANAGERS = new HashMap<>();
    private static final Map<String, String> CACHED_PATHS = new HashMap<>();

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
        PLUGINS.put(path, plugin);
        MANAGERS.put(path, fileManager);

        initPreDefinedLanguages(plugin);
        checkLanguageKeys(plugin);
    }

    @NotNull
    private static String getAccessingPath() {
        StackTraceElement[] elements = new Throwable().getStackTrace();

        // skip first 2 as they are always within this class
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
    private static String getIdentityPath() {
        String path = getAccessingPath();

        String identity = CACHED_PATHS.get(path);
        if (identity != null) return identity;

        for (String key : PLUGINS.keySet()) {
            if (path.startsWith(key)) {
                CACHED_PATHS.put(path, key);
                return key;
            }
        }

        throw new IllegalStateException("Could not find identity path!");
    }

    @NotNull
    private static FileManager getManager() {
        String path = getIdentityPath();
        return MANAGERS.get(path);
    }

    @NotNull
    private static JavaPlugin getPlugin() {
        String path = getIdentityPath();
        return PLUGINS.get(path);
    }

    private static void initPreDefinedLanguages(@NotNull JavaPlugin plugin) {
        deleteEmptyFiles(plugin);

        File folder = new File(plugin.getDataFolder(), "/Languages/");
        if (!folder.exists()) mkDir(folder);

        for (String language : LANGUAGES) {
            try {
                FileUtils.createFile(plugin, "languages/", "/Languages/", language + ".yml", false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkLanguageKeys(@NotNull JavaPlugin plugin) {
        FileManager manager = getManager();
        FileConfiguration def = getLanguageFile(plugin, manager, DEFAULT_LANG);

        for (String language : LANGUAGES) {
            File languageFile = new File(plugin.getDataFolder(), "/Languages/" + language + ".yml");
            if (!languageFile.exists()) continue;

            FileConfiguration file = getLanguageFile(plugin, manager, language);
            for (String key : def.getKeys(true)) {
                if (!file.contains(key)) {
                    plugin.getLogger().warning("Missing language key \"" + key + "\" in " + language + ".yml. Using default value.");
                    file.set(key, def.get(key));
                }
            }
        }
    }

    public static void send(@NotNull CommandSender sender, @NotNull String key, @NotNull P... placeholders) {
        send(sender, "", key, placeholders);
    }

    public static void send(@NotNull CommandSender sender, @NotNull String prefix, @NotNull String key, @NotNull P... placeholders) {
        send(sender, prefix, key, CommandSender::sendMessage, placeholders);
    }

    public static void send(@Nullable CommandSender sender, @NotNull String prefix, @NotNull String key, @NotNull BiConsumer<CommandSender, String> send, @NotNull P... placeholders) {
        String message;
        if (sender instanceof Player) message = get(key, (Player) sender, placeholders);
        else message = get(key, placeholders);

        if (message.isEmpty()) return;

        send.accept(sender, Lang.getPrefix() + prefix + message);
    }

    public static @NotNull String getPrefix() {
        String prefix = getConfig().getString("TradeSystem.Prefix", "&8» &r");
        return prepare(null, prefix);
    }

    public static @NotNull String getLanguageKey() {
        String key = getConfig().getString("TradeSystem.Language");
        if (key == null) return "ENG";
        return key;
    }

    private static @NotNull FileConfiguration getLanguageFile(@Nullable JavaPlugin plugin, @Nullable FileManager fileManager, @NotNull String langTag) {
        if (plugin == null) plugin = TradeSystem.getInstance();
        if (fileManager == null) fileManager = TradeSystem.getInstance().getFileManager();

        ConfigFile configFile = fileManager.getFile(langTag);
        if (configFile == null) {
            // The file usually is created before loading it, so we can simply switch to the fallback language if the file cannot be found.
            File file = new File(plugin.getDataFolder() + "/Languages/", langTag + ".yml");
            if (!file.exists()) langTag = DEFAULT_LANG;

            fileManager.loadFile(langTag, "/Languages/", "languages/");
            configFile = fileManager.getFile(langTag);
        }

        return configFile.getConfig();
    }

    public static @NotNull String get(@NotNull String key, @NotNull P... placeholders) {
        return get(key, null, placeholders);
    }

    public static @NotNull String get(@NotNull String key, @Nullable Player p, @NotNull P... placeholders) {
        try {
            // prioritize external manager
            return get(getPlugin(), getManager(), key, p, placeholders);
        } catch (NullPointerException e) {
            // use internal manager as fallback
            return get(null, null, key, p, placeholders);
        }
    }

    private static @NotNull String get(@Nullable JavaPlugin plugin, @Nullable FileManager fileManager, @NotNull String key, @Nullable Player p, @NotNull P... placeholders) {
        String s = getLanguageFile(plugin, fileManager, getLanguageKey()).getString(key, null);
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
