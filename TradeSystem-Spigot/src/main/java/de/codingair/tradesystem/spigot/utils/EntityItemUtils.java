package de.codingair.tradesystem.spigot.utils;

import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.PacketUtils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityItemUtils {
    private static final Class<?> ENTITY_ITEM_CLASS = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE("net.minecraft.world.entity.item"), "EntityItem");
    private static final IReflection.ConstructorAccessor ENTITY_ITEM = IReflection.getConstructor(ENTITY_ITEM_CLASS, PacketUtils.WorldClass, double.class, double.class, double.class, PacketUtils.ItemStackClass);

    @NotNull
    public static Item create(@NotNull Location location, @NotNull ItemStack item) {
        assert ENTITY_ITEM != null;
        Object entityItem = ENTITY_ITEM.newInstance(PacketUtils.getWorldServer(location.getWorld()), location.getX(), location.getY(), location.getZ(), PacketUtils.getItemStack(item));
        return (Item) PacketUtils.getBukkitEntity(entityItem);
    }

}
