package de.codingair.tradesystem.spigot.trade.gui.layout.types.utils;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.SignButton;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.*;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class SignGUIIcon<G> extends LayoutIcon implements TradeIcon, Clickable, Input<G>, ItemPrepareIcon {
    public SignGUIIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public final @NotNull Button getButton(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        int id = trade.getId(player);

        String[] built = buildSignLines(trade, player);

        String[] text;
        if (built == null) text = new String[0];
        else if (built.length > 4) {
            throw new IllegalStateException("Cannot open a SignGUI with more than 4 lines! Note that the first line will be used for the player input. Lines: " + Arrays.toString(built));
        } else {
            text = built;
        }

        return new SignButton(text) {
            @Override
            public boolean onSignChangeEvent(GUI gui, String[] input) {
                //executed on InventoryCloseEvent too
                if (trade.isCancelling()) return true;

                String origin = input[0];
                G in = convertInput(origin);
                IconResult result = processInput(trade, player, in, origin);

                if (result == IconResult.GUI) {
                    //update first line before reopening
                    super.lines[0] = input[0];
                    return false;
                }

                handleResult(SignGUIIcon.this, gui, result, trade, id);
                return true;
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

    protected  void handleResult(TradeIcon icon, GUI gui, IconResult result, @NotNull Trade trade, int id) {
        trade.handleClickResult(icon, id, gui, result, 1);
    }

    /**
     * @param trade  The trade instance.
     * @param player The trading player.
     * @return The sign GUI lines. Must have a maximum length of 3 (the very first line of the SignGUI will be used for the input). Will be ignored if returning null.
     */
    public abstract @Nullable String[] buildSignLines(@NotNull Trade trade, @NotNull Player player);
}
