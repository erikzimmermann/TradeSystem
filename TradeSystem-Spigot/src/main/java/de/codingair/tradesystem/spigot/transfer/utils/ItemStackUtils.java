package de.codingair.tradesystem.spigot.transfer.utils;

import org.bukkit.Color;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

public class ItemStackUtils {

    @Nullable
    public static Map<String, Object> serializeItemStack(@Nullable ItemStack item) {
        if (item == null) return null;

        Map<String, Object> data = item.serialize();
        convertMeta(data);

        return data;
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

        convertCustomEffects(metaData);
        convertCustomColor(metaData);
        convertAttributeModifiers(metaData);

        //add serialization alias (ConfigurationSerialization)
        metaData.put("==", "ItemMeta");
        return metaData;
    }

    private static void convertCustomEffects(@NotNull Map<String, Object> data) {
        data.computeIfPresent("custom-effects", (s, o) -> {
            if (o instanceof List) {
                //noinspection unchecked
                return serializeCustomEffects((List<PotionEffect>) o);
            }

            return o;
        });
    }

    @NotNull
    private static List<Map<String, Object>> serializeCustomEffects(List<PotionEffect> o) {
        List<Map<String, Object>> converted = new ArrayList<>();
        o.forEach(p -> {
            Map<String, Object> serialized = new HashMap<>(p.serialize());
            serialized.put("==", "PotionEffect");
            converted.add(serialized);
        });
        return converted;
    }

    private static void convertCustomColor(@NotNull Map<String, Object> data) {
        BiFunction<String, Object, Object> f = (s, o) -> {
            if (o instanceof Color) {
                return serializeCustomColor((Color) o);
            }

            return o;
        };

        data.computeIfPresent("custom-color", f);
        data.computeIfPresent("color", f);
    }

    @NotNull
    private static Map<String, Object> serializeCustomColor(Color o) {
        Map<String, Object> serialized = new HashMap<>(o.serialize());
        serialized.put("==", "Color");
        return serialized;
    }

    private static void convertAttributeModifiers(@NotNull Map<String, Object> data) {
        data.computeIfPresent("attribute-modifiers", (s, o) -> {
            if (o instanceof Map) {
                @SuppressWarnings ("unchecked")
                Map<String, Object> internal = (Map<String, Object>) o;

                Set<String> keys = new HashSet<>(internal.keySet());
                for (String key : keys) {
                    internal.computeIfPresent(key, ($, array) -> {
                        if (array instanceof ArrayList) {
                            @SuppressWarnings ("unchecked")
                            List<AttributeModifier> raw = (List<AttributeModifier>) array;
                            List<Map<String, Object>> serialised = new ArrayList<>();

                            for (AttributeModifier attributeModifier : raw) {
                                serialised.add(serializeAttributeModifier(attributeModifier));
                            }

                            return serialised;
                        } else return array;
                    });
                }

                return internal;
            }

            return o;
        });
    }

    @NotNull
    private static Map<String, Object> serializeAttributeModifier(AttributeModifier am) {
        Map<String, Object> serialized = new HashMap<>(am.serialize());
        serialized.put("==", "org.bukkit.attribute.AttributeModifier");
        return serialized;
    }

    @Nullable
    public static ItemStack deserializeItemStack(@Nullable Map<String, Object> data) {
        if (data == null) return null;

        //prepare clipped serialized data
        deserializeItemMeta(data);

        return ItemStack.deserialize(data);
    }

    private static void deserializeItemMeta(@NotNull Map<String, Object> data) {
        data.computeIfPresent("meta", ($, serialised) -> {
            @SuppressWarnings ("unchecked")
            Map<String, Object> map = (Map<String, Object>) serialised;

            deserializeCustomEffects(map);
            deserializeCustomColors(map);
            deserializeAttributeModifiers(map);

            return ConfigurationSerialization.deserializeObject(map);
        });
    }

    private static void deserializeCustomEffects(@NotNull Map<String, Object> data) {
        data.computeIfPresent("custom-effects", ($, serialised) -> {
            @SuppressWarnings ("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) serialised;

            List<PotionEffect> deserialized = new ArrayList<>();
            list.forEach(pData -> deserialized.add((PotionEffect) ConfigurationSerialization.deserializeObject(pData)));

            return deserialized;
        });
    }

    private static void deserializeCustomColors(@NotNull Map<String, Object> data) {
        //noinspection unchecked
        BiFunction<String, Object, Object> f = ($, serialised) -> ConfigurationSerialization.deserializeObject((Map<String, Object>) serialised);
        data.computeIfPresent("custom-color", f);
        data.computeIfPresent("color", f);
    }

    private static void deserializeAttributeModifiers(@NotNull Map<String, Object> data) {
        data.computeIfPresent("attribute-modifiers", ($, serialised) -> {
            @SuppressWarnings ("unchecked")
            Map<String, Object> internal = (Map<String, Object>) serialised;

            Set<String> keys = new HashSet<>(internal.keySet());
            for (String key : keys) {
                internal.computeIfPresent(key, ($2, serialised2) -> {
                    @SuppressWarnings ("unchecked")
                    List<Map<String, Object>> list = (List<Map<String, Object>>) serialised2;
                    List<AttributeModifier> modifiers = new ArrayList<>();

                    for (Map<String, Object> raw : list) {
                        modifiers.add((AttributeModifier) ConfigurationSerialization.deserializeObject(raw));
                    }

                    return modifiers;
                });
            }

            return internal;
        });
    }
}
