package de.codingair.tradesystem.spigot.trade.gui.layout.registration;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.gui.editor.Editor;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Information class for {@link MultiTradeIcon MultiTradeIcons}.
 */
public class MultiEditorInfo extends EditorInfo {
    private final String[] iconName;

    /**
     * @param name               The name of the icon which will be displayed in the layout editor.
     * @param type               The type of the icon. This will be used to categorize the icons in the layout editor.
     * @param editorIconSupplier A supplier for the item that will be displayed in the layout editor.
     * @param necessary          Means that a layout depends on this icon. If true, a layout cannot be created without this icon.
     * @param iconName           The names of the nested icons which will be displayed in the layout editor.
     */
    public MultiEditorInfo(@NotNull String name, @NotNull Type type, @NotNull Function<Editor, ItemBuilder> editorIconSupplier, boolean necessary, @NotNull String @NotNull ... iconName) {
        super(name, type, editorIconSupplier, necessary);
        this.iconName = iconName;
    }

    /**
     * @param name     The main name of the icon which will be displayed in the layout editor.
     * @param iconName The names of the nested icons which will be displayed in the layout editor.
     */
    public MultiEditorInfo(@NotNull String name, @NotNull String @NotNull ... iconName) {
        super(name);
        this.iconName = iconName;
    }

    /**
     * @return The names of the nested icons which will be displayed in the layout editor.
     */
    public @NotNull String @NotNull [] getIconName() {
        return iconName;
    }
}
