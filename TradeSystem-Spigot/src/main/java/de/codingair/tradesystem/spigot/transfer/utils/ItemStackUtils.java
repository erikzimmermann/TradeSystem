package de.codingair.tradesystem.spigot.transfer.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemStackUtils {
    /**
     * Allowed number of bytes being able to transfer at once. 32766 bytes is the maximum of Minecraft's limitations.
     */
    private static final int MAX_TRANSFER_LIMIT = 30000;

    /**
     * Checks an item for serialisation and transfer compatibility.
     *
     * @param item The item that should be checked.
     * @return True if the given item is compatible with TradeProxy. False, otherwise.
     */
    public static boolean isCompatible(@NotNull ItemStack item) {
        try {
            byte[] data = serialize(item);
            if (data.length >= MAX_TRANSFER_LIMIT) return false;
            ItemStack itemCopy = deserialize(data);

            return item.equals(itemCopy);
        } catch (Throwable t) {
            return false;
        }
    }

    @Contract("null -> null")
    public static byte[] serialize(@Nullable ItemStack item) {
        if (item == null) return null;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos);

            boos.writeObject(item);

            byte[] data = bos.toByteArray();
            bos.close();
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract("null -> null")
    public static ItemStack deserialize(byte @Nullable [] data) {
        if (data == null) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);

            return (ItemStack) bois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
