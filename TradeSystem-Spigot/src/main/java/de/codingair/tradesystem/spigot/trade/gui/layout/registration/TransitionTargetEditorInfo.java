package de.codingair.tradesystem.spigot.trade.gui.layout.registration;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import org.jetbrains.annotations.NotNull;

public class TransitionTargetEditorInfo extends EditorInfo {
    private final Class<? extends TradeIcon> origin;

    public TransitionTargetEditorInfo(@NotNull String name, @NotNull Class<? extends TradeIcon> origin) {
        super(name);
        this.origin = origin;
    }

    public Class<? extends TradeIcon> getOrigin() {
        return origin;
    }

    @Override
    public Type getType() {
        return IconHandler.getInfo(origin).getType();
    }
}
