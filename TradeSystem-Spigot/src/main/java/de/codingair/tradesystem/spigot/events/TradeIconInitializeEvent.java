package de.codingair.tradesystem.spigot.events;

import de.codingair.tradesystem.spigot.trade.gui.layout.registration.EditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.TransitionTargetEditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions.TradeIconException;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * Called during onEnable before loading trade layouts. This allows you to register your own TradeIcons.
 */
public class TradeIconInitializeEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final BiConsumer<Class<? extends TradeIcon>, EditorInfo> registry;

    public TradeIconInitializeEvent(@NotNull BiConsumer<Class<? extends TradeIcon>, EditorInfo> registry) {
        this.registry = registry;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Used for basic trade icons.
     *
     * @param icon The trade icon class that should be registered.
     * @param info The editor info for the layout editor.
     * @throws TradeIconException When an error occurred while registering this icon in the IconHandler.
     */
    public void registerIcon(@NotNull JavaPlugin plugin, @NotNull Class<? extends TradeIcon> icon, @NotNull EditorInfo info) throws TradeIconException {
        try {
            this.registry.accept(icon, info);
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof TradeIconException)
                throw new TradeIconException(getErrorMessage(plugin), ex.getCause());
            else throw new RuntimeException(getErrorMessage(plugin), ex.getCause());
        }
    }

    /**
     * Used for icons that only work with another icon like economy icons that must be present for the trade partner.
     *
     * @param icon The trade icon class that should be registered.
     * @param info The transition target editor info for the layout editor.
     * @throws TradeIconException When an error occurred while registering this icon in the IconHandler.
     */
    public void registerIcon(@NotNull JavaPlugin plugin, @NotNull Class<? extends TradeIcon> icon, @NotNull TransitionTargetEditorInfo info) throws TradeIconException {
        try {
            this.registry.accept(icon, info);
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof TradeIconException)
                throw new TradeIconException(getErrorMessage(plugin), ex.getCause());
            else throw new RuntimeException(getErrorMessage(plugin), ex.getCause());
        }
    }

    @NotNull
    private String getErrorMessage(@NotNull JavaPlugin plugin) {
        return "The plugin '" + plugin.getName() + "' by " +
                Arrays.toString(plugin.getDescription().getAuthors().toArray(new String[0])) +
                " issued an exception while registering TradeIcons for TradeSystem:";
    }
}
