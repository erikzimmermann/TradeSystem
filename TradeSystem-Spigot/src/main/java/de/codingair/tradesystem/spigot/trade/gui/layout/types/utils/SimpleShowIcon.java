package de.codingair.tradesystem.spigot.trade.gui.layout.types.utils;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleShowIcon<G> extends ShowIcon implements Transition.Consumer<G> {

    public SimpleShowIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        return false;
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        return FinishResult.PASS;
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        //this icon only shows some information from the trade partner
    }
}
