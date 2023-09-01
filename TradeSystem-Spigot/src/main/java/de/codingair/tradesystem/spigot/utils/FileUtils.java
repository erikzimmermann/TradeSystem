package de.codingair.tradesystem.spigot.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class FileUtils {

    public static void createFile(@NotNull JavaPlugin plugin, @NotNull String internalPath, @NotNull String externalPath, String fileName, boolean overwrite) throws IOException {
        InputStream is = plugin.getResource(internalPath + fileName);
        if (is == null) return;

        File folder = new File(plugin.getDataFolder(), externalPath);
        if (!folder.exists()) {
            if (!folder.mkdirs()) plugin.getLogger().warning("Could not create folder: " + externalPath);
        }

        File file = new File(folder, fileName);
        boolean exists = file.exists();
        if (!exists || overwrite) {
            if (!exists && !file.createNewFile()) plugin.getLogger().warning("Could not create file: " + externalPath + fileName);
            FileUtils.copy(is, Files.newOutputStream(file.toPath()));
        }
    }

    public static void copy(@Nullable InputStream from, @NotNull OutputStream to) throws IOException {
        if (from == null) return;

        byte[] buf = new byte[4096];
        while (true) {
            int r = from.read(buf);
            if (r == -1) return;
            to.write(buf, 0, r);
        }
    }

}
