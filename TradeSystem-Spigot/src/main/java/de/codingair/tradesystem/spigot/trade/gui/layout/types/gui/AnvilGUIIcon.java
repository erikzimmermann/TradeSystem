package de.codingair.tradesystem.spigot.trade.gui.layout.types.gui;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilSlot;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.AnvilButton;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.*;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class AnvilGUIIcon<G> extends LayoutIcon implements TradeIcon, Clickable, Input<G>, ItemPrepareIcon {
    public AnvilGUIIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public final @NotNull Button getButton(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        return new AnvilButton() {
            @Override
            public void onAnvil(GUI fallback, AnvilClickEvent e) {
                if (!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

                String origin = e.getInput(false);
                if (origin == null) origin = "";

                G in = convertInput(origin);
                IconResult result = processInput(trade, perspective, viewer, in, origin);

                getClickSound().play(viewer);
                if (result != IconResult.GUI) {
                    //won't be closed until we say it.
                    e.setClose(true);
                    handleResult(AnvilGUIIcon.this, fallback, result, trade, perspective);
                }
            }

            @Override
            public ItemStack buildAnvilItem() {
                return AnvilGUIIcon.this.buildAnvilItem(trade, viewer);
            }

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
                trade.acknowledgeGuiSwitch(viewer);  // fixes dupe glitch
            }

            @Override
            public boolean canSwitch(ClickType clickType) {
                return true;
            }
        }.setTitle(Lang.get("AnvilGUI_Title", viewer));
    }

    protected void handleResult(@NotNull TradeIcon icon, @NotNull GUI gui, @NotNull IconResult result, @NotNull Trade trade, @NotNull Perspective perspective) {
        trade.handleClickResult(icon, perspective, gui, result);
    }

    /**
     * @param trade  The trade instance.
     * @param viewer The player that is viewing the trade GUI. This is not necessarily the trading player.
     * @return The AnvilGUI item which will be used for the rename function.
     */
    public abstract @NotNull ItemStack buildAnvilItem(@NotNull Trade trade, @NotNull Player viewer);
}
