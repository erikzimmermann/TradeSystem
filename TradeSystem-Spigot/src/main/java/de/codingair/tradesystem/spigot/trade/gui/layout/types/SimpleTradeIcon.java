package de.codingair.tradesystem.spigot.trade.gui.layout.types;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleTradeIcon extends LayoutIcon implements TradeIcon, Clickable, ItemPrepareIcon {
    public SimpleTradeIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public final @NotNull Button getButton(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        int id = trade.getId(player);

        return new Button() {

            @Override
            public ItemStack buildItem() {
                return prepareItemStack(new ItemBuilder(itemStack), trade, player, other, othersName).getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                return isClickable(trade, player, other, othersName);
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                IconResult result = SimpleTradeIcon.this.onClick(trade, player, inventoryClickEvent);
                trade.handleClickResult(SimpleTradeIcon.this, player, id, gui, result, 0);
            }
        };
    }

    /**
     * Executed when the player clicks on this trade icon.
     *
     * @param trade  The trade instance.
     * @param player The player which opened this GUI.
     * @param event  The click event which triggered this function.
     * @return A {@link IconResult} for the trade behavior after clicking on this icon. Useful for opening other GUIs like the SignGUI.
     */
    public abstract @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Player player, @NotNull InventoryClickEvent event);
}
