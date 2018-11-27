package de.codingair.tradesystem.utils.blacklist;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BlockedItem {
    private Material material;
    private byte data;
    private String name;

    public BlockedItem(Material material, byte data, String name) {
        this.material = material;
        this.data = data;
        this.name = name;
    }

    public BlockedItem(Material material, byte data) {
        this(material, data, null);
    }

    public BlockedItem(String name) {
        this(null, (byte) 0, name);
    }

    public boolean matches(ItemStack item) {
        boolean matches = false;

        if(material != null) {
            if(item.getType() == this.material && data == item.getData().getData()) matches = true;
        }

        if(name != null && item.hasItemMeta() && item.getItemMeta().getDisplayName() != null) {
            if(item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', name))) matches = true;
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

        if(this.material != null) {
            json.put("Material", this.material.name());
            json.put("Data", this.data);
        }

        if(this.name != null) {
            json.put("Displayname", this.name);
        }

        return json.toJSONString();
    }

    public static BlockedItem fromString(String s) {
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(s);

            Material material = json.get("Material") == null ? null : Material.valueOf((String) json.get("Material"));
            byte data = material == null ? 0 : Byte.parseByte(json.get("Data") + "");
            String name = json.get("Displayname") == null ? null : (String) json.get("Displayname");

            return new BlockedItem(material, data, name);
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
