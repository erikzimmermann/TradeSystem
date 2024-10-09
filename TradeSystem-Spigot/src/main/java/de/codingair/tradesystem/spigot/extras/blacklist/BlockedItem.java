package de.codingair.tradesystem.spigot.extras.blacklist;

import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.tradesystem.spigot.utils.ShulkerBoxHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockedItem implements Serializable {

    public @Nullable Material material;
    public @Nullable Byte data;
    public @Nullable String originalDisplayName;
    public @Nullable String displayName;
    public @Nullable String originalLore;
    public @Nullable String lore;
    public @Nullable Integer customModelData;
    public @NotNull StringCompare compare = StringCompare.IGNORE_CASE;

    private BlockedItem() {
    }

    @NotNull
    public static BlockedItem create() {
        return new BlockedItem();
    }

    @NotNull
    public static BlockedItem create(@NotNull Map<?, ?> yml) {
        JSON data = new JSON(yml);
        BlockedItem item = create();
        item.read(data);
        return item;
    }

    @Deprecated
    public static BlockedItem fromString(String s) {
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(s);

            Material material = json.get("Material") == null ? null : Material.valueOf((String) json.get("Material"));
            Byte data = json.get("Data") == null ? null : Byte.parseByte(json.get("Data") + "");
            String name = json.get("Displayname") == null ? null : (String) json.get("Displayname");

            return create().material(material).data(data).displayName(name);
        } catch (NoSuchFieldError | IllegalArgumentException ex) {
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean read(DataMask mask) {
        this.material = mask.get("Material", Material.class);

        this.data = mask.getByte("Data");
        if (this.data == 0) this.data = null;

        this.displayName = mask.getString("DisplayName");
        if (this.displayName != null) {
            this.originalDisplayName = displayName;
            this.displayName = ChatColor.translateAll('&', this.displayName);
        }

        this.lore = mask.getString("Lore");
        if (this.lore != null) {
            this.originalLore = lore;
            this.lore = ChatColor.translateAll('&', this.lore);
        }

        StringCompare regex = mask.get("Compare", StringCompare.class);
        this.compare = regex == null ? StringCompare.IGNORE_CASE : regex;

        this.customModelData = mask.getInteger("CustomModelData", null);

        return true;
    }

    @Override
    public void write(DataMask mask) {
        mask.put("Material", material);
        mask.put("Data", data);
        mask.put("DisplayName", originalDisplayName);
        mask.put("Lore", originalLore);
        mask.put("Compare", compare);
        mask.put("CustomModelData", customModelData);
    }

    public boolean matches(@NotNull ItemStack item) {
        if (notValid()) return false;

        if (matchShulkerBoxContent(item)) return true;

        if (missMaterial(item)) return false;
        if (missDisplayName(item)) return false;
        if (missLore(item)) return false;
        return !missCustomModelData(item);
    }

    private boolean notValid() {
        return material == null && displayName == null && lore == null && customModelData == null;
    }

    private boolean matchShulkerBoxContent(@NotNull ItemStack item) {
        if (Version.atLeast(11)) {
            for (ItemStack itemStack : ShulkerBoxHelper.getItems(item)) {
                if (itemStack == null) continue;
                if (matches(itemStack)) return true;
            }
        }

        return false;
    }

    private boolean missMaterial(@NotNull ItemStack item) {
        if (this.material == null) return false;

        if (Version.after(12) || data == null) {
            return item.getType() != this.material;
        } else {
            //noinspection deprecation
            return item.getType() != this.material || item.getData() == null || data != item.getData().getData();
        }
    }

    private boolean missDisplayName(@NotNull ItemStack item) {
        if (this.displayName == null) return false;

        if (item.hasItemMeta() && item.getItemMeta() != null) {
            String displayName = item.getItemMeta().getDisplayName();

            return !this.compare.check(displayName, this.displayName);
        } else return true;
    }

    private boolean missLore(@NotNull ItemStack item) {
        if (lore == null) return false;

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.getLore() != null) {
                return meta.getLore().stream().noneMatch(s -> compare.check(s, lore));
            }
        }

        return true;
    }

    private boolean missCustomModelData(@NotNull ItemStack item) {
        if (customModelData == null || Version.before(14)) return false;

        if (item.hasItemMeta() && item.getItemMeta() != null) {
            return customModelData != item.getItemMeta().getCustomModelData();
        } else return true;
    }

    @Nullable
    public Material getMaterial() {
        return material;
    }

    @NotNull
    public BlockedItem material(@Nullable Material material) {
        this.material = material;
        return this;
    }

    @Nullable
    public Byte getData() {
        return data;
    }

    @NotNull
    public BlockedItem data(@Nullable Byte data) {
        this.data = data;
        return this;
    }

    @Nullable
    public String getDisplayName() {
        return originalDisplayName;
    }

    @NotNull
    public BlockedItem displayName(@Nullable String displayName) {
        this.originalDisplayName = displayName;

        if (displayName != null) this.displayName = ChatColor.translateAll('&', displayName);
        else this.displayName = null;
        return this;
    }

    @Nullable
    public String getLore() {
        return originalLore;
    }

    @NotNull
    public BlockedItem lore(@Nullable String lore) {
        this.originalLore = lore;

        if (lore != null) this.lore = ChatColor.translateAll('&', lore);
        else this.lore = null;
        return this;
    }

    @NotNull
    public BlockedItem strict() {
        this.compare = StringCompare.STRICT;
        return this;
    }

    @NotNull
    public BlockedItem ignoreCase() {
        this.compare = StringCompare.IGNORE_CASE;
        return this;
    }

    @NotNull
    public BlockedItem contains() {
        this.compare = StringCompare.CONTAINS;
        return this;
    }

    @NotNull
    public BlockedItem containsIgnoreCase() {
        this.compare = StringCompare.CONTAINS_IGNORE_CASE;
        return this;
    }

    @NotNull
    public BlockedItem regexAny() {
        this.compare = StringCompare.REGEX_ANY;
        return this;
    }

    @NotNull
    public BlockedItem regexAll() {
        this.compare = StringCompare.REGEX_ALL;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockedItem)) return false;
        BlockedItem that = (BlockedItem) o;
        return getMaterial() == that.getMaterial() && Objects.equals(getData(), that.getData()) && Objects.equals(originalDisplayName, that.originalDisplayName) && Objects.equals(originalLore, that.originalLore) && compare == that.compare && Objects.equals(customModelData, that.customModelData);
    }

    @Override
    public int hashCode() {
        //The hashCode is different in every server instance for enums. Use specific values instead!
        String material = getMaterial() == null ? null : getMaterial().name();
        return Objects.hash(material, getData(), originalDisplayName, originalLore, compare.ordinal(), customModelData);
    }

    @Override
    public String toString() {
        return "BlockedItem{" +
                "material=" + material +
                ", data=" + data +
                ", displayName='" + originalDisplayName + '\'' +
                ", lore='" + originalLore + '\'' +
                ", regex=" + compare +
                ", customModelData=" + customModelData +
                '}';
    }

    /**
     * This order is important for proxy trading. Changing the ordinal codes would break trading.
     */
    public enum StringCompare {
        STRICT(String::equals),
        CONTAINS(String::contains),
        CONTAINS_IGNORE_CASE((s0, s1) -> s0.toLowerCase().contains(s1.toLowerCase())),
        IGNORE_CASE(String::equalsIgnoreCase),
        REGEX_ANY((s, regex) -> {
            Matcher m = Pattern.compile(regex).matcher(s);
            return m.find();
        }),
        REGEX_ALL((s, regex) -> {
            Matcher m = Pattern.compile(regex).matcher(s);
            return m.matches();
        });

        private final BiFunction<String, String, Boolean> compare;

        StringCompare(@NotNull BiFunction<String, String, Boolean> compare) {
            this.compare = compare;
        }

        public boolean check(@Nullable String s, @NotNull String with) {
            if (s == null) return false;
            return compare.apply(s, with);
        }
    }
}
