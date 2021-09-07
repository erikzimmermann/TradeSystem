package de.codingair.tradesystem.spigot.trade.gui.layout.types.utils;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.SimpleTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ShowIcon extends SimpleTradeIcon {
    public ShowIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public final @NotNull IconResult onClick(@NotNull Trade trade, @NotNull Player player, @NotNull InventoryClickEvent event) {
        return IconResult.PASS;
    }

    @Override
    public final boolean isEmpty() {
        return true;
    }

    @Override
    public final void serialize(@NotNull DataOutputStream out) throws IOException {
        //ignore
    }

    @Override
    public final void deserialize(@NotNull DataInputStream in) throws IOException {
        //ignore
    }
}
