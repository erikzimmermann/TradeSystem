package de.codingair.tradesystem.spigot.trade.layout.registration;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.editor.Editor;
import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class EditorInfo {
    private final String name;
    private final Type type;
    private final Function<Editor, ItemBuilder> editorIcon;

    private Class<? extends TradeIcon> tradeIcon;
    private Class<? extends TradeIcon> transitionTarget;

    /**
     * Means that a layout depends on this icon. If true, a layout cannot be created without this icon.
     */
    private final boolean necessary;

    public EditorInfo(@NotNull String name, @NotNull Type type, @NotNull Function<Editor, ItemBuilder> editorIconSupplier, boolean necessary) {
        this.name = name;
        this.type = type;
        this.editorIcon = editorIconSupplier;
        this.necessary = necessary;
    }

    EditorInfo(@NotNull String name) {
        this.name = name;
        this.type = null;
        this.editorIcon = null;
        this.necessary = false;
    }

    public String getName() {
        return name;
    }

    public boolean nameOnly() {
        return type == null && editorIcon == null && !necessary;
    }

    public Type getType() {
        if (type == null) throw new NullPointerException();
        return type;
    }

    public ItemBuilder getEditorIcon(Editor editor) {
        if (editorIcon == null) throw new NullPointerException();
        return editorIcon.apply(editor);
    }

    public Class<? extends TradeIcon> getTradeIcon() {
        return tradeIcon;
    }

    void setTradeIcon(Class<? extends TradeIcon> tradeIcon) {
        this.tradeIcon = tradeIcon;
    }

    public boolean isNecessary() {
        return necessary;
    }

    @Nullable
    public Class<? extends TradeIcon> getTransitionTarget() {
        return transitionTarget;
    }

    void setTransitionTarget(Class<? extends TradeIcon> transitionTarget) {
        this.transitionTarget = transitionTarget;
    }
}
