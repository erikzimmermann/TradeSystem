package de.codingair.tradesystem.spigot.transfer.utils;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemStackUtils {

    @Nullable
    public static Map<String, Object> serializeItemStack(@Nullable ItemStack item) {
        if (item == null) return null;

        Map<String, Object> data = item.serialize();
        ItemStackUtils.convertMeta(data);

        return data;
    }

    @Nullable
    public static ItemStack deserializeItemStack(@Nullable Map<String, Object> data) {
        if (data == null) return null;
        data.computeIfPresent("meta", ($, serialised) -> ConfigurationSerialization.deserializeObject((Map<String, Object>) serialised));
        return ItemStack.deserialize(data);
    }

    private static void convertMeta(@NotNull Map<String, Object> data) {
        data.computeIfPresent("meta", (s, o) -> {
            if (o instanceof ItemMeta) {
                return serializeItemMeta((ItemMeta) o);
            }

            return o;
        });
    }

    @NotNull
    private static Map<String, Object> serializeItemMeta(ItemMeta o) {
        //create new map from immutable map
        Map<String, Object> metaData = new HashMap<>(o.serialize());

        //add serialization alias (ConfigurationSerialization)
        metaData.put("==", "ItemMeta");
        return metaData;
    }

}
