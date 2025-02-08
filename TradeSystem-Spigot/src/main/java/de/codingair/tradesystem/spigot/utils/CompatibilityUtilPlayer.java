package de.codingair.tradesystem.spigot.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompatibilityUtilPlayer {

    /**
     * In API versions 1.20.6 and earlier, InventoryView is a class.
     * In versions 1.21 and later, it is an interface.
     */

    public static ItemStack getCursor(Player player) {
        try {
            Object openInventory = player.getOpenInventory();
            Method getCursor = openInventory.getClass().getMethod("getCursor");
            getCursor.setAccessible(true);
            return (ItemStack) getCursor.invoke(openInventory);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get cursor from Player's open inventory", e);
        }
    }

    public static void setCursor(Player player, ItemStack itemStack) {
        try {
            Object openInventory = player.getOpenInventory();
            Method setCursor = openInventory.getClass().getMethod("setCursor", ItemStack.class);
            setCursor.setAccessible(true);
            setCursor.invoke(openInventory, itemStack);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set cursor in Player's open inventory", e);
        }
    }

    public static Inventory getTopInventory(Player player) {
        try {
            Object openInventory = player.getOpenInventory();
            Method getTopInventory = openInventory.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(openInventory);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get top inventory from Player's open inventory", e);
        }
    }


}
