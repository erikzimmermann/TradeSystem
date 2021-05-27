package de.codingair.tradesystem.spigot.trade.layout.registration;

import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;
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
}
