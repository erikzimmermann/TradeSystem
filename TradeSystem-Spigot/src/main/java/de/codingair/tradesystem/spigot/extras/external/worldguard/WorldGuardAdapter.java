package de.codingair.tradesystem.spigot.extras.external.worldguard;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Stream;

public class WorldGuardAdapter {
    public static void test() throws ClassNotFoundException, NoClassDefFoundError {
        Class.forName("com.sk89q.worldguard.WorldGuard");
    }

    @NotNull
    public Stream<String> getRegion(@NotNull Location location) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        World w = location.getWorld();
        if (w == null) throw new IllegalArgumentException("The location '" + location + "' does not provide a world!");

        RegionManager man = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(w));
        return getRegions(location, man);
    }

    @NotNull
    protected Stream<String> getRegions(@NotNull Location location, RegionManager man) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (man == null) return Stream.empty();

        ApplicableRegionSet set = getProtectedRegions(location, man);
        Set<ProtectedRegion> regions = set.getRegions();

        if (regions.isEmpty()) return Stream.empty();
        return regions.stream().map(ProtectedRegion::getId);
    }

    @NotNull
    protected ApplicableRegionSet getProtectedRegions(@NotNull Location location, RegionManager man) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (ApplicableRegionSet) man.getClass().getMethod("getApplicableRegions", BlockVector3.class).invoke(man, BlockVector3.at(location.getX(), location.getY(), location.getZ()));
    }
}
