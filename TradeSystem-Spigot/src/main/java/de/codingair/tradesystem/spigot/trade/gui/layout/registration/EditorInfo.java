package de.codingair.tradesystem.spigot.trade.gui.layout.registration;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.gui.editor.Editor;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents the data of a {@link TradeIcon} that will be used to run the layout editor.
 */
public class EditorInfo {
    private final String name;
    private final Type type;
    private final Function<Editor, ItemBuilder> editorIcon;
    private final String[] requiredPlugins;
    /**
     * Means that a layout depends on this icon. If true, a layout cannot be created without this icon.
     */
    private final boolean necessary;
    private Class<? extends TradeIcon> tradeIcon;
    private Class<? extends TradeIcon> transitionTarget;

    /**
     * @param name               The name of the icon which will be displayed in the layout editor.
     * @param type               The type of the icon. This will be used to categorize the icons in the layout editor.
     * @param editorIconSupplier A supplier for the item that will be displayed in the layout editor.
     * @param necessary          Means that a layout depends on this icon. If true, a layout cannot be created without this icon.
     * @param requiredPlugins    The plugins that are required to use this icon.
     */
    public EditorInfo(@NotNull String name, @NotNull Type type, @NotNull Function<Editor, ItemBuilder> editorIconSupplier, boolean necessary, String... requiredPlugins) {
        this.name = name;
        this.type = type;
        this.editorIcon = editorIconSupplier;
        this.necessary = necessary;
        this.requiredPlugins = requiredPlugins;
    }

    /**
     * Only used for icons that depend on another main icon (see {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition Transition} or {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon MultiTradeIcon}).
     *
     * @param name The name of the icon which will be displayed in the layout editor.
     */
    EditorInfo(@NotNull String name) {
        this.name = name;
        this.type = null;
        this.editorIcon = null;
        this.necessary = false;
        this.requiredPlugins = new String[0];
    }

    /**
     * @return The name of the icon which will be displayed in the layout editor.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * @return Whether this information instance only carries the name of the icon. Used for {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition transitions} or {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon MultiTradeIcons}.
     */
    public boolean nameOnly() {
        return type == null && editorIcon == null && !necessary;
    }

    /**
     * @return The type of the icon. This will be used to categorize the icons in the layout editor.
     */
    @NotNull
    public Type getType() {
        if (type == null) throw new NullPointerException(name + " (" + tradeIcon + ") has no type.");
        return type;
    }

    /**
     * @param editor The editor that is used to edit the layout.
     * @return The item that will be displayed in the layout editor.
     */
    @NotNull
    public ItemBuilder getEditorIcon(@NotNull Editor editor) {
        if (editorIcon == null) throw new NullPointerException();
        return editorIcon.apply(editor);
    }

    /**
     * @return The class of the icon that will be used to create the icon instance.
     */
    @NotNull
    public Class<? extends TradeIcon> getTradeIcon() {
        return tradeIcon;
    }

    /**
     * Only used when this icon was approved by the {@link IconHandler}.
     *
     * @param tradeIcon The class of the icon that will be used to create the icon instance.
     */
    void setTradeIcon(@NotNull Class<? extends TradeIcon> tradeIcon) {
        this.tradeIcon = tradeIcon;
    }

    /**
     * @return Whether this icon is necessary for a layout.
     */
    public boolean isNecessary() {
        return necessary;
    }

    /**
     * @return The transition target of this icon. Only used for {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition transitions}.
     */
    @Nullable
    public Class<? extends TradeIcon> getTransitionTarget() {
        return transitionTarget;
    }

    /**
     * @param transitionTarget The transition target of this icon. Only used for {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition transitions}.
     */
    void setTransitionTarget(@NotNull Class<? extends TradeIcon> transitionTarget) {
        this.transitionTarget = transitionTarget;
    }

    /**
     * @return The plugins that are required to use this icon.
     */
    public @NotNull String @NotNull [] getRequiredPlugins() {
        return requiredPlugins;
    }

    /**
     * @return Whether all required plugins are enabled.
     */
    public boolean matchRequirements() {
        for (String plugin : requiredPlugins) {
            if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) return false;
        }

        return true;
    }
}
