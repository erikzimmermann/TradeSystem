package de.codingair.tradesystem.trade.layout;

import de.codingair.codingapi.tools.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class Item {
    private int slot;
    private ItemStack item;
    private Function function;

    public Item(int slot, ItemStack item, Function function) {
        this.slot = slot;
        this.item = item;
        this.function = function;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public ItemStack getItem() {
        ItemBuilder builder = new ItemBuilder(this.item).removeEnchantments().setHideStandardLore(true).removeLore();

        builder.setName(ChatColor.translateAlternateColorCodes('&', builder.getName()));

        return builder.getItem();
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }
}
