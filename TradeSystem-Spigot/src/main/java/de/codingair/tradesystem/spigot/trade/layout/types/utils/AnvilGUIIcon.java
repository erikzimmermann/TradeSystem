package de.codingair.tradesystem.spigot.trade.layout.types.utils;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilSlot;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.AnvilButton;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.layout.types.*;
import de.codingair.tradesystem.spigot.trade.layout.types.feedback.IconResult;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AnvilGUIIcon<G> extends LayoutIcon implements TradeIcon, Clickable, Input<G>, ItemPrepareIcon {
    public AnvilGUIIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public final @NotNull Button getButton(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        int id = trade.getId(player);

        return new AnvilButton() {
            @Override
            public void onAnvil(GUI fallback, AnvilClickEvent e) {
                if (!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

                String origin = e.getInput(false);
                G in = convertInput(origin);
                IconResult result = processInput(trade, player, in, origin);

                getClickSound().play(player);
                if (result != IconResult.GUI) {
                    //won't be closed until we say it.
                    e.setClose(true);
                    handleResult(AnvilGUIIcon.this, fallback, result, trade, id);
                }
            }

            @Override
            public ItemStack buildAnvilItem() {
                return AnvilGUIIcon.this.buildAnvilItem(trade, player);
            }

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
                //ignore and just make the click sound
            }

            @Override
            public boolean canSwitch(ClickType clickType) {
                return true;
            }
        };
    }

    protected void handleResult(TradeIcon icon, GUI gui, IconResult result, @NotNull Trade trade, int id) {
        trade.handleClickResult(icon, id, gui, result, 1);
    }

    /**
     * @param trade  The trade instance.
     * @param player The trading player.
     * @return The AnvilGUI item which will be used for the rename function.
     */
    public abstract @NotNull ItemStack buildAnvilItem(@NotNull Trade trade, @NotNull Player player);
}
