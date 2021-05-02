package de.codingair.tradesystem.spigot.trade.layout;

import de.codingair.codingapi.tools.items.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Item {
    private static boolean failure = false;
    private int slot;
    private ItemStack item;
    private Function function;

    public Item(int slot, ItemStack item, Function function) {
        this.slot = slot;
        this.item = item;
        this.function = function;
    }

    public static Item fromJSONString(String s) {
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(s);

            int slot = Integer.parseInt("" + json.get("Slot"));
            ItemStack item = json.get("Item") == null ? null : ItemBuilder.getFromJSON((String) json.get("Item")).getItem();
            Function function = json.get("Function") == null ? null : Function.valueOf((String) json.get("Function"));

            return new Item(slot, item, function);
        } catch (ParseException e) {
            if (!failure) {
                failure = true;
                e.printStackTrace();
            }
            return null;
        }
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public ItemStack getItem() {
        if (this.item == null) return new ItemStack(Material.AIR);
        ItemBuilder builder = new ItemBuilder(this.item).removeEnchantments().setHideStandardLore(true).removeLore();

        if (builder.getName() != null) builder.setName(ChatColor.translateAlternateColorCodes('&', builder.getName()));
        else builder.setHideName(true);

        return builder.getItem();
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public Function getFunction() {
        return function;
    }

    public Item setFunction(Function function) {
        this.function = function;
        return this;
    }

    public Item clone() {
        return new Item(slot, item, function);
    }

    public String toJSONString() {
        JSONObject json = new JSONObject();

        json.put("Slot", this.slot);
        json.put("Item", this.item == null ? null : new ItemBuilder(this.item).toJSONString());
        json.put("Function", this.function == null ? null : this.function.name());

        return json.toJSONString();
    }
}
