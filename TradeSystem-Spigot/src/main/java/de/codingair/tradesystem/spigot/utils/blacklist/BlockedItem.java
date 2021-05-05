package de.codingair.tradesystem.spigot.utils.blacklist;

import de.codingair.codingapi.server.specification.Version;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Objects;

public class BlockedItem {
    private final @Nullable Material material;
    private final byte data;
    private final @Nullable String name;

    public BlockedItem(@Nullable Material material, byte data, @Nullable String name) {
        this.material = material;
        this.data = data;
        this.name = name;
    }

    public BlockedItem(@Nullable Material material, byte data) {
        this(material, data, null);
    }

    public BlockedItem(@Nullable String name) {
        this(null, (byte) 0, name);
    }

    public static BlockedItem fromString(String s) {
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(s);

            Material material = json.get("Material") == null ? null : Material.valueOf((String) json.get("Material"));
            byte data = material == null ? 0 : Byte.parseByte(json.get("Data") + "");
            String name = json.get("Displayname") == null ? null : (String) json.get("Displayname");

            return new BlockedItem(material, data, name);
        } catch (NoSuchFieldError | IllegalArgumentException ex) {
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean matches(ItemStack item) {
        boolean matches = false;

        if (material != null) {
            if (Version.get().isBiggerThan(Version.v1_12)) {
                if (item.getType() == this.material) matches = true;
            } else {
                if (item.getType() == this.material && data == item.getData().getData()) matches = true;
            }
        }

        if (name != null && item.hasItemMeta() && item.getItemMeta().getDisplayName() != null) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', name))) matches = true;
        }

        return matches;
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();

        if (this.material != null) {
            json.put("Material", this.material.name());
            json.put("Data", this.data);
        }

        if (this.name != null) {
            json.put("Displayname", this.name);
        }

        return json.toJSONString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockedItem that = (BlockedItem) o;
        return data == that.data &&
                material == that.material &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int material = this.material == null ? 0 : this.material.ordinal();
        return Objects.hash(material, data, name + "");
    }
}
