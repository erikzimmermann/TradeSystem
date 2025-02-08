package de.codingair.tradesystem.spigot.utils;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompatibilityUtilEvent {

    /**
     * In API versions 1.20.6 and earlier, InventoryView is a class.
     * In versions 1.21 and later, it is an interface.
     * This method uses reflection to get the top Inventory object from the
     * InventoryView associated with an InventoryEvent, to avoid runtime errors.
     * @param event The generic InventoryEvent with an InventoryView to inspect.
     * @return The top Inventory object from the event's InventoryView.
     */
    public static Inventory getTopInventory(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Inventory getBottomInventory(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getBottomInventory = view.getClass().getMethod("getBottomInventory");
            getBottomInventory.setAccessible(true);
            return (Inventory) getBottomInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCursor(InventoryClickEvent event, ItemStack currentItem) {
        try {
            Object view = event.getView();
            Method setCursor = view.getClass().getMethod("setCursor", ItemStack.class);
            setCursor.setAccessible(true);
            setCursor.invoke(view, currentItem);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set cursor in InventoryView", e);
        }
    }

    public static ItemStack getCursor(InventoryClickEvent event) {
        try {
            Object view = event.getView();
            Method getCursor = view.getClass().getMethod("getCursor");
            getCursor.setAccessible(true);
            return (ItemStack) getCursor.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get cursor from InventoryView", e);
        }
    }

}
