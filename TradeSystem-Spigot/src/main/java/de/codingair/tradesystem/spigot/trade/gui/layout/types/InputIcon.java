package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.gui.AnvilGUIIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.gui.SignGUIIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.gui.SimpleAnvilGUIIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.gui.SimpleSignGUIIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.utils.TriFunction;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Combines the {@link SignGUIIcon} and the {@link AnvilGUIIcon} and chooses the one which is selected by the main configuration file.
 *
 * @param <G> The type of the final value which will be modified by this input icon.
 */
public abstract class InputIcon<G> extends LayoutIcon implements TradeIcon, Clickable, StateHolder, Input<G>, ItemPrepareIcon {
    private final TradeIcon icon;

    public InputIcon(@NotNull ItemStack itemStack) {
        super(itemStack);

        switch (TradeSystem.handler().getInputGUI()) {
            case SIGN:
                this.icon = new SimpleSignGUIIcon<G>(itemStack) {
                    @Override
                    public @Nullable G convertInput(@NotNull String input) {
                        return InputIcon.this.convertInput(input);
                    }

                    @Override
                    public IconResult processInput(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable G input, @NotNull String origin) {
                        return InputIcon.this.processInput(trade, perspective, viewer, input, origin);
                    }

                    @Override
                    protected void handleResult(@NotNull TradeIcon icon, @NotNull GUI gui, @NotNull IconResult result, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Perspective viewer) {
                        super.handleResult(InputIcon.this, gui, result, trade, perspective, viewer);
                    }

                    @Override
                    public @NotNull String makeString(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable G current, boolean payment) {
                        return InputIcon.this.makeString(trade, perspective, viewer, current, true);
                    }

                    @Override
                    public @Nullable G getValue() {
                        return InputIcon.this.getValue();
                    }

                    @Override
                    public void setValue(G value) {
                        InputIcon.this.setValue(value);
                    }

                    @Override
                    public G getDefault() {
                        return InputIcon.this.getDefault();
                    }

                    @Override
                    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
                        return InputIcon.this.prepareItemStack(layout, trade, perspective, viewer);
                    }

                    @Override
                    public boolean isClickable(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
                        return InputIcon.this.isClickable(trade, perspective, viewer);
                    }

                    @Override
                    public void onFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
                        InputIcon.this.onFinish(trade, perspective, viewer, initiationServer);
                    }

                    @Override
                    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
                        return InputIcon.this.tryFinish(trade, perspective, viewer, initiationServer);
                    }

                    @Override
                    public boolean isEmpty() {
                        return InputIcon.this.isEmpty();
                    }

                    @Override
                    public void serialize(@NotNull DataOutputStream out) throws IOException {
                        InputIcon.this.serialize(out);
                    }

                    @Override
                    public void deserialize(@NotNull DataInputStream in) throws IOException {
                        InputIcon.this.deserialize(in);
                    }
                };
                break;

            case ANVIL:
            default:
                this.icon = new SimpleAnvilGUIIcon<G>(itemStack) {
                    @Override
                    public @Nullable G convertInput(@NotNull String input) {
                        return InputIcon.this.convertInput(input);
                    }

                    @Override
                    public IconResult processInput(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable G input, @NotNull String origin) {
                        return InputIcon.this.processInput(trade, perspective, viewer, input, origin);
                    }

                    @Override
                    protected void handleResult(@NotNull TradeIcon icon, @NotNull GUI gui, @NotNull IconResult result, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Perspective viewer) {
                        super.handleResult(InputIcon.this, gui, result, trade, perspective, viewer);
                    }

                    @Override
                    public @NotNull String makeString(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable G current, boolean payment) {
                        return InputIcon.this.makeString(trade, perspective, viewer, current, true);
                    }

                    @Override
                    public @Nullable G getValue() {
                        return InputIcon.this.getValue();
                    }

                    @Override
                    public void setValue(G value) {
                        InputIcon.this.setValue(value);
                    }

                    @Override
                    public G getDefault() {
                        return InputIcon.this.getDefault();
                    }

                    @Override
                    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
                        return InputIcon.this.prepareItemStack(layout, trade, perspective, viewer);
                    }

                    @Override
                    public boolean isClickable(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
                        return InputIcon.this.isClickable(trade, perspective, viewer);
                    }

                    @Override
                    public void onFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
                        InputIcon.this.onFinish(trade, perspective, viewer, initiationServer);
                    }

                    @Override
                    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
                        return InputIcon.this.tryFinish(trade, perspective, viewer, initiationServer);
                    }

                    @Override
                    public boolean isEmpty() {
                        return InputIcon.this.isEmpty();
                    }

                    @Override
                    public void serialize(@NotNull DataOutputStream out) throws IOException {
                        InputIcon.this.serialize(out);
                    }

                    @Override
                    public void deserialize(@NotNull DataInputStream in) throws IOException {
                        InputIcon.this.deserialize(in);
                    }
                };
                break;
        }
    }

    @Override
    public final @NotNull Button getButton(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        return this.icon.getButton(trade, perspective, viewer);
    }

    @Override
    public boolean isDisabled() {
        if (!(icon instanceof StateHolder)) throw new IllegalStateException("Icon has no state: " + icon.getClass());
        return ((StateHolder) icon).isDisabled();
    }

    @Override
    public void enable() {
        if (!(icon instanceof StateHolder)) throw new IllegalStateException("Icon has no state: " + icon.getClass());
        ((StateHolder) icon).enable();
    }

    @Override
    public void disable(@Nullable TriFunction<Trade, Perspective, Player, String> onClickMessage) {
        if (!(icon instanceof StateHolder)) throw new IllegalStateException("Icon has no state: " + icon.getClass());
        ((StateHolder) icon).disable(onClickMessage);
    }
}
