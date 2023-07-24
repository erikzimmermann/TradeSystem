package de.codingair.tradesystem.spigot.trade.gui.layout.registration;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the data of a {@link de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition.Consumer transition consumer} that will be used to run the layout editor.
 */
public class TransitionTargetEditorInfo extends EditorInfo {
    private final Class<? extends TradeIcon> origin;

    /**
     * @param name   The name of the icon which will be displayed in the layout editor.
     * @param origin The origin of the icon. This will be used to link the transition target to its dependant icon.
     */
    public TransitionTargetEditorInfo(@NotNull String name, @NotNull Class<? extends TradeIcon> origin) {
        super(name);
        this.origin = origin;
    }

    public Class<? extends TradeIcon> getOrigin() {
        return origin;
    }

    @Override
    public @NotNull Type getType() {
        return IconHandler.getInfo(origin).getType();
    }
}
