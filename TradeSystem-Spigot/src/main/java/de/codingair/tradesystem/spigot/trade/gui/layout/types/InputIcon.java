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
public abstract class InputIcon<G> extends LayoutIcon implements TradeIcon, Clickable, Input<G>, ItemPrepareIcon {
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
                    public IconResult processInput(@NotNull Trade trade, @NotNull Player player, @Nullable G input, @NotNull String origin) {
                        return InputIcon.this.processInput(trade, player, input, origin);
                    }

                    @Override
                    protected void handleResult(TradeIcon icon, GUI gui, IconResult result, @NotNull Trade trade, int id) {
                        super.handleResult(InputIcon.this, gui, result, trade, id);
                    }

                    @Override
                    public @NotNull String makeString(@NotNull Player player, @Nullable G current) {
                        return InputIcon.this.makeString(player, current);
                    }

                    @Override
                    public @Nullable G getValue() {
                        return InputIcon.this.getValue();
                    }

                    @Override
                    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
                        return InputIcon.this.prepareItemStack(layout, trade, player, other, othersName);
                    }

                    @Override
                    public boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
                        return InputIcon.this.isClickable(trade, player, other, othersName);
                    }

                    @Override
                    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
                        InputIcon.this.onFinish(trade, player, other, othersName, initiationServer);
                    }

                    @Override
                    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
                        return InputIcon.this.tryFinish(trade, player, other, othersName, initiationServer);
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
                    public IconResult processInput(@NotNull Trade trade, @NotNull Player player, @Nullable G input, @NotNull String origin) {
                        return InputIcon.this.processInput(trade, player, input, origin);
                    }

                    @Override
                    protected void handleResult(TradeIcon icon, GUI gui, IconResult result, @NotNull Trade trade, int id) {
                        super.handleResult(InputIcon.this, gui, result, trade, id);
                    }

                    @Override
                    public @NotNull String makeString(@NotNull Player player, @Nullable G current) {
                        return InputIcon.this.makeString(player, current);
                    }

                    @Override
                    public @Nullable G getValue() {
                        return InputIcon.this.getValue();
                    }

                    @Override
                    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
                        return InputIcon.this.prepareItemStack(layout, trade, player, other, othersName);
                    }

                    @Override
                    public boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
                        return InputIcon.this.isClickable(trade, player, other, othersName);
                    }

                    @Override
                    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
                        InputIcon.this.onFinish(trade, player, other, othersName, initiationServer);
                    }

                    @Override
                    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
                        return InputIcon.this.tryFinish(trade, player, other, othersName, initiationServer);
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
    public final @NotNull Button getButton(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        return this.icon.getButton(trade, player, other, othersName);
    }
}
