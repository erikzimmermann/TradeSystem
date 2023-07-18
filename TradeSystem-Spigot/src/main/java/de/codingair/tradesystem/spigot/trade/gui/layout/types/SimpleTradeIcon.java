package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class SimpleTradeIcon extends LayoutIcon implements TradeIcon, Clickable, ItemPrepareIcon {
    public SimpleTradeIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public final @NotNull Button getButton(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        return new Button() {

            @Override
            public ItemStack buildItem() {
                return prepareItemStack(new ItemBuilder(itemStack), trade, perspective, viewer).getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                return isClickable(trade, perspective, viewer);
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                IconResult result = SimpleTradeIcon.this.onClick(trade, perspective, viewer, inventoryClickEvent);
                trade.handleClickResult(SimpleTradeIcon.this, perspective, gui, result);
            }
        };
    }

    /**
     * Executed when the player clicks on this trade icon.
     *
     * @param trade       The trade instance.
     * @param perspective The perspective of the player who clicked on this icon.
     * @param viewer      The player which opened this GUI.
     * @param event       The click event which triggered this function.
     * @return A {@link IconResult} for the trade behavior after clicking on this icon. Useful for opening other GUIs like the SignGUI.
     */
    public abstract @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @NotNull InventoryClickEvent event);
}
